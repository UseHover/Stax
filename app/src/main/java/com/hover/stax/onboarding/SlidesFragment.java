package com.hover.stax.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


public class SlidesFragment extends Fragment {
	private static final String TAG = "SlidesFragment";
	private int resLayout = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null)
			resLayout = getArguments().getInt(TAG, 0);
	}

	public static SlidesFragment newInstance(int resLayout) {
		SlidesFragment fragment = new SlidesFragment();
		Bundle args = new Bundle();
		args.putInt(TAG, resLayout);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(resLayout, container, false);
	}
}
