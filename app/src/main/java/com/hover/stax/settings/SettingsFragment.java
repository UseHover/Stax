package com.hover.stax.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
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
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.databinding.FragmentSettingsBinding;
import com.hover.stax.languages.Lang;
import com.hover.stax.languages.LanguageViewModel;
import com.hover.stax.navigation.NavigationInterface;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

import static android.view.View.GONE;

public class SettingsFragment extends Fragment implements NavigationInterface {
    final public static String LANG_CHANGE = "Settings";

    private ArrayAdapter<Channel> accountAdapter;

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_security)), requireContext());

        PinsViewModel securityViewModel = new ViewModelProvider(this).get(PinsViewModel.class);

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        setUpAccounts(securityViewModel);
        setUpChooseLang();
        setUpContactStax();
        setupGetSupport();

        return binding.getRoot();
    }

    private void setUpChooseLang() {
        TextView btn = binding.languageCard.selectLanguageBtn;
        LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
        languageViewModel.loadLanguages().observe(getViewLifecycleOwner(), languages -> {
            for (Lang lang : languages) {
                if (lang.isSelected()) btn.setText(lang.name);
            }
        });

        assert getActivity() != null;
        btn.setOnClickListener(view -> navigateToLanguageSelectionFragment(getActivity()));
    }

    private void setupGetSupport() {
        binding.getSupportStax.contactSupport.setOnClickListener(v -> openEmailClient(
                getString(R.string.stax_support_email),
                getString(R.string.stax_emailing_subject, Hover.getDeviceId(requireContext())),
                requireActivity()));
    }

    private void setUpAccounts(PinsViewModel securityViewModel) {
        accountAdapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item);
        securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
            showAccounts(channels);
            if (channels != null && channels.size() > 1)
                createDefaultSelector(channels, securityViewModel);
            else
                binding.cardAccounts.defaultAccountEntry.setVisibility(GONE);
        });
    }

    private void setUpContactStax() {
        binding.contactStax.twitterContact.setOnClickListener(v -> Utils.openUrl(getString(R.string.stax_twitter_url), requireContext()));
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

    private void openEmailClient(String recipientEmail, String subject, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:"+recipientEmail+" ?subject=" + subject);
        intent.setData(data);
        activity.startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}