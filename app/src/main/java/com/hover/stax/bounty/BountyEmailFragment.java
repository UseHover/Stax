package com.hover.stax.bounty;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxButton;
import com.hover.stax.views.StaxDialog;
import com.hover.stax.views.StaxTextInputLayout;

import java.lang.ref.WeakReference;

import static com.hover.stax.utils.Constants.COUNT_FIVE_SECS;

public class BountyEmailFragment extends Fragment implements NavigationInterface, View.OnClickListener {
	private BountyViewModel bountyViewModel;
	private View view;
	private StaxTextInputLayout emailInput;
	private StaxButton continueButton;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_bounty_enter_email_layout, container, false);
		promptEmailOrNavigateBountyList();
		bountyViewModel = new ViewModelProvider(requireActivity()).get(BountyViewModel.class);
		return view;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initEmailInput();
		initContinueButton();
		uploadBountyUserObserver();
	}

	private void uploadBountyUserObserver() {
		bountyViewModel.getUploadBountyResult().observe(getViewLifecycleOwner(), result-> {
			if(result.equals(Constants.SUCCESS)) {
				emailInput.setState("", AbstractStatefulInput.SUCCESS);
				continueButton.endAnimation();
				promptEmailOrNavigateBountyList();
			}
			else {
				emailInput.setFocusable(true);
				emailInput.setState("", AbstractStatefulInput.NONE);
				showErrorDialog();
			}
		});
	}

	private void initEmailInput() {
		emailInput = view.findViewById(R.id.emailInput);
		emailInput.addTextChangedListener(emailWatcher);
	}

	private void initContinueButton() {
		continueButton = view.findViewById(R.id.continueEmailBountyButton);
		continueButton.setOnClickListener(this);
	}

	private boolean validates() {
		String emailError = bountyViewModel.emailError();
		emailInput.setState(emailError, emailError == null ? AbstractStatefulInput.SUCCESS : AbstractStatefulInput.ERROR);
		return emailError == null;
	}

	private boolean isContinueButton(View v) {
		return v.getId() == R.id.continueEmailBountyButton;
	}

	private void promptEmailOrNavigateBountyList() {
		if (Utils.getBoolean(Constants.BOUNTY_EMAIL, getContext())) navigateToBountyListFragment(this);
		else view.findViewById(R.id.bounty_email_layout_id).setVisibility(View.VISIBLE);
	}

	private TextWatcher emailWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
		}

		@Override
		public void afterTextChanged(Editable editable) {
		}

		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			bountyViewModel.setEmail(charSequence.toString());
		}
	};

	private void showErrorDialog() {
		new StaxDialog(requireActivity())
				.setDialogTitle(R.string.no_internet_title)
				.setDialogTitleDrawable(R.drawable.ic_warning)
				.setDialogMessage(R.string.no_internet_desc)
				.setNegButton(R.string.btn_cancel, null)
				.setPosButton(R.string.retry, v -> {
					onClick(view.findViewById(R.id.continueEmailBountyButton));
				})
				.showIt();
	}

	@Override
	public void onClick(View v) {
		if (isContinueButton(v) && validates()) {
			emailInput.setFocusable(false);
			emailInput.setState("", AbstractStatefulInput.DISABLED);
			continueButton.startAnimation(COUNT_FIVE_SECS);
			new BountyAsyncCaller(
					new WeakReference<>(getContext()),
					bountyViewModel)
					.execute();
		}
	}
}
