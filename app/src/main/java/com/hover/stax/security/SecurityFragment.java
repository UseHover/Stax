package com.hover.stax.security;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.Lang;
import com.hover.stax.languages.LanguageViewModel;
import com.hover.stax.languages.SelectLanguageActivity;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;
import com.yariksoffice.lingver.Lingver;

import java.util.List;

import static android.view.View.GONE;

public class SecurityFragment extends Fragment {
	final public static String LANG_CHANGE = "Settings";

	private ArrayAdapter<Channel> accountAdapter;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_security)));
		PinsViewModel securityViewModel = new ViewModelProvider(this).get(PinsViewModel.class);

		View root = inflater.inflate(R.layout.fragment_security, container, false);
		setUpAccounts(root, securityViewModel);
		setUpRemovePins(root, securityViewModel);
		setUpChooseLang(root);
		return root;
	}

	private void setUpChooseLang(View root) {
		TextView btn = root.findViewById(R.id.select_language_btn);
		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(getViewLifecycleOwner(), languages -> {
			for (Lang lang : languages) {
				if (lang.isSelected()) btn.setText(lang.name);
			}
		});
		btn.setOnClickListener(view -> goToLanguageSelect(view.getContext()));
	}

	private void goToLanguageSelect(Context c) {
		Intent intent = new Intent(c, SelectLanguageActivity.class);
		intent.putExtra(LANG_CHANGE, true);
		startActivity(intent);
	}

	private void setUpAccounts(View root, PinsViewModel securityViewModel) {
		accountAdapter = new ArrayAdapter<>(root.getContext(), R.layout.stax_spinner_item);
		securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			showAccounts(channels, root);
			if (channels != null && channels.size() > 1)
				createDefaultSelector(channels, root, securityViewModel);
			else
				root.findViewById(R.id.defaultAccountEntry).setVisibility(GONE);
		});
	}

	private void showAccounts(List<Channel> channels, View root) {
		ListView lv = root.findViewById(R.id.accounts_list);
		accountAdapter.clear();
		accountAdapter.addAll(channels);
		lv.setAdapter(accountAdapter);
		lv.setOnItemClickListener((arg0, arg1, position, arg3) -> goToAccountDetail(channels.get(position).id));
		UIHelper.fixListViewHeight(lv);
	}

	private void goToAccountDetail(int channel_id) {
		Bundle bundle = new Bundle();
		bundle.putInt("channel_id", channel_id);
		NavHostFragment.findNavController(SecurityFragment.this).navigate(R.id.pinUpdateFragment, bundle);
	}

	private void createDefaultSelector(List<Channel> channels, View root, PinsViewModel securityViewModel) {
		AutoCompleteTextView spinner = root.findViewById(R.id.defaultAccountSpinner);
		root.findViewById(R.id.defaultAccountEntry).setVisibility(View.VISIBLE);
		spinner.setAdapter(accountAdapter);
		spinner.setText(spinner.getAdapter().getItem(0).toString(), false);
		spinner.setOnItemClickListener((adapterView, view, pos, id) -> {
			if (pos != 0) securityViewModel.setDefaultAccount(channels.get(pos));
		});
	}

	private void setUpRemovePins(View root, PinsViewModel securityViewModel) {
		root.findViewById(R.id.removePinsButtonId).setOnClickListener(view -> {
			new StaxDialog(root.getContext(), this)
					.setDialogTitle(R.string.remove_pins)
					.setDialogMessage(R.string.remove_pins_dialog_message)
					.setPosButton(R.string.yes, btn -> {
						securityViewModel.clearAllPins();
						UIHelper.flashMessage(getContext(), getContext().getResources().getString(R.string.remove_pin_successful));
					})
					.setNegButton(R.string.no, null)
					.isDestructive()
					.showIt();
		});
	}

}
