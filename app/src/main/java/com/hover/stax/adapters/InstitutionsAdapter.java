package com.hover.stax.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.sdk.sims.SimInfo;
import com.hover.stax.R;
import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.institutions.Institution;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InstitutionsAdapter extends RecyclerView.Adapter<InstitutionsAdapter.InstitutionViewHolder> {
	private List<Institution> institutions;
	private List<Integer> selected;

	private final SelectListener selectListener;

	public InstitutionsAdapter(List<Institution> institutionList, SelectListener listener) {
		this.institutions = institutionList;
		this.selectListener = listener;
		selected = new ArrayList<>();
	}

	public void updateSelected(List<Integer> ids) {
		selected = ids;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public InstitutionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_service_item, parent, false);
		return new InstitutionViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull InstitutionViewHolder holder, int position) {
		Institution institution = institutions.get(position);

		holder.id.setText(Integer.toString(institution.id));
		holder.name.setText(institution.name);
		//holder.serviceLogo.setImageBitmap(institution.logo);
		holder.shadowFrame.setVisibility(selected.contains(institution.id) ? View.VISIBLE : View.GONE);
		holder.checkIcon.setVisibility(selected.contains(institution.id) ? View.VISIBLE : View.GONE);
	}

	class InstitutionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView name, id;
		CircleImageView logo, shadowFrame;
		ImageView checkIcon;

		InstitutionViewHolder(@NonNull View itemView) {
			super(itemView);
			id = itemView.findViewById(R.id.service_item_id);
			name = itemView.findViewById(R.id.service_item_name_id);
			logo = itemView.findViewById(R.id.service_item_image_id);
			shadowFrame = itemView.findViewById(R.id.checked_service_frame);
			checkIcon = itemView.findViewById(R.id.checked_service_tick);

			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			selectListener.onTap(Integer.parseInt(id.getText().toString()));
		}
	}

	public interface SelectListener  {
		void onTap(int institutionId);
	}

	@Override
	public int getItemCount() {
		if (institutions != null) return institutions.size();
		else return 0;
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public int getItemViewType(int position) {
		return position;
	}
}
