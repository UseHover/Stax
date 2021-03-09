package com.hover.stax.bounty;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hover.stax.R;
import com.hover.stax.database.DatabaseRepo;
import com.hover.stax.navigation.AbstractNavigationActivity;
import com.hover.stax.views.AbstractStatefulInput;

public class BountyActivity extends AbstractNavigationActivity {
	public BountyViewModel bountyViewModel;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bountyViewModel = new ViewModelProvider(this).get(BountyViewModel.class);
		setContentView(R.layout.activity_bounty);
	}
}
