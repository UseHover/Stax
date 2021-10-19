package com.hover.stax.hover;

import static org.koin.java.KoinJavaComponent.get;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hover.sdk.actions.HoverAction;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.account.Account;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.requests.Request;
import com.hover.stax.transactions.StaxTransaction;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class TransactionReceiver extends BroadcastReceiver {

    private final DatabaseRepo repo = get(DatabaseRepo.class);
    private Channel channel;
    private Account account;
    private HoverAction action;
    private StaxContact contact;

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.e("got an update. %s", intent.getStringExtra(TransactionContract.COLUMN_UUID));
        new Thread(() -> {
            Timber.e("thread");
            action = repo.getAction(intent.getStringExtra(TransactionContract.COLUMN_ACTION_ID));
            channel = repo.getChannel(action.channel_id);
            createAccounts(intent);
            updateBalance(intent);
            updateContacts(intent);
            updateTransaction(intent, context.getApplicationContext());
            updateRequests(intent);
        }).start();
    }

    private void updateBalance(Intent intent) {
        if (intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS)) {
            HashMap<String, String> input_extras = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS);
            if (input_extras != null && input_extras.containsKey(Constants.ACCOUNT_ID)) {
                String accountId = input_extras.get(Constants.ACCOUNT_ID);
                if (accountId != null)
                    account = repo.getAccount(Integer.parseInt(accountId));
            }
        }

        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
            if (account != null && parsed_variables != null && parsed_variables.containsKey("balance")) {
                account.updateBalance(parsed_variables);
                repo.update(account);
            }
        }
    }

    private void updateContacts(Intent intent) {
        contact = StaxContact.findOrInit(intent, channel.countryAlpha2, repo);
        contact.updateNames(intent);
        repo.save(contact);
    }

    private void updateTransaction(final Intent intent, Context c) {
        Timber.e("updating t");
        repo.insertOrUpdateTransaction(intent, action, contact, c);
    }

    private void updateRequests(Intent intent) {
        if (intent.getStringExtra(TransactionContract.COLUMN_TYPE).equals(HoverAction.RECEIVE)) {
            List<Request> rs = repo.getRequests();
            for (Request r: rs) {
                if (r.requestee_ids.contains(contact.id) && Utils.getAmount(r.amount) == Utils.getAmount(getAmount(intent))) {
                    r.matched_transaction_uuid = intent.getStringExtra(TransactionContract.COLUMN_UUID);
                    repo.update(r);
                }
            }
        }
    }

    private String getAmount(Intent intent) {
        String amount = null;
        if (intent.hasExtra(TransactionContract.COLUMN_INPUT_EXTRAS))
            amount = getAmount((HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_INPUT_EXTRAS));
        if (amount == null && intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES))
            amount = getAmount((HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES));
        return amount;
    }

    private String getAmount(HashMap<String, String> extras) {
        if (extras != null && extras.containsKey(HoverAction.AMOUNT_KEY)) {
            return extras.get(HoverAction.AMOUNT_KEY);
        } else return null;
    }

    private void createAccounts(Intent intent) {
        List<Account> accounts = repo.getAllAccounts();
        if (intent.hasExtra(TransactionContract.COLUMN_PARSED_VARIABLES)) {
            HashMap<String, String> parsed_variables = (HashMap<String, String>) intent.getSerializableExtra(TransactionContract.COLUMN_PARSED_VARIABLES);
            if (parsed_variables != null && parsed_variables.containsKey("userAccountList")) {
                accounts.addAll(parseAccounts(parsed_variables.get("userAccountList")));
            }
        }
        repo.saveAccounts(accounts);
    }

    private List<Account> parseAccounts(String accountList) {
        Pattern pattern = Pattern.compile("^[\\d]{1,2}[>):.\\s]+(.+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(accountList);

        ArrayList<Account> accounts = new ArrayList<>();
        while (matcher.find()) {
            Account new_account = new Account(Objects.requireNonNull(matcher.group(1)), channel);
            if (!accounts.contains(new_account))
                accounts.add(new_account);
        }
        if (repo.getDefaultAccount() != null && accounts.size() > 0)
            accounts.get(0).setDefault(true);

        return accounts;
    }
}
