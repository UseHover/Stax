package com.hover.stax.bounty;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxTextInputLayout;

import java.lang.ref.WeakReference;

public class BountyEmailFragment extends Fragment implements NavigationInterface, View.OnClickListener {
	private BountyViewModel bountyViewModel;
	private View view;
	private StaxTextInputLayout emailInput;

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
				promptEmailOrNavigateBountyList();
			}
			else UIHelper.flashMessage(requireContext(), result);
		});
	}
	private void initEmailInput() {
		emailInput = view.findViewById(R.id.emailInput);
		emailInput.addTextChangedListener(emailWatcher);
	}

	private void initContinueButton() {
		AppCompatButton continueButton = view.findViewById(R.id.continueEmailBountyButton);
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

	@Override
	public void onClick(View v) {
		if (isContinueButton(v) && validates()) {
			new BountyAsyncCaller(
					new WeakReference<>(getContext()),
					bountyViewModel)
					.execute();
		}
	}
}
