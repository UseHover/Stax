package com.hover.stax.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.databinding.Stax2lineitemBinding;

public class Stax2LineItem extends RelativeLayout {

    private final Stax2lineitemBinding binding;

    public Stax2LineItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        binding = Stax2lineitemBinding.inflate(LayoutInflater.from(context), this, true);

        binding.title.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        binding.subtitle.setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
        binding.subtitle.setTextColor(getResources().getColor(R.color.offWhite));
    }

    public void setContent(String title, String sub) {
        setTitle(title);
        setSubtitle(sub);
    }

    public void setTitle(String title) {
        if(title != null) binding.title.setText(title);
    }

    public void setSubtitle(String sub) {
        if(sub != null) binding.subtitle.setText(sub);
    }

    public void setContact(StaxContact contact) {
        if (contact == null) return;
        setTitle(contact.shortName());
        if (!contact.shortName().equals(contact.getPhoneNumber()))
            setSubtitle(contact.getPhoneNumber());
    }
}
