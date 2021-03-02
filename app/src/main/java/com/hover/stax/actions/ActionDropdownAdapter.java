package com.hover.stax.actions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.hover.stax.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;


public class ActionDropdownAdapter extends ArrayAdapter<Action> {
	private List<Action> actions;


	public ActionDropdownAdapter(@NonNull List<Action> actions, @NonNull Context context) {
		super(context, 0, actions);
		this.actions = actions;

	}


	@NonNull
	@Override
	public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
		Action a = actions.get(position);
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stax_spinner_item_with_logo, parent,false);

		ViewHolder holder = new ViewHolder(view);
		holder.setAction(a, getContext().getString(R.string.root_url));
		return view;
	}


	private static class ViewHolder implements Target {
		TextView id;
		ImageView logo;
		AppCompatTextView channelText;
		View divider;

		private ViewHolder(View view) {
			logo = view.findViewById(R.id.service_item_image_id);
			channelText = view.findViewById(R.id.service_item_name_id);
			id = view.findViewById(R.id.service_item_id);
			divider = view.findViewById(R.id.service_item_divider);
			divider.setVisibility(View.GONE);
		}

		@SuppressLint("SetTextI18n")
		private void setAction(Action action, String baseUrl) {
			id.setText(Integer.toString(action.id));
			channelText.setText(action.toString());
			Picasso.get().load(baseUrl+action.to_institution_logo).networkPolicy(NetworkPolicy.OFFLINE).into(this);
		}

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(id.getContext().getResources(), bitmap);
			d.setCircular(true);
			logo.setImageDrawable(d);
		}

		@Override
		public void onBitmapFailed(Exception e, Drawable errorDrawable) {
			Log.e("LogTag", e.getMessage());
		}

		@Override public void onPrepareLoad(Drawable placeHolderDrawable) { }
	}

	@Override
	public int getCount() { return actions.size(); }

	@Override
	public long getItemId(int position) { return position; }

	@Override
	public int getItemViewType(int position) {
		return position;
	}
}
