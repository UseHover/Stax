package com.hover.stax.contacts;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxDropdownLayout;

import java.util.List;

public class ContactInput extends LinearLayout {

	private ImageButton contactButton;
	private StaxDropdownLayout contactInputLayout;
	private AutoCompleteTextView contactAutocomplete;

	public ContactInput(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.contact_input, this);
		contactButton = findViewById(R.id.contact_button);
		contactInputLayout = findViewById(R.id.contactDropdownLayout);
		contactAutocomplete = findViewById(R.id.autoCompleteView);
	}

	public void setRecent(List<StaxContact> contacts, Context c) {
		ArrayAdapter<StaxContact> adapter = new StaxContactArrayAdapter(c, contacts);
		contactAutocomplete.setAdapter(adapter);
	}

	public void setSelected(StaxContact contact) {
		if (contact != null) setText(contact.toString());
	}

	public void setText(String number) {
		if (number != null && !number.isEmpty()) {
			setState(null, AbstractStatefulInput.SUCCESS);
			contactAutocomplete.setText(number);
		}
	}

	public void setHint(String hint) { contactInputLayout.setHint(hint); }

	public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
		contactAutocomplete.setOnItemClickListener(listener);
	}

	public void setChooseContactListener(OnClickListener listener) {
		contactButton.setOnClickListener(listener);
	}

	public void addTextChangedListener(TextWatcher listener) {
		contactAutocomplete.addTextChangedListener(listener);
	}

	public void setState(String message, int state) { contactInputLayout.setState(message, state); }

	public void resetState() { setState(null, AbstractStatefulInput.NONE); }
}
