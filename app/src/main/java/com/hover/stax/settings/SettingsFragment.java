package com.hover.stax.settings;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hover.sdk.api.Hover;
import com.hover.stax.BuildConfig;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.FragmentSettingsBinding;
import com.hover.stax.languages.Lang;
import com.hover.stax.languages.LanguageViewModel;
import com.hover.stax.library.LibraryActivity;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public class SettingsFragment extends Fragment implements NavigationInterface {

    final public static String LANG_CHANGE = "Settings", TEST_MODE_KEY = "test_mode";

    private ArrayAdapter<Channel> accountAdapter;

    private FragmentSettingsBinding binding;
    private int clickCounter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_security)), requireContext());

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        setUpAccounts();
        setUpChooseLang();
        setUpEnableTestMode();
        setUpSupport();
        setUpUssdLibrary();
        setupAppVersionInfo();

        return binding.getRoot();
    }

    private void setUpAccounts() {
        PinsViewModel securityViewModel = new ViewModelProvider(requireActivity()).get(PinsViewModel.class);
        accountAdapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item);
        securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
            showAccounts(channels);
            if (channels != null && channels.size() > 1)
                createDefaultSelector(channels, securityViewModel);
            else
                binding.cardAccounts.defaultAccountEntry.setVisibility(GONE);
        });
    }

    private void showAccounts(List<Channel> channels) {
        ListView lv = binding.cardAccounts.accountsList;
        accountAdapter.clear();
        accountAdapter.addAll(channels);
        lv.setAdapter(accountAdapter);
        lv.setOnItemClickListener((arg0, arg1, position, arg3) -> navigateToPinUpdateFragment(channels.get(position).id, SettingsFragment.this));
        UIHelper.fixListViewHeight(lv);
    }

    private void createDefaultSelector(List<Channel> channels, PinsViewModel securityViewModel) {
        AutoCompleteTextView spinner = binding.cardAccounts.defaultAccountSpinner;
        binding.cardAccounts.defaultAccountEntry.setVisibility(View.VISIBLE);
        spinner.setAdapter(accountAdapter);
        spinner.setText(spinner.getAdapter().getItem(0).toString(), false);
        spinner.setOnItemClickListener((adapterView, view, pos, id) -> {
            if (pos != 0) securityViewModel.setDefaultAccount(channels.get(pos));
        });
    }

    private void setUpChooseLang() {
        TextView btn = binding.languageCard.selectLanguageBtn;
        LanguageViewModel languageViewModel = new ViewModelProvider(requireActivity()).get(LanguageViewModel.class);
        languageViewModel.loadLanguages().observe(getViewLifecycleOwner(), languages -> {
            for (Lang lang : languages) {
                if (lang.isSelected()) btn.setText(lang.name);
            }
        });

        assert getActivity() != null;
        btn.setOnClickListener(view -> navigateToLanguageSelectionFragment(getActivity()));
    }

    private void setUpEnableTestMode() {
        binding.cardAccounts.testMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Utils.saveBoolean(Constants.TEST_MODE, isChecked, requireContext());
            UIHelper.flashMessage(requireContext(), isChecked ? R.string.test_mode_toast : R.string.test_mode_disabled);
        });

        binding.cardAccounts.testMode.setVisibility(Utils.getBoolean(Constants.TEST_MODE, requireContext()) ? VISIBLE : GONE);

        binding.disclaimer.setOnClickListener(v -> {
            clickCounter++;
            if (clickCounter == 5)
                UIHelper.flashMessage(requireContext(), R.string.test_mode_almost_toast);
            else if (clickCounter == 7)
                enableTestMode();
        });
    }

    private void enableTestMode() {
        Utils.saveBoolean(Constants.TEST_MODE, true, requireContext());
        binding.cardAccounts.testMode.setVisibility(VISIBLE);
        UIHelper.flashMessage(requireContext(), R.string.test_mode_toast);
    }

    private void setUpSupport() {
        binding.staxSupport.twitterContact.setOnClickListener(v -> Utils.openUrl(getString(R.string.stax_twitter_url), requireContext()));
        binding.staxSupport.receiveStaxUpdate.setOnClickListener(v -> Utils.openUrl(getString(R.string.receive_stax_updates_url), requireContext()));
        binding.staxSupport.requestFeature.setOnClickListener(v -> Utils.openUrl(getString(R.string.stax_nolt_url), requireContext()));
        binding.staxSupport.contactSupport.setOnClickListener(v -> Utils.openEmail(getString(R.string.stax_emailing_subject, Hover.getDeviceId(requireContext())), requireContext()));
        binding.staxSupport.faq.setOnClickListener(v -> navigateFAQ(this));
    }

    private void setUpUssdLibrary() {
        binding.libraryCard.visitLibrary.setOnClickListener(v -> getActivity().startActivity(new Intent(getActivity(), LibraryActivity.class)));
    }

    private void setupAppVersionInfo() {
        String deviceId = Hover.getDeviceId(requireContext());
        String appVersion = BuildConfig.VERSION_NAME;
        String versionCode = String.valueOf(BuildConfig.VERSION_CODE);
        binding.staxAndDeviceInfo.setText(getString(R.string.app_version_and_device_id, appVersion, versionCode, deviceId));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}