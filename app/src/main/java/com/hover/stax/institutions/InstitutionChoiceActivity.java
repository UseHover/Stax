package com.hover.stax.institutions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.hover.stax.R;

public class InstitutionChoiceActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.institution_choice_activity);

		InstitutionViewModel model = new ViewModelProvider(this).get(InstitutionViewModel.class);
		model.getInstitutions().observe(this, institutions -> {
			// update UI
		});
	}
}