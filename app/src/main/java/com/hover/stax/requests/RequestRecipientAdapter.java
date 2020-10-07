package com.hover.stax.requests;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;

import java.util.List;

public class RequestRecipientAdapter extends RecyclerView.Adapter<RequestRecipientAdapter.RequestFromWhoInputViewHolder> {

	private List<Request> requestList;
	private ContactClickListener contactClickListener;

	public RequestRecipientAdapter(List<Request> requestList, ContactClickListener contactClickListener) {
		this.requestList = requestList;
		this.contactClickListener = contactClickListener;
	}


	@NonNull
	@Override
	public RequestFromWhoInputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.request_fromwho_input, parent, false);
		return new RequestFromWhoInputViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull RequestFromWhoInputViewHolder holder, int position) {
		Request request = requestList.get(position);

		EditText editText = holder.editText;
		editText.setTag(request.tag);
		editText.setText(request.recipient);
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				contactClickListener.onEditText(request.tag, s.toString());
			}
		});

		holder.contactButton.setOnClickListener(v -> contactClickListener.onClick(request.tag));
	}

	@Override
	public int getItemCount() {
		return requestList != null ? requestList.size() : 0;
	}

	static class RequestFromWhoInputViewHolder extends RecyclerView.ViewHolder {

		private EditText editText;
		private ImageButton contactButton;

		RequestFromWhoInputViewHolder(@NonNull View itemView) {
			super(itemView);
			editText = itemView.findViewById(R.id.recipient_number);
			contactButton = itemView.findViewById(R.id.contact_button);
		}
	}

	interface ContactClickListener {
		void onClick(int tag);

		void onEditText(int tag, String content);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
