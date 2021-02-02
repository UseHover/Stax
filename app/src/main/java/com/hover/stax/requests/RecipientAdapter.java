package com.hover.stax.requests;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.contacts.StaxContactArrayAdapter;

import java.util.List;

public class RecipientAdapter extends RecyclerView.Adapter<RecipientAdapter.RecipientViewHolder> {
	private List<StaxContact> recipients;
	private List<StaxContact> allContacts;
	private UpdateListener updateListener;

	RecipientAdapter(List<StaxContact> recipients, List<StaxContact> contacts, UpdateListener listener) {
		this.recipients = recipients;
		allContacts = contacts;
		updateListener = listener;
	}

	void update(List<StaxContact> recips) { recipients = recips; notifyDataSetChanged(); }

	void updateContactList(List<StaxContact> contacts) { allContacts = contacts; notifyDataSetChanged(); }

	@NonNull
	@Override
	public RecipientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipient_input, parent, false);
		return new RecipientViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final @NonNull RecipientViewHolder holder, int position) {
		holder.itemView.setVisibility(View.VISIBLE);

		try{
			ArrayAdapter<StaxContact> adapter = new StaxContactArrayAdapter(holder.view.getContext(), allContacts);
			holder.input.setAdapter(adapter);
		}catch (NullPointerException ignored) {
			//All contact list may return null whilst loading. The updateContactList method auto refreshes this after load completes.
		}


		if (recipients != null && recipients.size() > position && recipients.get(position).getPhoneNumber() != null)
			holder.input.setText(recipients.get(position).toString());

		holder.input.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void afterTextChanged(Editable editable) { }
			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (!recipients.get(position).toString().equals(charSequence.toString()))
					updateListener.onUpdate(position, new StaxContact(charSequence.toString()));
			}
		});

		holder.input.setOnItemClickListener((adapterView, view, pos, id) -> {
			StaxContact contact = (StaxContact) adapterView.getItemAtPosition(pos);
			updateListener.onUpdate(position, contact);
		});

		holder.contactButton.setOnClickListener(view -> updateListener.onClickContact(position, holder.view.getContext()));
	}

	public interface UpdateListener {
		void onUpdate(int pos, StaxContact recipient);
		void onClickContact(int index, Context c);
	}

	static class RecipientViewHolder extends RecyclerView.ViewHolder {
		final View view;
		final AutoCompleteTextView input;
		final AppCompatImageButton contactButton;

		RecipientViewHolder(@NonNull View itemView) {
			super(itemView);
			view = itemView;
			input = itemView.findViewById(R.id.recipient_autocomplete);
			contactButton = itemView.findViewById(R.id.contact_button);
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