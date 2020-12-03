package com.hover.stax.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hover.stax.R;
import com.hover.stax.contacts.StaxContact;

public class Stax2LineItem extends RelativeLayout {
	private RelativeLayout contentView;

	public Stax2LineItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.stax_2lineitem, this);
		contentView = findViewById(R.id.content);
		((TextView) contentView.findViewById(R.id.title)).setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
		((TextView) contentView.findViewById(R.id.subtitle)).setTextAlignment(TEXT_ALIGNMENT_TEXT_END);
		((TextView) contentView.findViewById(R.id.subtitle)).setTextColor(getResources().getColor(R.color.offWhite));
	}

	public void setContent(String title, String sub) {
		setTitle(title);
		setSubtitle(sub);
	}

	public void setTitle(String title) {
		((TextView) contentView.findViewById(R.id.title)).setText(title);
	}

	public void setSubtitle(String sub) {
		((TextView) contentView.findViewById(R.id.subtitle)).setText(sub);
	}

	public void setContact(StaxContact contact) {
		setTitle(contact.shortName());
		if (!contact.shortName().equals(contact.phoneNumber))
			setSubtitle(contact.phoneNumber);
	}
}
