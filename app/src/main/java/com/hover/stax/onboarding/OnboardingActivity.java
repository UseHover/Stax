package com.hover.stax.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.amplitude.api.Amplitude;
import com.hover.stax.R;
import com.hover.stax.home.MainActivity;
import com.hover.stax.utils.Utils;


public class OnboardingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
	private static final String TAG = "OnboardingActivity";

	private static final int FIRST_SCROLL_DELAY = 4000;
	private static final int OTHER_SCROLL_DELAY = 5000;
	private static final double SWIPE_DURATION_FACTOR = 2.0;
	private static final double AUTOSCROLL_EASE_DURACTION_FACTOR = 5.0;

	private StaxAutoScrollViewPager viewPager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Amplitude.getInstance().logEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)));
		setContentView(R.layout.onboarding_layout);

		setupOnboardingSlides();
		initContinueButton();
	}

	private void initContinueButton() {
		findViewById(R.id.onboarding_continue_btn).setOnClickListener(this);
	}
	private void setupOnboardingSlides() {
		viewPager = findViewById(R.id.vpPager);
		FragmentPagerAdapter adapterViewPager = new SlidesPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		viewPager.startAutoScroll(FIRST_SCROLL_DELAY);
		viewPager.setInterval(OTHER_SCROLL_DELAY);
		viewPager.setCycle(true);
		viewPager.setAutoScrollDurationFactor(AUTOSCROLL_EASE_DURACTION_FACTOR);
		viewPager.setSwipeScrollDurationFactor(SWIPE_DURATION_FACTOR);
		viewPager.setStopScrollWhenTouch(true);
		viewPager.addOnPageChangeListener(this);
		viewPager.setAdapter(adapterViewPager);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		Log.d(TAG, "pager scrolled onboarding slide: " + position);
	}

	@Override
	public void onPageSelected(int position) {
		Log.d(TAG, "pager selected onboarding slide: " + position);
		Amplitude.getInstance().logEvent(getString(R.string.viewing_onboarding_slide, String.valueOf(position)));
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		Log.d(TAG, "pager state changed to " + state);
	}

	@Override
	public void onClick(View v) {
		viewPager.stopAutoScroll();
		Amplitude.getInstance().logEvent(getString(R.string.clicked_onboarding_getstarted_btn));
		setPassedThrough();
		goToMainActivity();
	}

	private void goToMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
	public static boolean hasPassedThrough(Context context) {
		return Utils.getBoolean(TAG, context);
	}
	private void setPassedThrough() {
		Utils.saveBoolean(TAG, true, this);
	}
}
