package com.hover.stax.security;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.hover.stax.ApplicationInstance;
import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.home.MainActivity;
import com.hover.stax.languages.Lang;
import com.hover.stax.languages.LanguageViewModel;
import com.hover.stax.utils.UIHelper;
import com.yariksoffice.lingver.Lingver;

import org.intellij.lang.annotations.Language;

public class SecurityFragment extends Fragment {
	final public static String LANG_CHANGE = "Settings";

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.nav_security)));
		View root = inflater.inflate(R.layout.fragment_security, container, false);
		setUpChooseLang(root);
		PinsViewModel securityViewModel = new ViewModelProvider(this).get(PinsViewModel.class);
		setUpChooseDefault(root, securityViewModel);
		setUpRemovePins(root, securityViewModel);
		return root;
	}

	private void setUpChooseLang(View root) {
		AppCompatSpinner languageSpinner = root.findViewById(R.id.selectLanguageSpinner);
		LanguageViewModel languageViewModel = new ViewModelProvider(this).get(LanguageViewModel.class);
		languageViewModel.loadLanguages().observe(getViewLifecycleOwner(), languages -> {
			ArrayAdapter<Lang> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_items, languages);
			languageSpinner.setAdapter(adapter);
			Log.e("SecFrag", "selected: " + Lingver.getInstance().getLanguage());
			languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					if (position != 0) {
						String code = languages.get(position).code;
						if (code != null) {
							Amplitude.getInstance().logEvent(getString(R.string.selected_language, code));
							Lingver.getInstance().setLocale(ApplicationInstance.getContext(), code);
							restart();
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) { }
			});
		});
	}

	private void restart() {
		Intent intent = new Intent(getActivity(), MainActivity.class);
		intent.putExtra(LANG_CHANGE, true);
		startActivity(intent);
		if (getActivity() != null) getActivity().finish();
	}

	private void setUpChooseDefault(View root, PinsViewModel securityViewModel) {
		AppCompatSpinner spinner = root.findViewById(R.id.defaultAccountSpinner);
		securityViewModel.getSelectedChannels().observe(getViewLifecycleOwner(), channels -> {
			ArrayAdapter<Channel> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_items, channels);
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
				public void onNothingSelected(AdapterView<?> parent) { }
			});
		});
	}

	private void setUpRemovePins(View root, PinsViewModel securityViewModel) {
		root.findViewById(R.id.removePinsButtonId).setOnClickListener(view -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(getContext() != null ? getContext() : ApplicationInstance.getContext());
			builder.setTitle(getContext().getResources().getString(R.string.remove_pins));
			builder.setMessage(getContext().getResources().getString(R.string.remove_pins_dialog_message));
			builder.setPositiveButton(getContext().getResources().getString(R.string.yes), (dialog, which) -> {
				securityViewModel.clearAllPins();
				UIHelper.flashMessage(getContext(), getContext().getResources().getString(R.string.remove_pin_successful));
			});
			builder.setNegativeButton(getContext().getResources().getString(R.string.no), null);

			builder.show();
		});
	}

}
