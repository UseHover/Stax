package com.hover.stax.permissions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PermissionsFragment extends DialogFragment {
	private static String TAG = "PermissionsFragment", REASON = "reason", STARTWITH = "start_with";

	public final static int PHONE = 0, SMS = 1, OVERLAY = 2, ACCESS = 3;

	private StaxPermissionDialog dialog;
	private int current;
	private PermissionHelper helper;

	static PermissionsFragment newInstance(String reason, PermissionHelper ph) {
		PermissionsFragment f = new PermissionsFragment();
		Bundle args = new Bundle();
		args.putString(REASON, reason);
		args.putInt(STARTWITH, ph.hasOverlayPerm() ? ACCESS : OVERLAY);
		f.setArguments(args);
		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		helper = new PermissionHelper(getContext());
		current = helper.hasOverlayPerm() ? ACCESS : OVERLAY;
		dialog = (StaxPermissionDialog) new StaxPermissionDialog(getActivity())
			.setDialogTitle(R.string.perm_dialoghead)
			.setDialogMessage(getString(R.string.perm_dialogbody, getArguments().getString(REASON)))
			.setNegButton(R.string.btn_cancel, onCancel)
			.setPosButton(R.string.perm_cta1, clickedGoOverlay)
			.highlightPos();
		maybeUpdateToNext();
		return dialog.createIt();
	}

	private View.OnClickListener onCancel = view -> cancel();
	private View.OnClickListener clickedGoOverlay = view -> requestOverlay();
	private View.OnClickListener clickedGoAccess = view -> requestAccessibility();

	public void requestOverlay() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permoverlay));
		helper.requestOverlayPerm();
	}

	public void requestAccessibility() {
		Amplitude.getInstance().logEvent(getString(R.string.request_permaccessibility));
		Hover.setPermissionActivity("com.hover.stax.permissions.PermissionsActivity", getContext());
		helper.requestAccessPerm();
	}

	@Override
	public void onResume() {
		Log.e(TAG, "on resume");
		super.onResume();
		new Handler().postDelayed(() -> maybeUpdateToNext(), 500);
	}

	private void maybeUpdateToNext() {
		if (getArguments().getInt(STARTWITH) == ACCESS && !helper.hasAccessPerm())
			setOnlyNeedAccess();
		else if (current == OVERLAY && helper.hasOverlayPerm() && !helper.hasAccessPerm())
			animateToStep2();
		else if (helper.hasAccessPerm())
			animateToDone();
	}

	private void setOnlyNeedAccess() {
		animateToStep2();
		dialog.getView().findViewById(R.id.progress_text).setVisibility(View.GONE);
		dialog.getView().findViewById(R.id.progress_indicator).setVisibility(View.GONE);
		dialog.setDialogMessage(getString(R.string.perm_accessibiltiy_dialogbody, getArguments().getString(REASON)));
		dialog.setPosButton(R.string.perm_cta1, clickedGoAccess);
	}

	private void animateToStep2() {
		if (dialog != null) {
			dialog.animateProgressTo(81);
			((TextView) dialog.getView().findViewById(R.id.progress_text)).setText(getContext() != null ? getString(R.string.perm_progress2) : "");
			dialog.getView().findViewById(R.id.overlay_example).setVisibility(View.GONE);
			dialog.getView().findViewById(R.id.accessibility_example).setVisibility(View.VISIBLE);
			dialog.setPosButton(R.string.perm_cta2, clickedGoAccess);
		}
	}

	private void animateToDone() {
		Amplitude.getInstance().logEvent(getString(R.string.granted_sdk_permissions));
		if (dialog != null)
			dialog.animateProgressTo(100);
		getActivity().setResult(RESULT_OK);
		new Handler().postDelayed(() -> getActivity().finish(), 800);
	}

	private void cancel() {
		if (dialog != null)
			dialog.dismiss();
		getActivity().setResult(RESULT_CANCELED);
		getActivity().finish();
	}

	@Override
	public void onDismiss(@NonNull DialogInterface dialog) {
		super.onDismiss(dialog);
		dialog = null;
		cancel();
	}
}
