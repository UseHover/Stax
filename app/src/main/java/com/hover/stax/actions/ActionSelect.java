package com.hover.stax.actions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.hover.sdk.actions.HoverAction;
import com.hover.stax.R;
import com.hover.stax.databinding.ActionSelectBinding;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxDropdownLayout;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.hover.stax.utils.Constants.size55;

public class ActionSelect extends LinearLayout implements RadioGroup.OnCheckedChangeListener, Target {
    private static String TAG = "ActionSelect";

    private StaxDropdownLayout dropdownLayout;
    private AutoCompleteTextView dropdownView;
    private TextView radioHeader;
    private RadioGroup isSelfRadio;

    private List<HoverAction> actions;
    private HoverAction highlightedAction;
    private HighlightListener highlightListener;

    private ActionSelectBinding binding;

    public ActionSelect(Context context, AttributeSet attrs) {
        super(context, attrs);

        binding = ActionSelectBinding.inflate(LayoutInflater.from(context), this, true);

        init();
    }

    private void init() {
        dropdownLayout = binding.actionDropdownInput;
        dropdownView = binding.actionDropdownInput.autoCompleteTextView;
        radioHeader = binding.header;
        isSelfRadio = binding.isSelfRadioGroup;
        this.setVisibility(GONE);
    }

    public void updateActions(List<HoverAction> filteredActions) {
        Timber.e("Updating to %s", filteredActions);
        this.setVisibility(filteredActions == null || filteredActions.size() <= 0 ? View.GONE : View.VISIBLE);
        if (filteredActions == null || filteredActions.size() <= 0) return;

        actions = filteredActions;
        highlightedAction = null;
        List<HoverAction> uniqRecipientActions = sort(filteredActions);

        ActionDropdownAdapter actionDropdownAdapter = new ActionDropdownAdapter(uniqRecipientActions, getContext());
        dropdownView.setAdapter(actionDropdownAdapter);
        dropdownView.setOnItemClickListener((adapterView, view2, pos, id) -> selectRecipientNetwork((HoverAction) adapterView.getItemAtPosition(pos)));
        Timber.e("uniq recipient networks %s", uniqRecipientActions.size());
        dropdownLayout.setVisibility(showRecipientNetwork(uniqRecipientActions) ? VISIBLE : GONE);
        Timber.e(actions.get(0).transaction_type);

        radioHeader.setText(actions.get(0).transaction_type.equals(HoverAction.AIRTIME) ? R.string.airtime_who_header : R.string.send_who_header);
    }

    public static List<HoverAction> sort(List<HoverAction> actions) {
        ArrayList<Integer> uniqRecipInstIds = new ArrayList<>();
        ArrayList<HoverAction> uniqRecipActions = new ArrayList<>();
        for (HoverAction a : actions) {
            if (!uniqRecipInstIds.contains(a.recipientInstitutionId())) {
                uniqRecipInstIds.add(a.recipientInstitutionId());
                uniqRecipActions.add(a);
            }
        }
        return uniqRecipActions;
    }

    private boolean showRecipientNetwork(List<HoverAction> actions) {
        return actions.size() > 1 || (actions.size() == 1 && !actions.get(0).isOnNetwork());
    }

    public void selectRecipientNetwork(HoverAction action) {
        if (action.equals(highlightedAction)) return;

        setDropDownValue(action);
        setRadioValuesIfRequired(action);
    }

    private void setRadioValuesIfRequired(HoverAction action) {
        dropdownLayout.setState(null, AbstractStatefulInput.SUCCESS);
        List<HoverAction> options = getWhoMeOptions(action.recipientInstitutionId());
        if (options.size() == 1) {
            if (!options.get(0).requiresRecipient())
                dropdownLayout.setState(getContext().getString(R.string.self_only_money_warning), AbstractStatefulInput.INFO);
            selectAction(action);
            isSelfRadio.setVisibility(GONE);
            radioHeader.setVisibility(GONE);
        } else
            createRadios(options);
    }

    private void setDropDownValue(HoverAction a) {
        dropdownView.setText(a.toString(), false);
        UIHelper.loadPicasso(getContext().getString(R.string.root_url) + a.to_institution_logo, size55, this);
    }

    public void selectAction(HoverAction a) {
        Timber.e("selecting action %s", a);
        highlightedAction = a;
        if (highlightListener != null) highlightListener.highlightAction(a);
    }

    public void setListener(HighlightListener hl) {
        highlightListener = hl;
    }

    public void setState(String message, int state) {
        dropdownLayout.setState(message, state);
    }

    private List<HoverAction> getWhoMeOptions(int recipientInstId) {
        List<HoverAction> options = new ArrayList<>();
        if (actions == null) return options;
        for (HoverAction a : actions) {
            if (a.recipientInstitutionId() == recipientInstId && !options.contains(a))
                options.add(a);
        }
        return options;
    }

    private void createRadios(List<HoverAction> actions) {
        isSelfRadio.removeAllViews();
        isSelfRadio.clearCheck();

        if(!actions.isEmpty()) {
            for (int i = 0; i < actions.size(); i++) {
                HoverAction a = actions.get(i);
                RadioButton radioButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.stax_radio_button, null);
                radioButton.setText(a.getPronoun(getContext()));
                radioButton.setId(i);
                isSelfRadio.addView(radioButton);
            }
        }

        isSelfRadio.setOnCheckedChangeListener(this);
        isSelfRadio.check(highlightedAction != null ? actions.indexOf(highlightedAction) : 0);
        isSelfRadio.setVisibility(actions.size() > 1 ? VISIBLE : GONE);
        radioHeader.setVisibility(actions.size() > 1 ? VISIBLE : GONE);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == -1 && actions != null) return;
        selectAction(actions.get(checkedId));
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getContext().getResources(), bitmap);
        d.setCircular(true);
        dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(d, null, null, null);
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        Timber.e(e.getLocalizedMessage());
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        dropdownView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_grey_circle_small, 0, 0, 0);
    }

    public interface HighlightListener {
        void highlightAction(HoverAction a);
    }
}
