package com.hover.stax.bounties;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.databinding.FragmentBountyEmailBinding;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.AbstractStatefulInput;
import com.hover.stax.views.StaxTextInputLayout;

import java.lang.ref.WeakReference;

public class BountyEmailFragment extends Fragment implements NavigationInterface, View.OnClickListener, BountyAsyncCaller.AsyncResponseListener {

    private StaxTextInputLayout emailInput;
    private FragmentBountyEmailBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBountyEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailInput = binding.emailInput;
        emailInput.setText(Utils.getString(BountyActivity.EMAIL_KEY, getContext()));
        binding.continueEmailBountyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Utils.logAnalyticsEvent(getString(R.string.clicked_bounty_email_continue_btn), requireContext());
        if (validates()) {
            emailInput.setEnabled(false);
            new BountyAsyncCaller(new WeakReference<>(requireContext()), this).execute(emailInput.getText());
            emailInput.setState(getString(R.string.bounty_uploading_email), AbstractStatefulInput.INFO);
        } else {
            emailInput.setState(getString(R.string.bounty_email_error), AbstractStatefulInput.ERROR);
        }
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
            setEmailError();
        }
    }

    private void setEmailError() {
        Utils.logAnalyticsEvent(getString(R.string.bounty_email_err, getString(R.string.bounty_api_internet_error)), requireContext());
        emailInput.setEnabled(true);
        emailInput.setState(getString(R.string.bounty_api_internet_error), AbstractStatefulInput.ERROR);
    }

    private void saveAndContinue() {
        Utils.logAnalyticsEvent(getString(R.string.bounty_email_success), requireContext());
        Utils.saveString(BountyActivity.EMAIL_KEY, emailInput.getText(), getContext());
        NavHostFragment.findNavController(this).navigate(R.id.bountyListFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
