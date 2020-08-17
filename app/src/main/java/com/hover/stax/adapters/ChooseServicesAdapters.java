package com.hover.stax.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.enums.Service_category;
import com.hover.stax.enums.Service_in_list_status;
import com.hover.stax.interfaces.CustomOnClickListener;
import com.hover.stax.models.StaxServiceModel;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChooseServicesAdapters  extends RecyclerView.Adapter<ChooseServicesAdapters.ChooseServicesViewHolder> {
	private ArrayList<StaxServiceModel> servicesModelArrayList;
	private CustomOnClickListener customOnClickListener;
	private Service_category service_category;
	private int textColorAdded, textColorNotAdded;

public ChooseServicesAdapters(ArrayList<StaxServiceModel> servicesModelArrayList, CustomOnClickListener customOnClickListener, Service_category service_category, int colorAdded, int colorNotAdded) {
	this.servicesModelArrayList = servicesModelArrayList;
	this.customOnClickListener = customOnClickListener;
	this.service_category = service_category;
	this.textColorAdded = colorAdded;
	this.textColorNotAdded  = colorNotAdded;
}

public void updateSelectStatus(Service_in_list_status status, int position) {
if(status == Service_in_list_status.ADD_TO_LIST) servicesModelArrayList.get(position).setAdded(true);
else servicesModelArrayList.get(position).setAdded(false);

notifyItemChanged(position);
}

public void resetSelectStatus(int position) {
	boolean currentStatus = servicesModelArrayList.get(position).getAdded();
	servicesModelArrayList.get(position).setAdded(!currentStatus);
	notifyItemChanged(position);
}

@NonNull
@Override
public ChooseServicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
	View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.choose_service_item, parent, false);
	return new ChooseServicesViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull ChooseServicesViewHolder holder, int position) {
	StaxServiceModel staxServiceModel = servicesModelArrayList.get(position);
	//holder.serviceLogo.setImageBitmap(staxServicesModel.getServiceLogo());
	holder.serviceName.setText(staxServiceModel.getServiceName());

	if(staxServiceModel.getAdded()) {
	holder.shadowFrame.setVisibility(View.VISIBLE);
	holder.checkIcon.setVisibility(View.VISIBLE);
	holder.serviceName.setTextColor(textColorAdded);
	}
	else {
		holder.shadowFrame.setVisibility(View.GONE);
		holder.checkIcon.setVisibility(View.GONE);
		holder.serviceName.setTextColor(textColorNotAdded);
	}

	holder.itemView.setOnClickListener(view-> {
		customOnClickListener.customClickListener(
				staxServiceModel.getServiceId(),
				(staxServiceModel.getAdded()) ? Service_in_list_status.REMOVE_FROM_LIST : Service_in_list_status.ADD_TO_LIST,
				position,
				service_category);
	});
}

@Override
public int getItemCount() {
	if(servicesModelArrayList !=null) return servicesModelArrayList.size();
	else return 0;
}

@Override
public long getItemId(int position) {
	return position;
}

@Override
public int getItemViewType(int position) {
	return position;
}

static class ChooseServicesViewHolder extends RecyclerView.ViewHolder {
		CircleImageView serviceLogo, shadowFrame;
		ImageView checkIcon;
		TextView serviceName;
		ChooseServicesViewHolder(@NonNull View itemView) {
			super(itemView);
			serviceLogo = itemView.findViewById(R.id.service_item_image_id);
			shadowFrame = itemView.findViewById(R.id.checked_service_frame);
			checkIcon = itemView.findViewById(R.id.checked_service_tick);
			serviceName = itemView.findViewById(R.id.service_item_name_id);

		}
	}
}
