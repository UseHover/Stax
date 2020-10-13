package com.hover.stax.security;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.amplitude.api.Amplitude;
import com.hover.sdk.transactions.TransactionContract;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.Lang;
import com.hover.stax.languages.LanguageViewModel;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.views.StaxDialog;
import com.yariksoffice.lingver.Lingver;

import java.util.List;

import static android.view.View.GONE;

public class SecurityFragment extends Fragment {
	final public static String LANG_CHANGE = "Settings";

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_security)));
		PinsViewModel securityViewModel = new ViewModelProvider(this).get(PinsViewModel.class);

		View root = inflater.inflate(R.layout.fragment_security, container, false);
		setUpChooseLang(root);
		setUpAccounts(root, securityViewModel);
		setUpRemovePins(root, securityViewModel);
		return root;
	}

	private void setUpChooseLang(View root) {
		AppCompatSpinner languageSpinner = root.findViewById(R.id.selectLanguageSpinner);
		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(getViewLifecycleOwner(), languages -> {
			ArrayAdapter<Lang> adapter = new ArrayAdapter<>(getContext(), R.layout.stax_spinner_item, languages);
			languageSpinner.setAdapter(adapter);
			languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (position != 0) {
						String code = languages.get(position).code;
						if (code != null) {
							Lang.LogChange(code, getActivity());
							Lingver.getInstance().setLocale(getContext(), code);
							restart();
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});
		});
	}

	private void restart() {
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.putExtra(LANG_CHANGE, true);
		startActivity(intent);
		if (getActivity() != null) getActivity().finish();
	}

	private void setUpAccounts(View root, PinsViewModel securityViewModel) {
		securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			showAccounts(channels, root);
			if (channels != null && channels.size() > 1) {
				createDefaultSelector(channels, root, securityViewModel);
			} else {
				root.findViewById(R.id.defaultAccountSpinner).setVisibility(GONE);
			}
		});
	}

	private void showAccounts(List<Channel> channels, View root) {
		ListView lv = root.findViewById(R.id.accounts_list);
		ArrayAdapter<Channel> adapter = new ArrayAdapter<>(requireActivity(), R.layout.stax_spinner_item, channels);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener((arg0, arg1, position, arg3) -> goToAccountDetail(channels.get(position).id));
		UIHelper.fixListViewHeight(lv);
	}

	private void goToAccountDetail(int channel_id) {
		Bundle bundle = new Bundle();
		bundle.putInt(TransactionContract.COLUMN_CHANNEL_ID, channel_id);
		NavHostFragment.findNavController(SecurityFragment.this).navigate(R.id.channelsDetailsFragment);
	}

	private void createDefaultSelector(List<Channel> channels, View root, PinsViewModel securityViewModel) {
		AppCompatSpinner spinner = root.findViewById(R.id.defaultAccountSpinner);
		ArrayAdapter<Channel> adapter = new ArrayAdapter<>(getContext(), R.layout.stax_spinner_item, channels);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position != 0) { // 0 is always be the default account, so need to set for 0.
					Channel newDefaultChannel = channels.get(position);
					newDefaultChannel.defaultAccount = true;
					securityViewModel.setDefaultAccount(newDefaultChannel);
				}

				AppCompatTextView textView = (AppCompatTextView) parent.getChildAt(0);
				if (textView != null) {
					textView.setTextColor(getResources().getColor(R.color.white));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
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
