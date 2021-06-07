package com.hover.stax.balances;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.BalanceItemBinding;
import com.hover.stax.utils.DateUtils;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import static android.view.View.GONE;

public class BalanceAdapter extends RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder> {
    private final static String TAG = "BalanceAdapter";

    private final List<Channel> channels;

    private final BalanceListener balanceListener;
    private boolean showBalance = false;

    public BalanceAdapter(List<Channel> channels, BalanceListener listener) {
        this.channels = channels;
        this.balanceListener = listener;
    }

    public void showBalanceAmounts(boolean show) {
        this.showBalance = show;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BalanceItemBinding binding = BalanceItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BalanceViewHolder(binding);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        Channel channel = channels.get(position);

        UIHelper.setTextUnderline(holder.binding.balanceChannelName,channel.name);
        holder.binding.channelId.setText(String.valueOf(channel.id));

        if (!showBalance)
            holder.binding.balanceSubtitle.setVisibility(GONE);

        if (channel.latestBalance != null && showBalance) {
            holder.binding.balanceSubtitle.setVisibility(View.VISIBLE);
            holder.binding.balanceSubtitle.setText(DateUtils.humanFriendlyDate(channel.latestBalanceTimestamp));
            holder.binding.balanceAmount.setText(Utils.formatAmount(channel.latestBalance));
            setColorForEmptyAmount(false, holder, 0);
        } else {
            holder.binding.balanceAmount.setText("");
            setColorForEmptyAmount(true, holder, UIHelper.getColor(channel.secondaryColorHex, false, holder.itemView.getContext()));
        }
        if (showBalance && channel.latestBalance == null) {
            holder.binding.balanceSubtitle.setVisibility(View.VISIBLE);
            holder.binding.balanceSubtitle.setText(holder.itemView.getContext().getString(R.string.refresh_balance_desc));
        }

        setColors(holder, channel,
                UIHelper.getColor(channel.primaryColorHex, true, holder.itemView.getContext()),
                UIHelper.getColor(channel.secondaryColorHex, false, holder.itemView.getContext()));

        if(channel.id == Channel.DUMMY) {
            holder.binding.balanceSubtitle.setVisibility(GONE);
            holder.binding.balanceRefreshIcon.setImageResource(R.drawable.ic_add_icon_24);
        }
    }

    private void setColors(BalanceViewHolder holder, Channel channel, int primary, int secondary) {
        holder.binding.getRoot().setCardBackgroundColor(primary);
        holder.binding.balanceSubtitle.setTextColor(secondary);
        holder.binding.balanceAmount.setTextColor(secondary);
        holder.binding.balanceChannelName.setTextColor(secondary);
        holder.binding.balanceRefreshIcon.setColorFilter(secondary);
    }

    private void setColorForEmptyAmount(boolean show, BalanceViewHolder holder, int secondary) {
        if (show) {
            Drawable drawable = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_remove);
            if (drawable != null) {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable.mutate(), secondary);
                holder.binding.balanceAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            }
        } else
            holder.binding.balanceAmount.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

    }

    class BalanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public BalanceItemBinding binding;

        public BalanceViewHolder(BalanceItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
            binding.balanceRefreshIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (balanceListener != null ){
                if (binding.balanceRefreshIcon.equals(v)) {
                    balanceListener.onTapRefresh(Integer.parseInt(binding.channelId.getText().toString()));
                } else {
                    balanceListener.onTapDetail(Integer.parseInt(binding.channelId.getText().toString()));
                }
            }
        }
    }

    public interface BalanceListener {
        void onTapRefresh(int channelId);

        void onTapDetail(int channelId);
    }

    @Override
    public int getItemCount() {
        return channels == null ? 0 : channels.size();
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
