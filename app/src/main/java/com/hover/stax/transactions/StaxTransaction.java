package com.hover.stax.transactions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.HoverParameters;
import com.hover.sdk.transactions.Transaction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Entity(tableName = "stax_transactions", indices = {@Index(value = {"uuid"}, unique = true)})
public class StaxTransaction {

    public final static String CONFIRM_CODE_KEY = "confirmCode", SENDER_NAME_KEY = "senderName", SENDER_PHONE_KEY = "senderPhone", SENDER_ACCOUNT_KEY = "senderAccount";

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int id;

    @NonNull
    @ColumnInfo(name = "uuid")
    public String uuid;

    @NonNull
    @ColumnInfo(name = "action_id")
    public String action_id;

    @NonNull
    @ColumnInfo(name = "environment", defaultValue = "0")
    public Integer environment;

    @NonNull
    @ColumnInfo(name = "transaction_type")
    public String transaction_type;

    @NonNull
    @ColumnInfo(name = "channel_id")
    public int channel_id;

    @NonNull
    @ColumnInfo(name = "status", defaultValue = Transaction.PENDING)
    public String status;

    @NonNull
    @ColumnInfo(name = "initiated_at", defaultValue = "CURRENT_TIMESTAMP")
    public Long initiated_at;

    @NonNull
    @ColumnInfo(name = "updated_at", defaultValue = "CURRENT_TIMESTAMP")
    public Long updated_at;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "amount")
    public Double amount;

    @ColumnInfo(name = "fee")
    public Double fee;

    @ColumnInfo(name = "confirm_code")
    public String confirm_code;

    @ColumnInfo(name = "counterparty")
    public String counterparty;

    @ColumnInfo(name = "recipient_id")
    public String counterparty_id;


    public StaxTransaction() {}

    public StaxTransaction(Intent data, HoverAction action, StaxContact contact, Context c) {
        if (data.hasExtra(TransactionContract.COLUMN_UUID) && data.getStringExtra(TransactionContract.COLUMN_UUID) != null) {
            uuid = data.getStringExtra(TransactionContract.COLUMN_UUID);
            action_id = data.getStringExtra(TransactionContract.COLUMN_ACTION_ID);
            transaction_type = data.getStringExtra(TransactionContract.COLUMN_TYPE);
            environment = data.getIntExtra(TransactionContract.COLUMN_ENVIRONMENT, 0);
            channel_id = data.getIntExtra(TransactionContract.COLUMN_CHANNEL_ID, -1);
            status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
            initiated_at = data.getLongExtra(TransactionContract.COLUMN_REQUEST_TIMESTAMP, DateUtils.now());
            updated_at = initiated_at;

            parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS));

            if (data.hasExtra(StaxContact.LOOKUP_KEY))
                counterparty_id = data.getStringExtra(StaxContact.LOOKUP_KEY);

            Log.e("Transaction", "creating transaction with uuid: " + uuid);

            if (transaction_type != null) description = generateDescription(action, contact, c);
        }
    }

    public void update(Intent data, HoverAction action, StaxContact contact, Context c) {
        status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
        updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at);

        parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES));

        if (data.hasExtra(StaxContact.LOOKUP_KEY))
            counterparty_id = data.getStringExtra(StaxContact.LOOKUP_KEY);

        if (contact != null)
            description = generateDescription(action, contact, c);
    }

    private void parseExtras(HashMap<String, String> extras) {
        if (extras == null) return;
        if (extras.containsKey(HoverAction.AMOUNT_KEY) && amount == null)
            amount = Utils.getAmount((extras.get(HoverAction.AMOUNT_KEY)));

        if (extras.containsKey(HoverAction.PHONE_KEY))
            counterparty = extras.get(HoverAction.PHONE_KEY);
        else if (extras.containsKey(HoverAction.ACCOUNT_KEY))
            counterparty = extras.get(HoverAction.ACCOUNT_KEY);
        else if (extras.containsKey(SENDER_PHONE_KEY))
            counterparty = extras.get(SENDER_PHONE_KEY);
        else if (extras.containsKey(SENDER_ACCOUNT_KEY))
            counterparty = extras.get(SENDER_ACCOUNT_KEY);

        if (extras.containsKey(HoverAction.FEE_KEY))
            fee = Utils.getAmount(extras.get(HoverAction.FEE_KEY));
        if (extras.containsKey(CONFIRM_CODE_KEY))
            confirm_code = extras.get(CONFIRM_CODE_KEY);
    }

    private String generateDescription(HoverAction action, StaxContact contact, Context c) {
        if (isRecorded())
            return c.getString(R.string.descrip_recorded, action.from_institution_name);

        String recipientStr = contact != null ? contact.shortName() : counterparty;
        switch (transaction_type) {
            case HoverAction.AIRTIME:
                return c.getString(R.string.descrip_airtime_sent, action.from_institution_name, ((counterparty == null || counterparty.equals("")) ? "myself" : recipientStr));
            case HoverAction.P2P:
                return c.getString(R.string.descrip_transfer_sent, action.from_institution_name, recipientStr);
            case HoverAction.ME2ME:
                return c.getString(R.string.descrip_transfer_sent, action.from_institution_name, action.to_institution_name);
            case HoverAction.RECEIVE:
                return c.getString(R.string.descrip_transfer_received, counterparty);
            default:
                return "Other";
        }
    }

    public boolean isRecorded() {
        return environment == HoverParameters.MANUAL_ENV;
    }

    public String getDisplayAmount() {
        String a = Utils.formatAmount(amount);
        if (!transaction_type.equals(HoverAction.RECEIVE))
            a = "-" + a;
        else if (isRecorded())
            return "\\u2014";
        return a;
    }

    @NotNull
    @Override
    public String toString() {
        return description;
    }
}
