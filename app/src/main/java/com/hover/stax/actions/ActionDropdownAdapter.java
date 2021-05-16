package com.hover.stax.actions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

import timber.log.Timber;

import static com.hover.stax.utils.Constants.size55;


public class ActionDropdownAdapter extends ArrayAdapter<HoverAction> {
    private final List<HoverAction> actions;

    public ActionDropdownAdapter(@NonNull List<HoverAction> actions, @NonNull Context context) {
        super(context, 0, actions);
        this.actions = actions;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        HoverAction a = actions.get(position);
        ViewHolder holder;

        if (view == null) {
            StaxSpinnerItemWithLogoBinding binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            view = binding.getRoot();

            holder = new ViewHolder(binding);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.setAction(a, getContext().getString(R.string.root_url));

        return view;
    }


    private static class ViewHolder implements Target {
        TextView id;
        ImageView logo;
        AppCompatTextView channelText;
        View divider;

        StaxSpinnerItemWithLogoBinding binding;

        private ViewHolder(StaxSpinnerItemWithLogoBinding binding) {
            this.binding = binding;

            logo = binding.serviceItemImageId;
            channelText = binding.serviceItemNameId;
            id = binding.serviceItemId;
            divider = binding.serviceItemDivider;
            divider.setVisibility(View.GONE);
        }

        @SuppressLint("SetTextI18n")
        private void setAction(HoverAction action, String baseUrl) {
            id.setText(Integer.toString(action.id));
            channelText.setText(action.toString());
            UIHelper.loadPicasso(baseUrl + action.to_institution_logo, size55, this);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(id.getContext().getResources(), bitmap);
            d.setCircular(true);
            logo.setImageDrawable(d);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            Timber.e(e);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    @Override
    public int getCount() {
        return actions.size();
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
