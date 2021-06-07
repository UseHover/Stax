package com.hover.stax.balances;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.BalanceDummyCardBinding;
import com.hover.stax.utils.UIHelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;

class BalanceColorCardAdapter extends RecyclerView.Adapter<BalanceColorCardAdapter.ViewHolder> {
    private final List<Channel> channels;

    public BalanceColorCardAdapter(List<Channel> channels) {
        this.channels = channels;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        BalanceDummyCardBinding balanceDummyCardBinding = BalanceDummyCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(balanceDummyCardBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull BalanceColorCardAdapter.ViewHolder holder, int position) {
        String colorHex = channels.get(position).primaryColorHex;
        holder.cardView.setCardBackgroundColor(UIHelper.getColor(colorHex, false, holder.itemView.getContext()));
    }

    @Override
    public int getItemCount() {
        return channels !=null ? channels.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        public ViewHolder(@NonNull @NotNull BalanceDummyCardBinding balanceDummyCardBinding) {
            super(balanceDummyCardBinding.getRoot());
            cardView = balanceDummyCardBinding.getRoot();
        }
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
