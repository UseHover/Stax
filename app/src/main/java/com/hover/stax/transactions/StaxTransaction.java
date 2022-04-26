package com.hover.stax.transactions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

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
import com.hover.stax.accounts.*;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import timber.log.Timber;

@Entity(tableName = "stax_transactions", indices = {@Index(value = {"uuid"}, unique = true)})
public class StaxTransaction {

    public final static String CONFIRM_CODE_KEY = "confirmCode", FEE_KEY = "fee";
    public final static String MMI_ERROR = "mmi-error", PIN_ERROR = "pin-error", BALANCE_ERROR = "balance-error",
            UNREGISTERED_ERROR = "unregistered-error", INVALID_ENTRY_ERROR = "invalid-entry",
            NO_RESPONSE_ERROR = "no-response", INCOMPLETE_ERROR = "incomplete";

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

    @ColumnInfo(name = "category")
    public String category;

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

    @ColumnInfo(name = "balance")
    public String balance;

    @ColumnInfo(name = "account_id")
    public Integer accountId;

    @ColumnInfo(name = "account_name")
    public Integer accountName;

    @ColumnInfo(name = "note")
    public String note;

    // FIXME: DO not use! This is covered by contact model. No easy way to drop column yet, but room 2.4 adds an easy way. Currently alpha, use once it is stable
    @ColumnInfo(name = "counterparty")
    public String counterparty;

    public StaxTransaction() {
    }

    public StaxTransaction(Intent data, HoverAction action, StaxContact contact, Context c) {
        if (data.hasExtra(TransactionContract.COLUMN_UUID) && data.getStringExtra(TransactionContract.COLUMN_UUID) != null) {
            uuid = data.getStringExtra(TransactionContract.COLUMN_UUID);
            channel_id = data.getIntExtra(TransactionContract.COLUMN_CHANNEL_ID, -1);
            action_id = data.getStringExtra(TransactionContract.COLUMN_ACTION_ID);
            transaction_type = data.getStringExtra(TransactionContract.COLUMN_TYPE);
            environment = data.getIntExtra(TransactionContract.COLUMN_ENVIRONMENT, 0);
            status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
            category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY);
            initiated_at = data.getLongExtra(TransactionContract.COLUMN_REQUEST_TIMESTAMP, DateUtils.now());
            updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at);

            counterparty_id = contact.id;
            description = generateDescription(action, contact, c);

            parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS));
            parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES));
            Timber.v("creating transaction with uuid: %s", uuid);
        }
    }

    public void update(Intent data, HoverAction action, StaxContact contact, Context c) {
        status = data.getStringExtra(TransactionContract.COLUMN_STATUS);
        category = data.getStringExtra(TransactionContract.COLUMN_CATEGORY);
        parseExtras((HashMap<String, String>) data.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES));

        if (counterparty_id == null) counterparty_id = contact.id;
        description = generateDescription(action, contact, c);
        updated_at = data.getLongExtra(TransactionContract.COLUMN_UPDATE_TIMESTAMP, initiated_at);
    }

    private void parseExtras(HashMap<String, String> extras) {
        if (extras == null) return;

        Timber.e("Extras %s", extras.keySet());

        if (extras.containsKey(HoverAction.AMOUNT_KEY))
            amount = Utils.getAmount(extras.get(HoverAction.AMOUNT_KEY));
        if (extras.containsKey(FEE_KEY))
            fee = Utils.getAmount(extras.get(FEE_KEY));
        if (extras.containsKey(CONFIRM_CODE_KEY))
            confirm_code = extras.get(CONFIRM_CODE_KEY);
        if (extras.containsKey(HoverAction.BALANCE))
            balance = extras.get(HoverAction.BALANCE);
        if (extras.containsKey(AccountKt.ACCOUNT_ID))
            accountId = Integer.parseInt(extras.get(AccountKt.ACCOUNT_ID));
        if (extras.containsKey(HoverAction.NOTE_KEY))
            note = extras.get(HoverAction.NOTE_KEY);
    }

    private String generateDescription(HoverAction action, StaxContact contact, Context c) {
        if (isRecorded())
            return c.getString(R.string.descrip_recorded, action.from_institution_name);

        switch (transaction_type) {
            case HoverAction.BALANCE:
                return c.getString(R.string.descrip_balance, action.from_institution_name);
            case HoverAction.AIRTIME:
                return c.getString(R.string.descrip_airtime_sent, getDisplayAmount(), contact == null ? c.getString(R.string.self_choice) : contact.shortName());
            case HoverAction.P2P:
                return c.getString(R.string.descrip_transfer_sent, getDisplayAmount(), contact.shortName());
            case HoverAction.ME2ME:
                return c.getString(R.string.descrip_transfer_sent, getDisplayAmount(), action.to_institution_name);
            case HoverAction.C2B:
                return c.getString(R.string.descrip_bill_paid, getDisplayAmount(), action.to_institution_name);
            case HoverAction.RECEIVE:
                return c.getString(R.string.descrip_transfer_received, contact.shortName());
            case HoverAction.FETCH_ACCOUNTS:
                return c.getString(R.string.descrip_fetch_accounts, action.from_institution_name);
            default:
                return "Other";
        }
    }

    @SuppressLint("DefaultLocale")
    public String generateLongDescription(HoverAction action, StaxContact contact, Context c) {
        if (isRecorded())
            return c.getString(R.string.descrip_recorded, action.from_institution_name);

        switch (transaction_type) {
            case HoverAction.BALANCE:
                return c.getString(R.string.descrip_balance, action.from_institution_name);
            case HoverAction.AIRTIME:
                return c.getString(R.string.descrip_long_airtime_send, getDisplayAmount(), action.from_institution_name, contact == null ? c.getString(R.string.self_choice) : contact.shortName());
            case HoverAction.P2P:
                return c.getString(R.string.descrip_long_transfer_send, getDisplayAmount(), action.from_institution_name, contact == null ? " " : contact.shortName(), action.to_institution_name);
            case HoverAction.ME2ME:
                return c.getString(R.string.descrip_long_move, getDisplayAmount(), action.from_institution_name, action.to_institution_name);
            case HoverAction.C2B:
                return c.getString(R.string.descrip_long_bill_paid, getDisplayAmount(), action.from_institution_name, action.to_institution_name);
            case HoverAction.RECEIVE:
                return c.getString(R.string.descrip_transfer_received, contact.shortName());
            default:
                return "Other";
        }
    }

    public TransactionStatus getFullStatus() {
        return new TransactionStatus(this);
    }
    public Boolean  isFailed(){ return status.equals(Transaction.FAILED); }
    public Boolean isSuccessful() {return status.equals(Transaction.SUCCEEDED);}
    public Boolean isBalanceType() {return transaction_type.equals(HoverAction.BALANCE);}

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
        } else return "";
    }

    public String getDisplayBalance() {
        if(!balance.isEmpty()) {
            return Utils.formatAmount(balance);
        }
        else return balance;
    }

    @NotNull
    @Override
    public String toString() {
        return description;
    }
}