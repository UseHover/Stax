package com.hover.stax.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.requests.Request;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.Utils;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestsViewHolder> {
	private Context context;
	private List<Request> requestList;
	private final SelectListener selectListener;

	public RequestsAdapter(List<Request> requests, SelectListener selectListener, Context c) {
		this.requestList = requests;
		this.selectListener = selectListener;
		context = c;
	}

	@NonNull
	@Override
	public RequestsAdapter.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_list_item, parent, false);
		return new RequestsAdapter.RequestsViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull RequestsAdapter.RequestsViewHolder holder, int position) {
		Request r = requestList.get(position);
		holder.description.setText(r.getDescription(context));
		holder.amount.setText(r.amount != null ? Utils.formatAmount(r.amount) : "none");
		holder.header.setVisibility(View.VISIBLE);
		holder.header.setText(DateUtils.humanFriendlyDate(r.date_sent));
		holder.itemView.setOnClickListener(view -> {
			selectListener.viewRequestDetail(r.id);
		});
	}

	@Override
	public int getItemCount() {
		return requestList != null ? requestList.size() : 0;
	}

	static class RequestsViewHolder extends RecyclerView.ViewHolder {
		private TextView description, amount, header;

		RequestsViewHolder(@NonNull View itemView) {
			super(itemView);
			description = itemView.findViewById(R.id.trans_content);
			amount = itemView.findViewById(R.id.trans_amount);
			header = itemView.findViewById(R.id.trans_date);
		}
	}

	public interface SelectListener {
		void viewRequestDetail(int id);
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