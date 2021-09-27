package com.hover.stax.transactions;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.api.Hover;
import com.hover.sdk.api.HoverParameters;
import com.hover.sdk.transactions.Transaction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import timber.log.Timber;

@Entity(tableName = "stax_transactions", indices = {@Index(value = {"uuid"}, unique = true)})
public class StaxTransaction {

    public final static String CONFIRM_CODE_KEY = "confirmCode", FEE_KEY = "fee", CATEGORY_INCOMPLETE_SESSION = "incomplete_session";

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

    @ColumnInfo(name = "recipient_id")
    public String counterparty_id;

    @ColumnInfo(name = "category")
    public String category;

    // FIXME: DO not use! This is covered by contact model. No easy way to drop column yet, but room 2.4 adds an easy way. Currently alpha, use once it is stable
    @ColumnInfo(name = "counterparty")
    public String counterparty;

    public StaxTransaction() {
    }

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

            counterparty_id = contact.id;
            description = generateDescription(action, contact, c);
            parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS));
            Timber.v("creating transaction with uuid: %s", uuid);
        }
    }

    public void update(Intent data, HoverAction action, StaxContact contact, Boolean isNewTransaction, Context c) {
        if( !isNewTransaction && isSessionIncomplete(action, c)) setFailed_Incomplete();
        else status = data.getStringExtra(TransactionContract.COLUMN_STATUS);

        Timber.e("Updating to status %s - %s", status, action);
        updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at);

        parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES));

        if (counterparty_id == null)
            counterparty_id = contact.id;

        description = generateDescription(action, contact, c);
    }

    private Boolean isSessionIncomplete(HoverAction action, Context c)   {
        int numOfSteps = action.custom_steps.length();
        int ussdLength = Hover.getTransaction(uuid, c).ussdMessages.length();
        return ussdLength < numOfSteps -1;
    }

    public void setFailed_Incomplete() {
        status = Transaction.FAILED;
        category = CATEGORY_INCOMPLETE_SESSION;
    }

    private void parseExtras(HashMap<String, String> extras) {
        if (extras == null) return;

        if (extras.containsKey(HoverAction.AMOUNT_KEY))
            amount = Utils.getAmount(extras.get(HoverAction.AMOUNT_KEY));
        if (extras.containsKey(FEE_KEY))
            fee = Utils.getAmount(extras.get(FEE_KEY));
        if (extras.containsKey(CONFIRM_CODE_KEY))
            confirm_code = extras.get(CONFIRM_CODE_KEY);
    }

    private String generateDescription(HoverAction action, StaxContact contact, Context c) {
        if (isRecorded())
            return c.getString(R.string.descrip_recorded, action.from_institution_name);

        switch (transaction_type) {
            case HoverAction.AIRTIME:
                return c.getString(R.string.descrip_airtime_sent, action.from_institution_name, contact == null ? c.getString(R.string.self_choice) : contact.shortName());
            case HoverAction.P2P:
                return c.getString(R.string.descrip_transfer_sent, action.from_institution_name, contact.shortName());
            case HoverAction.ME2ME:
                return c.getString(R.string.descrip_transfer_sent, action.from_institution_name, action.to_institution_name);
            case HoverAction.C2B:
                return c.getString(R.string.descrip_bill_paid, action.to_institution_name);
            case HoverAction.RECEIVE:
                return c.getString(R.string.descrip_transfer_received, contact.shortName());
            default:
                return "Other";
        }
    }

    public TransactionStatus getFullStatus() {
        return new TransactionStatus(this);
    }

    public boolean isRecorded() {
        return environment == HoverParameters.MANUAL_ENV;
    }

    public String getDisplayAmount() {
        if (amount != null) {
            String a = Utils.formatAmount(amount);
            if (!transaction_type.equals(HoverAction.RECEIVE))
                a = "-" + a;
            else if (isRecorded())
                return "\\u2014";
            return a;
        } else return null;
    }

    @NotNull
    @Override
    public String toString() {
        return description;
    }
}