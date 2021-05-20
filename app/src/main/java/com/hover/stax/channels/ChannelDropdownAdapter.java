package com.hover.stax.channels;

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

import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.hover.stax.utils.Constants.size55;

public class ChannelDropdownAdapter extends ArrayAdapter<Channel> {
    private List<Channel> channels;
    private ViewHolder holder;
    private StaxSpinnerItemWithLogoBinding binding;

    public ChannelDropdownAdapter(@NonNull List<Channel> channelList, @NonNull Context context) {
        super(context, 0, channelList);
        channels = channelList;
    }

    public static List<Channel> sort(List<Channel> channels, boolean showSelected) {
        ArrayList<Channel> selected_list = new ArrayList<>();
        ArrayList<Channel> sorted_list = new ArrayList<>();
        for (Channel c : channels) {
            if (c.selected) selected_list.add(c);
            else sorted_list.add(c);
        }
        Collections.sort(selected_list);
        Collections.sort(sorted_list);
        if (showSelected)
            sorted_list.addAll(0, selected_list);
        return sorted_list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        Channel c = channels.get(position);
        Log.e("ADAPTER", "getting view for pos " + position);

        if (view == null) {
            binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            view = binding.getRoot();
            holder = new ViewHolder(binding);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.setChannel(c);
        updateDivider(c, position);

        return view;
    }

    private void updateDivider(Channel currentChannel, int pos) {
        if (pos > 0) {
            Channel prevChannel = channels.get(pos - 1);
            if (!currentChannel.selected && prevChannel.selected) addDivider();
            else removeDivider();
        } else removeDivider();
    }

    private void addDivider() {
        holder.divider.setVisibility(View.VISIBLE);
    }

    private void removeDivider() {
        holder.divider.setVisibility(View.GONE);
    }

    private static class ViewHolder implements Target {
        TextView id;
        ImageView logo;
        AppCompatTextView channelText;
        View divider;

        private ViewHolder(StaxSpinnerItemWithLogoBinding withLogoBinding) {
            logo = withLogoBinding.serviceItemImageId;
            channelText = withLogoBinding.serviceItemNameId;
            id = withLogoBinding.serviceItemId;
            divider = withLogoBinding.serviceItemDivider;
        }

        @SuppressLint("SetTextI18n")
        private void setChannel(Channel channel) {
            id.setText(Integer.toString(channel.id));
            channelText.setText(channel.toString());
            UIHelper.loadPicasso(channel.logoUrl, size55, this);
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
        return channels.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public Channel getItem(int position) {
        return channels.isEmpty() ? null : channels.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
