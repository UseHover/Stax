package com.hover.stax.bounties;

import android.content.Context;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.hover.stax.databinding.HomeListItemBinding;

class BountyListItem extends LinearLayout {
    private Bounty bounty;
    private SelectListener selectListener;

    private final HomeListItemBinding binding;

    BountyListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        binding = HomeListItemBinding.inflate(LayoutInflater.from(context), this, true);
    }

    public void setBounty(Bounty b, SelectListener listener) {
        bounty = b;
        setContent();
        chooseState();
        selectListener = listener;
    }

    private void setContent() {
        binding.liDescription.setText(bounty.generateDescription(getContext()));
        binding.liAmount.setText(getContext().getString(R.string.bounty_amount_with_currency, bounty.action.bounty_amount));
    }

    private void chooseState() {
        if (!bounty.action.bounty_is_open && bounty.transactionCount() > 0) { // Bounty is closed and done by current user
            setState(R.color.muted_green, R.string.done, R.drawable.ic_check, false, null);
        } else if (!bounty.action.bounty_is_open) { // This bounty is closed and done by another user
            setState(R.color.lighter_grey, 0, 0, false, null);
        } else if (bounty.transactionCount() > 0) { // Bounty is open and with a transaction by current user
            setState(R.color.pending_brown, R.string.bounty_pending_short_desc, R.drawable.ic_warning, true,
                    (view) -> selectListener.viewTransactionDetail(bounty.transactions.get(0).uuid));
        } else
            setState(R.color.cardViewColor, 0, 0, true, (view) -> selectListener.viewBountyDetail(bounty));
    }

    private void setState(int color, int noticeString, int noticeIcon, boolean isOpen, View.OnClickListener listener) {
        setBackgroundColor(getContext().getResources().getColor(color));
        if (noticeString != 0) binding.liCallout.setText(noticeString);
        binding.liCallout.setCompoundDrawablesWithIntrinsicBounds(noticeIcon, 0, 0, 0);
        binding.liCallout.setVisibility(noticeString != 0 ? View.VISIBLE : View.GONE);
        if(!isOpen) strikeThrough(binding.liAmount);
        if(!isOpen)strikeThrough(binding.liDescription);
        setOnClickListener(listener);
    }

    private void strikeThrough(TextView textView) {
        textView.setText(textView.getText(), TextView.BufferType.SPANNABLE);
        Spannable spannable = (Spannable) textView.getText();
        spannable.setSpan(new StrikethroughSpan(), 0, textView.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    public interface SelectListener {
        void viewTransactionDetail(String uuid);

        void viewBountyDetail(Bounty b);
    }
}
