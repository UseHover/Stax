package com.hover.stax.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hover.stax.R;

import org.jetbrains.annotations.NotNull;

public class SlidesPagerAdapter extends FragmentPagerAdapter {
    private static final int SLIDES_SIZE = 3;

    public SlidesPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @Override
    public int getCount() {
        return SLIDES_SIZE;
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SlidesFragment.newInstance(R.layout.slide1_fragment);
            case 1:
                return SlidesFragment.newInstance(R.layout.slide2_fragment);
            case 2:
                return SlidesFragment.newInstance(R.layout.slide3_fragment);
            default:
                return new Fragment();
        }
    }
}