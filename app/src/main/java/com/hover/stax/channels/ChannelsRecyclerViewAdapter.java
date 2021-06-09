package com.hover.stax.channels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.databinding.StaxSpinnerItemWithLogoBinding;
import com.hover.stax.utils.UIHelper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import timber.log.Timber;

import static android.view.View.GONE;
import static com.hover.stax.utils.Constants.size55;

public class ChannelsRecyclerViewAdapter extends RecyclerView.Adapter<ChannelsRecyclerViewAdapter.ChannelsViewHolder> {
    private final List<Channel> channels;
    private final SelectListener selectListener;

    public ChannelsRecyclerViewAdapter(@NonNull List<Channel> channelList, SelectListener selectListener) {
        this.channels = channelList;
        this.selectListener = selectListener;
    }

    public static class ChannelsViewHolder extends RecyclerView.ViewHolder implements Target {
        TextView id;
        ImageView logo;
        AppCompatTextView channelText;
        View divider;

        private ChannelsViewHolder(StaxSpinnerItemWithLogoBinding withLogoBinding) {
            super(withLogoBinding.getRoot());
            logo = withLogoBinding.serviceItemImageId;
            channelText = withLogoBinding.serviceItemNameId;
            id = withLogoBinding.serviceItemId;
            divider = withLogoBinding.serviceItemDivider;
            divider.setVisibility(GONE);
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

    @NonNull
    @NotNull
    @Override
    public ChannelsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        StaxSpinnerItemWithLogoBinding binding = StaxSpinnerItemWithLogoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChannelsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ChannelsRecyclerViewAdapter.ChannelsViewHolder holder, int position) {
        Channel c = channels.get(position);
        holder.itemView.setTag(holder);
        holder.setChannel(c);
        holder.itemView.setOnClickListener(view -> selectListener.clickedChannel(c));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }
    public interface SelectListener {
        void clickedChannel(Channel channel);
    }
}
