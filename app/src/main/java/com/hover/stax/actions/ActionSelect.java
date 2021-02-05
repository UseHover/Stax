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
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

public class ActionSelect extends LinearLayout implements RadioGroup.OnCheckedChangeListener {
	private static String TAG = "ActionSelect";

	private TextInputLayout input;
	private AutoCompleteTextView dropdownView;
	private TextView radioHeader;
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
		dropdownView = findViewById(R.id.action_autoComplete);
		radioHeader = findViewById(R.id.header);
		isSelfRadio = findViewById(R.id.isSelfRadioGroup);
		this.setVisibility(GONE);
	}

	public void updateActions(List<Action> filteredActions) {
		Log.e(TAG, "Updating to " + filteredActions);
		this.setVisibility(filteredActions == null || filteredActions.size() <= 0 ? View.GONE : View.VISIBLE);
		if (filteredActions == null || filteredActions.size() <= 0) return;

		actions = filteredActions;
		highlightedAction = null;
		uniqRecipientActions = sort(filteredActions);

		ArrayAdapter actionDropdownAdapter = new ArrayAdapter<>(getContext(), R.layout.stax_spinner_item, uniqRecipientActions);
		dropdownView.setAdapter(actionDropdownAdapter);
		dropdownView.setOnItemClickListener((adapterView, view2, pos, id) -> selectRecipientNetwork((Action) adapterView.getItemAtPosition(pos)));
		Log.e(TAG, "uniq recipient networks " + uniqRecipientActions.size());
		input.setVisibility(showRecipientNetwork(uniqRecipientActions) ? VISIBLE : GONE);
		radioHeader.setText(actions.get(0).transaction_type.equals(Action.AIRTIME) ? R.string.airtime_who_header : R.string.send_who_header);
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

	private boolean showRecipientNetwork(List<Action> actions) {
		return actions.size() > 1 || (actions.size() == 1 && !actions.get(0).isOnNetwork());
	}

	public void selectRecipientNetwork(Action action) {
		if (action.equals(highlightedAction)) return;
		input.setHelperText(null);
		dropdownView.setText(action.toString(), false);
		List<Action> options = getWhoMeOptions(action.recipientInstitutionId());
		if (options.size() == 1) {
			if (!options.get(0).requiresRecipient())
				input.setHelperText(getContext().getString(R.string.self_only_money_warning));
			selectAction(action);
			isSelfRadio.setVisibility(GONE);
			radioHeader.setVisibility(GONE);
		} else
			createRadios(options);
	}

	public void selectAction(Action a) {
		Log.e(TAG, "selecting action " + a);
		highlightedAction = a;
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
		radioHeader.setVisibility(actions.size() > 1 ? VISIBLE : GONE);
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
