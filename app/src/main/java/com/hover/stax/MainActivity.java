package com.hover.stax;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hover.stax.ui.onboard.SplashScreenActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

	public static boolean GO_TO_SPLASH_SCREEN = true;
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if(GO_TO_SPLASH_SCREEN) {
		startActivity(new Intent(this, SplashScreenActivity.class));
		finishAffinity();
	}

	setContentView(R.layout.activity_main);
	BottomNavigationView navView = findViewById(R.id.nav_view);
	// Passing each menu ID as a set of Ids because each
	// menu should be considered as top level destinations.
	AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
			 R.id.navigation_home, R.id.navigation_buyAirtime, R.id.navigation_moveMoney, R.id.navigation_security)
			.build();
	NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
	NavigationUI.setupWithNavController(navView, navController);
}

}
