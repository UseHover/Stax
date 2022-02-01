package com.hover.stax.channels;

import static com.hover.stax.utils.Constants.size55;

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

import java.util.List;

import timber.log.Timber;

public class ChannelsDropdownAdapter extends ArrayAdapter<Channel> {
    private final List<Channel> channels;
    private ViewHolder holder;
    private StaxSpinnerItemWithLogoBinding binding;

    public ChannelsDropdownAdapter(@NonNull List<Channel> channelList, @NonNull Context context) {
        super(context, 0, channelList);
        channels = channelList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        Channel c = channels.get(position);

        if (view == null) {
            binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            view = binding.getRoot();
            holder = new ViewHolder(binding);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.setChannel(c);

        return view;
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

    public static class ViewHolder implements Target {
        TextView id;
        ImageView logo;
        AppCompatTextView channelText;

        private ViewHolder(StaxSpinnerItemWithLogoBinding withLogoBinding) {
            logo = withLogoBinding.serviceItemImageId;
            channelText = withLogoBinding.serviceItemNameId;
            id = withLogoBinding.serviceItemId;
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
}
