package com.hover.stax.onboarding;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hover.stax.R;
import com.hover.stax.databinding.OnboardingLayoutBinding;
import com.hover.stax.home.MainActivity;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;


public class OnBoardingActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private static final String TAG = "OnboardingActivity";

    private static final int FIRST_SCROLL_DELAY = 4000;
    private static final int OTHER_SCROLL_DELAY = 5000;
    private static final double SWIPE_DURATION_FACTOR = 2.0;
    private static final double AUTOSCROLL_EASE_DURACTION_FACTOR = 5.0;

    private StaxAutoScrollViewPager viewPager;

    private OnboardingLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        UIHelper.setFullscreenView(this);
        super.onCreate(savedInstanceState);
        Utils.logAnalyticsEvent(getString(R.string.visit_screen, getString(R.string.visit_onboarding)), this);
        binding = OnboardingLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupOnboardingSlides();
        initContinueButton();
    }

    private void setFullscreenView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }
    private void initContinueButton() {
        binding.onboardingContinueBtn.setOnClickListener(this);
    }


    private void setupOnboardingSlides() {
        viewPager = binding.vpPager;
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
    }

    @Override
    public void onPageSelected(int position) {
        Utils.logAnalyticsEvent(getString(R.string.viewing_onboarding_slide, String.valueOf(position)), this);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onClick(View v) {
        viewPager.stopAutoScroll();
        Utils.logAnalyticsEvent(getString(R.string.clicked_getstarted), this);
        setPassedThrough();
        goToMainActivity();
    }

    private void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    public static boolean hasPassedThrough(Context context) {
        return Utils.getBoolean(TAG, context);
    }

    private void setPassedThrough() {
        Utils.saveBoolean(TAG, true, this);
    }
}
