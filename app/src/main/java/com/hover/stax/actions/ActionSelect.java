package com.hover.stax.actions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.views.CustomDropdownLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class ActionSelect extends LinearLayout implements RadioGroup.OnCheckedChangeListener, Target {
	private static String TAG = "ActionSelect";

	private CustomDropdownLayout input;
	private AutoCompleteTextView dropdownView;
	private TextView radioHeader;
	private RadioGroup isSelfRadio;

	private List<Action> actions;
	private int selectedRecipientId;
	private Action highlightedAction;
	private HighlightListener highlightListener;

	public ActionSelect(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.action_select, this);
		init();
	}
	private void init() {
		input = findViewById(R.id.action_dropdown_input);
		dropdownView = input.findViewById(R.id.dropdownInputTextView);
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
		List<Action> uniqRecipientActions = sort(filteredActions);

		ActionDropdownAdapter actionDropdownAdapter = new ActionDropdownAdapter(uniqRecipientActions, getContext());
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

		clearInputError();
		setDropDownValue(action);
		setRadioValuesIfRequired(action);
	}

	private void clearInputError() {input.setNormal();}
	private void setRadioValuesIfRequired(Action action) {
		List<Action> options = getWhoMeOptions(action.recipientInstitutionId());
		if (options.size() == 1) {
			if (!options.get(0).requiresRecipient())
				input.setInfo(getContext().getString(R.string.self_only_money_warning));
			selectAction(action);
			isSelfRadio.setVisibility(GONE);
			radioHeader.setVisibility(GONE);
		} else
			createRadios(options);
	}
	private void setDropDownValue(Action a) {
		dropdownView.setText(a.toString(), false);
		Picasso.get()
				.load(getContext().getString(R.string.root_url)+ a.to_institution_logo)
				.resize(55,55).into(this);
	}

	public void selectAction(Action a) {
		Log.e(TAG, "selecting action " + a);
		highlightedAction = a;
		if (highlightListener != null) highlightListener.highlightAction(a);
	}

	public void setListener(HighlightListener hl) { highlightListener = hl; }

	private List<Action> getWhoMeOptions(int recipientInstId) {
		List<Action> options = new ArrayList<>();
		if (actions == null) return options;
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

	@Override
	public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
		RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create (getContext().getResources(), bitmap);
		d.setCircular(true);
		dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
	}

	@Override
	public void onBitmapFailed(Exception e, Drawable errorDrawable) {
		Log.e("LogTag", e.getMessage());
	}

	@Override
	public void onPrepareLoad(Drawable placeHolderDrawable) {
		dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0);
	}

	public interface HighlightListener {
		void highlightAction(Action a);
	}
}
