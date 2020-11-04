package com.hover.stax.requestAccount;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.amplitude.api.Amplitude;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hover.stax.R;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxCardView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class RequestAccountActivity extends AppCompatActivity {
	private TextView requestDescText, contactDescText;
	private RequestAccountViewModel requestAccountViewModel;
	private TableRow countryRow, networkRow;
	private TextView countryValue, networkValue;
	private RadioGroup radioCountryGrp, radioNetworkGrp;
	private StaxCardView countryStaxCard, networkStaxCard, giveContactStaxCard;
	private TextInputEditText phoneInput, emailInput;
	private TextInputLayout phoneEntry, emailEntry;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_account_layout);
		init();
		requestAccountViewModel = new ViewModelProvider(this).get(RequestAccountViewModel.class);
		setClicks();

		requestAccountViewModel.getCountriesMutableLiveData().observe(this, this::createCountryRadios);
		requestAccountViewModel.getRequestAccountStageMutableLiveData().observe(this, stage-> {
			switch (stage) {
				case SELECT_COUNTRY: showSelectCountryStage();
					break;
				case SELECT_NETWORK: showSelectNetworkStage();
					break;
				default:showGiveContactInfoStage();
					break;
			}
		});
		requestAccountViewModel.getPhoneError().observe(this, stringRes-> {
			if(stringRes !=null && stringRes !=0) {
				phoneEntry.setError(getString(stringRes));
				phoneEntry.setErrorIconDrawable(0);
			}
			else {
				phoneEntry.setError(null);
			}
		});
		requestAccountViewModel.getEmailError().observe(this, stringRes-> {
			if(stringRes !=null && stringRes !=0) {
				emailEntry.setError(getString(stringRes));
				emailEntry.setErrorIconDrawable(0);
			}
			else {
				emailEntry.setError(null);
			}
		});

	}

	void init() {
		countryRow = findViewById(R.id.countryRow);
		networkRow = findViewById(R.id.networkRow);
		countryValue = findViewById(R.id.countryValue);
		networkValue = findViewById(R.id.networkValue);
		requestDescText = findViewById(R.id.request_account_text_desc);
		contactDescText = findViewById(R.id.contact_desc);

		radioCountryGrp = findViewById(R.id.countriesRadioGroup);
		radioNetworkGrp = findViewById(R.id.networkRadioGroup);
		countryStaxCard = findViewById(R.id.countryStaxCard);
		networkStaxCard = findViewById(R.id.networkStaxCard);
		giveContactStaxCard = findViewById(R.id.giveContactStaxCard);

		phoneInput = findViewById(R.id.phone_input);
		emailInput = findViewById(R.id.email_input);
		phoneEntry = findViewById(R.id.phoneEntry);
		emailEntry = findViewById(R.id.emailEntry);

		countryRow.setVisibility(View.GONE);
		networkRow.setVisibility(View.GONE);
	}

	void showSelectCountryStage() {
		countryRow.setVisibility(View.GONE);
		networkRow.setVisibility(View.GONE);

		countryStaxCard.setVisibility(View.VISIBLE);
		networkStaxCard.setVisibility(View.GONE);
		giveContactStaxCard.setVisibility(View.GONE);

	}
	void showSelectNetworkStage() {
		countryRow.setVisibility(View.VISIBLE);
		networkRow.setVisibility(View.GONE);

		countryStaxCard.setVisibility(View.GONE);
		networkStaxCard.setVisibility(View.VISIBLE);
		giveContactStaxCard.setVisibility(View.GONE);

		radioCountryGrp.setVisibility(View.GONE);
		radioCountryGrp.setOnCheckedChangeListener(null);
	}
	void showGiveContactInfoStage() {
		requestDescText.setText(getResources().getString(R.string.request_received_content, networkValue.getText()));
		contactDescText.setText(getResources().getString(R.string.contact_optional, networkValue.getText()));

		countryRow.setVisibility(View.VISIBLE);
		networkRow.setVisibility(View.VISIBLE);

		countryStaxCard.setVisibility(View.GONE);
		networkStaxCard.setVisibility(View.GONE);
		giveContactStaxCard.setVisibility(View.VISIBLE);
	}

	void setClicks() {
		findViewById(R.id.continueCountryButton).setOnClickListener(v -> {
			SupportedCountries.LogChange(countryValue.getText().toString(), RequestAccountActivity.this);
			createServicesRadio();
			requestAccountViewModel.setNextRequestAccountStage(RequestAccountStage.SELECT_COUNTRY);
		});

		findViewById(R.id.continueNetworkButton).setOnClickListener(v -> {
			JSONObject data = new JSONObject();
			try { data.put("network", networkValue.getText().toString()); } catch (JSONException ignored) { }
			Amplitude.getInstance().logEvent(getResources().getString(R.string.selected_network), data);
			requestAccountViewModel.sendAccountRequestInfoToFirebase(countryValue.getText().toString(), networkValue.getText().toString(), Utils.getDeviceId(this));
			requestAccountViewModel.setNextRequestAccountStage(RequestAccountStage.SELECT_NETWORK);
		
		});

		findViewById(R.id.noThanksButton).setOnClickListener(v -> {
				goToPreviousActivity(false);
		});

		findViewById(R.id.sendContactButton).setOnClickListener(v -> {
			if(requestAccountViewModel.validateContactEntries(phoneInput.getText().toString(), emailInput.getText().toString())) {
				requestAccountViewModel.sendContactInfoToFirebase(phoneInput.getText().toString(), emailInput.getText().toString(), Utils.getDeviceId(this));
				goToPreviousActivity(true);
			}
		});
	}

	private void createCountryRadios(List<SupportedCountries> supportedCountries) {
		for (int l = 0; l < supportedCountries.size(); l++) {
			@SuppressLint("InflateParams") RadioButton radioButton = (RadioButton) LayoutInflater.from(this).inflate(R.layout.stax_radio_button, null);
			radioButton.setId(l);
			radioButton.setText(supportedCountries.get(l).name);
			radioButton.setTag(supportedCountries.get(l).name);
			if (l ==0 ) {
				countryValue.setText(supportedCountries.get(l).name);
				radioButton.setChecked(true);
			}
			else radioButton.setChecked(false);

			radioCountryGrp.addView(radioButton);
		}

		radioCountryGrp.setOnCheckedChangeListener(this::onSelectCountry);
	}
	void createServicesRadio() {
		AccountsByCountry accountsByCountry = new AccountsByCountry(null, null);
		List<String> services = accountsByCountry.filterAccounts(accountsByCountry.init(), countryValue.getText().toString());
		for (int l = 0; l < services.size(); l++) {
			@SuppressLint("InflateParams")
			RadioButton radioButton = (RadioButton) LayoutInflater.from(this).inflate(R.layout.stax_radio_button, null);
			radioButton.setId(l);
			radioButton.setText(services.get(l));
			radioButton.setTag(services.get(l));
			if (l ==0 ) {
				networkValue.setText(services.get(l));
				radioButton.setChecked(true);
			} else radioButton.setChecked(false);
			radioNetworkGrp.addView(radioButton);
		}
		radioNetworkGrp.setOnCheckedChangeListener(this::onNetworkCountry);
	}

	private void onSelectCountry(RadioGroup group, int checkedId) {
		RadioButton radioBtn = group.findViewById(checkedId);
		countryValue.setText(radioBtn.getTag().toString());
	}
	private void onNetworkCountry(RadioGroup group, int checkedId) {
		RadioButton radioBtn = group.findViewById(checkedId);
		networkValue.setText(radioBtn.getTag().toString());
	}

	private void goToPreviousActivity(boolean sendNotice) {
		if(sendNotice) UIHelper.flashMessage(this, getResources().getString(R.string.toast_confirm_contact));
		onBackPressed();

	}
}
