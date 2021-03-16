package com.hover.stax.bounties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.hover.stax.R;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxTextInputLayout;

import java.lang.ref.WeakReference;

public class BountyEmailFragment extends Fragment implements NavigationInterface, View.OnClickListener,  BountyAsyncCaller.AsyncResponseListener {
	private StaxTextInputLayout emailInput;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_bounty_email, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		emailInput = view.findViewById(R.id.emailInput);
		emailInput.setText(Utils.getString(BountyActivity.EMAIL_KEY, getContext()));
		view.findViewById(R.id.continueEmailBountyButton).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (validates()) {
			emailInput.setEnabled(false);
			new BountyAsyncCaller(new WeakReference<>(getContext()), this).execute(emailInput.getText());
			emailInput.setState(getString(R.string.bounty_uploading_email), AbstractStatefulInput.INFO);
		} else
			emailInput.setState(getString(R.string.bounty_email_error), AbstractStatefulInput.ERROR);
	}

	private boolean validates() {
		if (emailInput.getText() == null) return false;
		String email = emailInput.getText().replace(" ", "");
		return email.matches("(?:[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");
	}

	@Override
	public void onComplete(Integer responseCode) {
		if (responseCode >= 200 && responseCode < 300)
			saveAndContinue();
		else {
			emailInput.setEnabled(true);
			emailInput.setState(getString(R.string.bounty_api_internet_error), AbstractStatefulInput.ERROR);
		}
	}

	private void saveAndContinue() {
		Utils.saveString(BountyActivity.EMAIL_KEY, emailInput.getText(), getContext());
		NavHostFragment.findNavController(this).navigate(R.id.bountyListFragment);
	}
}
