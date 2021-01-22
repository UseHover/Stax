package com.hover.stax.permissions;

import android.app.Activity;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.hover.stax.R;
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
		customNegListener = null;
		customPosListener = null;
	}
}
