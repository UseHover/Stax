package com.hover.stax.contacts;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hover.stax.R;
import com.hover.stax.databinding.ContactInputBinding;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxDropdownLayout;

import java.util.List;

public class ContactInput extends LinearLayout {

    private final ImageButton contactButton;
    private final StaxDropdownLayout contactInputLayout;
    private final AutoCompleteTextView contactAutocomplete;

    public ContactInput(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ContactInputBinding binding = ContactInputBinding.inflate(LayoutInflater.from(context), this, true);

        contactButton = binding.contactButton;
        contactInputLayout = binding.contactDropdownLayout;
        contactAutocomplete = binding.contactDropdownLayout.findViewById(R.id.autoCompleteView);
        contactAutocomplete.setOnFocusChangeListener(this::setState);

        contactAutocomplete.setOnClickListener(v -> Utils.showSoftKeyboard(context, v));
        contactAutocomplete.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    public void setRecent(List<StaxContact> contacts, Context c) {
        ArrayAdapter<StaxContact> adapter = new StaxContactArrayAdapter(c, contacts);
        contactAutocomplete.setAdapter(adapter);
    }

    public void setSelected(StaxContact contact) {
        if (contact != null) setText(contact.toString(), false);
    }

    public void setText(String number, boolean filter) {
        if (number != null && !number.isEmpty())
            setState(null, AbstractStatefulInput.SUCCESS);
        contactAutocomplete.setText(number, filter);
    }

    public void setHint(String hint) {
        contactInputLayout.setHint(hint);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        contactAutocomplete.setOnItemClickListener(listener);
    }

    public void setChooseContactListener(OnClickListener listener) {
        contactButton.setOnClickListener(listener);
    }

    public void addTextChangedListener(TextWatcher listener) {
        contactAutocomplete.addTextChangedListener(listener);
    }

    public void setState(String message, int state) {
        contactInputLayout.setState(message, state);
    }

    private void setState(View v, boolean hasFocus) {
        if (!hasFocus)
            contactInputLayout.setState(null,
                    contactAutocomplete.getText() != null && contactAutocomplete.getText().toString() != null && !contactAutocomplete.getText().toString().isEmpty() ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.NONE);
    }
}
