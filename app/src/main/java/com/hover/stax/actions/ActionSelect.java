package com.hover.stax.actions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

public class ActionSelect extends LinearLayout {

	private TextInputLayout input;
	private AutoCompleteTextView textView;
	private RadioGroup isSelfRadio;

	private List<Action> actions;
	private List<Action> uniqRecipientActions;
	private int selectedRecipientId;
	private Action selectedAction;

	public ActionSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.action_select, this);
		input = findViewById(R.id.action_dropdown_input);
		textView = findViewById(R.id.action_autoComplete);
		isSelfRadio = findViewById(R.id.isSelfRadioGroup);

		isSelfRadio.setVisibility(GONE);
	}

	public void updateActions(List<Action> filteredActions) {
		actions = filteredActions;
		uniqRecipientActions = sort(filteredActions);

		ArrayAdapter actionDropdownAdapter = new ArrayAdapter<Action>(getContext(), R.layout.stax_spinner_item);
		textView.setAdapter(actionDropdownAdapter);
//		textView.setText(textView.getAdapter().getItem(0).toString(), false);
		textView.setOnItemClickListener((adapterView, view2, pos, id) -> onSelectRecipientNetwork((Action) adapterView.getItemAtPosition(pos)));
	}

	public List<Action> sort(List<Action> actions) {
		ArrayList<Integer> uniqRecipInstIds = new ArrayList<>();
		ArrayList<Action> uniqRecipActions = new ArrayList<>();
		for (Action a : actions) {
			if (!uniqRecipInstIds.contains(a.recipientInstitutionId())) {
				uniqRecipInstIds.add(a.recipientInstitutionId());
				uniqRecipActions.add(a);
			}
		}
		return uniqRecipActions;
	}

	private void onSelectRecipientNetwork(Action action) {
		List<Action> options = getWhoMeOptions(action.recipientInstitutionId());
		if (options.size() == 1)
			selectedAction = action;
		else
			createRadios(options);
	}

	private List<Action> getWhoMeOptions(int recipientInstId) {
		List<Action> options = new ArrayList<>();
		for (Action a: actions) {
			if (a.recipientInstitutionId() == recipientInstId && !options.contains(a))
				options.add(a);
		}
		return options;
	}

	private void createRadios(List<Action> actions) {
		isSelfRadio.removeAllViews();
		for (int i = 0; i < actions.size(); i++){
			Action a =  actions.get(i);
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(a.getPronoun(getContext()));
			radioButton.setId(i);
			radioButton.setChecked(i == 0);
			isSelfRadio.addView(radioButton);
		}
		isSelfRadio.setVisibility(VISIBLE);
	}
}
