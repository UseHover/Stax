package com.hover.stax.ui.chooseService.pin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.hover.stax.MainActivity;
import com.hover.stax.R;
import com.hover.stax.adapters.VariableRecyclerAdapter;
import com.hover.stax.database.ConvertRawDatabaseDataToModels;
import com.hover.stax.interfaces.VariableEditinterface;
import com.hover.stax.utils.UIHelper;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ServicesPinActivity extends AppCompatActivity implements VariableEditinterface {
private Timer timer= new Timer();
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.services_pin_layout);

	RecyclerView pinRecyclerView = findViewById(R.id.pin_recyclerView);
	ServicePinViewModel servicePinViewModel = new ViewModelProvider(this).get(ServicePinViewModel.class);
	servicePinViewModel.getServicePins();
	servicePinViewModel.loadServicePins().observe(this, choosePinModels -> {
		pinRecyclerView.setLayoutManager(UIHelper.setMainLinearManagers(this));
		pinRecyclerView.setHasFixedSize(true);
		pinRecyclerView.setAdapter(new VariableRecyclerAdapter( choosePinModels, this, true));
	});

	TextView cancelText = findViewById(R.id.choose_serves_cancel);
	UIHelper.setTextUnderline(cancelText, getResources().getString(R.string.cancel));
	cancelText.setOnClickListener(view-> finish());

	findViewById(R.id.continuePinButton).setOnClickListener(view->{
		MainActivity.GO_TO_SPLASH_SCREEN = false;
		startActivity(new Intent(this, MainActivity.class));
	});


}

@Override
public void onEditStringChanged(String serviceId, String newValue) {
	timer.cancel(); timer = new Timer(); long DELAY = 800;
	timer.schedule(new TimerTask() {@Override public void run() {
		new ConvertRawDatabaseDataToModels().saveUserServicePin(serviceId, newValue);
	}}, DELAY);

}
}
