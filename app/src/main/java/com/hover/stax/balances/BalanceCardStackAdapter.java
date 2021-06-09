package com.hover.stax.balances;

import android.content.Context;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.StackBalanceCardBinding;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.staxcardstack.StaxCardStackAdapter;
import com.hover.stax.views.staxcardstack.StaxCardStackView;

class BalanceCardStackAdapter extends StaxCardStackAdapter<Channel> {

    public BalanceCardStackAdapter(Context context) {
        super(context);
    }

    @Override
    public void bindView(Channel channel, int pos, StaxCardStackView.ViewHolder holder) {
        if(holder instanceof MyViewHolder) {
            String hex = channel.primaryColorHex;
            ((MyViewHolder) holder).onBind(hex);
        }
    }

    @Override
    protected StaxCardStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        return new MyViewHolder(StackBalanceCardBinding.inflate(getLayoutInflater(), parent, false));
    }

    static class MyViewHolder extends StaxCardStackView.ViewHolder {
        private final CardView cardView;
        public MyViewHolder(StackBalanceCardBinding binding) {
            super(binding.getRoot());
            cardView = binding.getRoot();
        }
        public void onBind(String hex) {
            cardView.setCardBackgroundColor(UIHelper.getColor(hex, false, getContext()));
        }
    }

}
