package com.hover.stax.navigation;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.amplitude.api.Amplitude;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.home.MainActivity;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.settings.SettingsFragment;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;

public abstract class AbstractNavigationActivity extends AppCompatActivity implements NavigationInterface {

    protected NavController navController;
    protected AppBarConfiguration appBarConfiguration;
    protected NavHostFragment navHostFragment;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setUpNav() {
        setBottomBar();

        if (getIntent().getBooleanExtra(SettingsFragment.LANG_CHANGE, false))
            navigate(this, Constants.NAV_SETTINGS);
    }

    private void setBottomBar() {
        BottomNavigationView nav = findViewById(R.id.nav_view);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = getNavController();

            NavigationUI.setupWithNavController(nav, navController);

            appBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_balance, R.id.navigation_settings).build();
        }

        setNavClickListener(nav);
        setDestinationChangedListener(nav);
    }

    private void setNavClickListener(BottomNavigationView nav) {
        nav.setOnNavigationItemSelectedListener(item -> {
            if (this instanceof MainActivity)
                checkPermissionsAndNavigate(getNavConst(item.getItemId()));
            else
                navigateThruHome(item.getItemId());
            return true;
        });
    }

    private void setDestinationChangedListener(BottomNavigationView nav) {
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (controller.getGraph().getId() == R.id.bounty_navigation) {
                    nav.getMenu().findItem(R.id.navigation_settings).setChecked(true);
                }
            });
        }
    }

    protected NavController getNavController() {
        return navHostFragment.getNavController();
    }

    public void checkPermissionsAndNavigate(int toWhere) {
        PermissionHelper permissionHelper = new PermissionHelper(this);
        if (toWhere == Constants.NAV_SETTINGS || toWhere == Constants.NAV_HOME || permissionHelper.hasBasicPerms()) {
            navigate(this, toWhere, getIntent(), false);
        } else {
            PermissionUtils.showInformativeBasicPermissionDialog(
                    pos -> PermissionUtils.requestPerms(getNavConst(toWhere), AbstractNavigationActivity.this),
                    neg -> Utils.logAnalyticsEvent(getString(R.string.perms_basic_cancelled), AbstractNavigationActivity.this),
                    this);
        }
    }

    protected void navigateThruHome(int destId) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (destId == R.id.navigation_balance)
            intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_BALANCE);
        else if (destId == R.id.navigation_settings)
            intent.putExtra(Constants.FRAGMENT_DIRECT, Constants.NAV_SETTINGS);
        else if (destId != R.id.navigation_home) {
            onBackPressed();
            return;
        }

        startActivity(intent);
    }

    protected int getNavConst(int destId) {
        if (destId == R.id.navigation_balance) return Constants.NAV_BALANCE;
        else if (destId == R.id.navigation_settings) return Constants.NAV_SETTINGS;
        else if (destId == R.id.navigation_home) return Constants.NAV_HOME;
        else return destId;
    }

    public void getStartedWithBountyButton(View view) {
        checkPermissionsAndNavigate(Constants.NAV_BOUNTY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.logPermissionsGranted(grantResults, this);
        checkPermissionsAndNavigate(requestCode);
    }
}
