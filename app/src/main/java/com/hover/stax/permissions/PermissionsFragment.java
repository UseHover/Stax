package com.hover.stax.permissions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.permissions.PermissionDialog;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.channels.ChannelsAdapter;
import com.hover.stax.security.PinsViewModel;
import com.hover.stax.views.StaxDialog;

import static android.app.Activity.RESULT_OK;

public class PermissionsFragment extends DialogFragment {
	private static String TAG = "PermissionsFragment";

	public final static int PHONE = 0, SMS = 1, OVERLAY = 2, ACCESS = 3;

	private StaxPermissionDialog dialog;
	private int current;
	private PermissionHelper helper;

	static PermissionsFragment newInstance(int num) {
		PermissionsFragment f = new PermissionsFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("num", num);
		f.setArguments(args);

		return f;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Log.e(TAG, "creating dialog " + getArguments().getInt("num"));
		current = OVERLAY;
		helper = new PermissionHelper(getContext());
		dialog = (StaxPermissionDialog) new StaxPermissionDialog(getActivity())
			.setDialogTitle(R.string.perm_dialoghead)
			.setDialogMessage(R.string.perm_dialogbody)
			.setNegButton(R.string.btn_cancel, onCancel)
			.setPosButton(R.string.perm_cta1, clickedGoOverlay)
			.highlightPos();
		return dialog.createIt();
	}

	private View.OnClickListener onCancel = view -> dialog.dismiss();
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
		if (current == OVERLAY && helper.hasOverlayPerm() && !helper.hasAccessPerm())
			animateToStep2();
		else if (current == ACCESS && helper.hasAccessPerm())
			animateToDone();
	}

	private void animateToStep2() {
		ProgressBar pb = getView().findViewById(R.id.progress_indicator);
		pb.setProgress(81);
		dialog.setPosButton(R.string.perm_cta2, clickedGoAccess);
	}

	private void animateToDone() {
		ProgressBar pb = getView().findViewById(R.id.progress_indicator);
		pb.setProgress(81);
		Amplitude.getInstance().logEvent(getString(R.string.granted_sdk_permissions));
		getActivity().setResult(RESULT_OK);
		getActivity().finish();
	}
}
