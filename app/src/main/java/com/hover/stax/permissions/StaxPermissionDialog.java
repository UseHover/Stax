package com.hover.stax.permissions;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hover.stax.R;
import com.hover.stax.utils.Utils;
import com.hover.stax.views.StaxDialog;

public class StaxPermissionDialog extends StaxDialog {
	private final static String TAG = "StaxPermissionDialog";

	public final static int OVERVIEW = 0, OVERLAY = 1, ACCESS = 2;
	private int cmd;

	public StaxPermissionDialog(@NonNull Activity a) {
		this(a, a.getLayoutInflater());
	}

	private StaxPermissionDialog(Activity a, LayoutInflater inflater) {
		super(a);
		context = a;
		view = inflater.inflate(R.layout.stax_permission_dialog, null);
		((TextView) view.findViewById(R.id.app_name)).setText(Utils.getAppName(a));
		customNegListener = null;
		customPosListener = null;
	}

	public void animateProgressTo(int percent) {
		ObjectAnimator oa = ObjectAnimator.ofInt(view.findViewById(R.id.progress_indicator), "progress", percent);
		oa.setDuration(800);
		oa.setInterpolator(new DecelerateInterpolator());
		oa.start();
	}
}
