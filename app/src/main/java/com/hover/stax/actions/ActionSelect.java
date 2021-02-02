package com.hover.stax.actions;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.channels.ChannelDropdown;

import java.util.ArrayList;
import java.util.List;

public class ActionSelect extends LinearLayout implements RadioGroup.OnCheckedChangeListener {
	private static String TAG = "ActionSelect";

	private TextInputLayout input;
	private AutoCompleteTextView textView;
	private RadioGroup isSelfRadio;

	private List<Action> actions;
	private List<Action> uniqRecipientActions;
	private int selectedRecipientId;
	private Action highlightedAction;
	private HighlightListener highlightListener;

	public ActionSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.action_select, this);
		input = findViewById(R.id.action_dropdown_input);
		textView = findViewById(R.id.action_autoComplete);
		isSelfRadio = findViewById(R.id.isSelfRadioGroup);
		isSelfRadio.setVisibility(VISIBLE);
		this.setVisibility(GONE);
	}

	public void updateActions(List<Action> filteredActions) {
		Log.e(TAG, "Updating to " + filteredActions);
		actions = filteredActions;
		highlightedAction = null;
		uniqRecipientActions = sort(filteredActions);

		ArrayAdapter actionDropdownAdapter = new ArrayAdapter<>(getContext(), R.layout.stax_spinner_item, uniqRecipientActions);
		textView.setAdapter(actionDropdownAdapter);
		textView.setOnItemClickListener((adapterView, view2, pos, id) -> selectRecipientNetwork((Action) adapterView.getItemAtPosition(pos)));
		Log.e(TAG, "uniq recipient networks " + uniqRecipientActions.size());
		input.setVisibility(uniqRecipientActions.size() <= 1 ? GONE: VISIBLE);
		if (uniqRecipientActions.size() == 1)
			selectRecipientNetwork(uniqRecipientActions.get(0));
	}

	public static List<Action> sort(List<Action> actions) {
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

	public void selectRecipientNetwork(Action action) {
		List<Action> options = getWhoMeOptions(action.recipientInstitutionId());
		if (options.size() == 1) {
			selectAction(options.get(0));
		} else createRadios(options);
	}

	public void selectAction(Action a) {
		Log.e(TAG, "selecting action " + a);
		highlightedAction = a;
		textView.setText(a.toString(), false);
		if (highlightListener != null) highlightListener.highlightAction(a);
	}

	public void setListener(HighlightListener hl) { highlightListener = hl; }

	private List<Action> getWhoMeOptions(int recipientInstId) {
		List<Action> options = new ArrayList<>();
		for (Action a: actions) {
			if (a.recipientInstitutionId() == recipientInstId && !options.contains(a))
				options.add(a);
		}
		return options;
	}

	private void createRadios(List<Action> actions) {
		Log.e(TAG, "creating radios. " + actions.size());
		isSelfRadio.removeAllViews();
		isSelfRadio.clearCheck();
		for (int i = 0; i < actions.size(); i++){
			Action a =  actions.get(i);
			RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
			radioButton.setText(a.getPronoun(getContext()));
			radioButton.setId(i);
			isSelfRadio.addView(radioButton);
		}
		isSelfRadio.setOnCheckedChangeListener(this);
		isSelfRadio.check(highlightedAction != null ? actions.indexOf(highlightedAction) : 0);
		isSelfRadio.setVisibility(actions.size() > 1 ? VISIBLE : GONE);
		findViewById(R.id.header).setVisibility(actions.size() > 1 ? VISIBLE : GONE);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (checkedId == -1) return;
		selectAction(actions.get(checkedId));
	}

	public void setError(String message) {
		input.setError(message);
	}

	public interface HighlightListener {
		void highlightAction(Action a);
	}
}
