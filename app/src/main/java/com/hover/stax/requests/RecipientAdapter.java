package com.hover.stax.requests;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.contacts.ContactInput;
import com.hover.stax.contacts.StaxContact;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {
    private List<StaxContact> recipients;
    private List<StaxContact> allContacts;
    private final UpdateListener updateListener;

    RecipientAdapter(List<StaxContact> recips, List<StaxContact> contacts, UpdateListener listener) {
        recipients = recips;
        allContacts = contacts != null ? contacts : new ArrayList<>();
        updateListener = listener;
    }

    void update(List<StaxContact> recips) {
        recipients = recips;
        Timber.e("Recipients %s", recipients);
        notifyDataSetChanged();
    }

    void updateContactList(List<StaxContact> contacts) {
        allContacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactInput ci = new ContactInput(parent.getContext(), null);
        ci.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new RecipientViewHolder(ci);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecipientViewHolder holder, int position) {
        ContactInput ci = (ContactInput) holder.itemView;
        ci.setHint(ci.getContext().getString(R.string.send_request_to));
        ci.setRecent(allContacts, ci.getContext());


        if (recipients != null && recipients.size() > position && recipients.get(position).accountNumber != null)
            ci.setText(recipients.get(position).toString(), false);

        ci.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!recipients.get(position).toString().equals(charSequence.toString()))
                    updateListener.onUpdate(position, new StaxContact(charSequence.toString()));
            }
        });

        ci.setOnItemClickListener((adapterView, view, pos, id) -> {
            StaxContact contact = (StaxContact) adapterView.getItemAtPosition(pos);
            updateListener.onUpdate(position, contact);
        });

        ci.setChooseContactListener(view -> updateListener.onClickContact(position, ci.getContext()));
    }

    public interface UpdateListener {
        void onUpdate(int pos, StaxContact recipient);

        void onClickContact(int index, Context c);
    }

    static class RecipientViewHolder extends RecyclerView.ViewHolder {
        RecipientViewHolder(@NonNull ContactInput itemView) {
            super(itemView);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return recipients == null ? 0 : recipients.size();
    }
}