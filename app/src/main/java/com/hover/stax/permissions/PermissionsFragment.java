package com.hover.stax.permissions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.amplitude.api.Amplitude;
import com.hover.sdk.api.Hover;
import com.hover.sdk.permissions.PermissionHelper;
import com.hover.stax.R;
import com.hover.stax.utils.Constants;
import com.hover.stax.utils.Utils;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PermissionsFragment extends DialogFragment {
    private static String TAG = "PermissionsFragment", REASON = "reason", STARTWITH = "start_with";

    public final static int PHONE = 0, SMS = 1, OVERLAY = 2, ACCESS = 3;

    private PermissionHelper helper;
    private StaxPermissionDialog dialog;
    private int current;
    private boolean hasLeft = false;

    static PermissionsFragment newInstance(String reason, boolean onlyAccessibility) {
        PermissionsFragment f = new PermissionsFragment();
        Bundle args = new Bundle();
        args.putString(REASON, reason);
        args.putInt(STARTWITH, onlyAccessibility ? ACCESS : OVERLAY);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        helper = new PermissionHelper(getContext());
        current = helper.hasOverlayPerm() ? ACCESS : OVERLAY;
        Utils.logAnalyticsEvent(getString(current == OVERLAY ? R.string.perms_overlay_dialog : R.string.perms_accessibility_dialog), requireContext());
        dialog = (StaxPermissionDialog) new StaxPermissionDialog(getActivity())
                .setDialogTitle(R.string.perm_dialoghead)
                .setDialogMessage(getString(R.string.perm_dialogbody, getArguments().getString(REASON)))
                .setNegButton(R.string.btn_cancel, view -> cancel())
                .setPosButton(R.string.perm_cta1, view -> requestOverlay())
                .highlightPos();
        maybeUpdateToNext();
        return dialog.createIt();
    }

    public void requestOverlay() {
        hasLeft = true;
        Utils.logAnalyticsEvent(getString(R.string.perms_overlay_requested), requireContext());
        helper.requestOverlayPerm();
    }

    public void requestAccessibility() {
        hasLeft = true;
        Utils.logAnalyticsEvent(getString(R.string.perms_accessibility_requested), requireContext());
        Hover.setPermissionActivity(Constants.PERM_ACTIVITY, getContext());
        helper.requestAccessPerm();
    }

    @Override
    public void onResume() {
        super.onResume();
        logReturnEvent();
        maybeUpdateToNext();
    }

    private void logReturnEvent() {
        if (hasLeft) {
            if (current == OVERLAY)
                Utils.logAnalyticsEvent(getString(helper.hasOverlayPerm() ? R.string.perms_overlay_granted : R.string.perms_overlay_notgranted), requireContext());
            else if (current == ACCESS)
                Utils.logAnalyticsEvent(getString(helper.hasAccessPerm() ? R.string.perms_accessibility_granted : R.string.perms_accessibility_notgranted), requireContext());
        }
    }

    private void maybeUpdateToNext() {
        if (getArguments().getInt(STARTWITH) == ACCESS && !helper.hasAccessPerm())
            setOnlyNeedAccess();
        else if (current == OVERLAY && helper.hasOverlayPerm() && !helper.hasAccessPerm())
            new Handler(Looper.getMainLooper()).postDelayed(this::animateToStep2, 500);
        else if (helper.hasAccessPerm())
            animateToDone();
    }

    private void setOnlyNeedAccess() {
        animateToStep2();
        dialog.getView().findViewById(R.id.progress_text).setVisibility(View.GONE);
        dialog.getView().findViewById(R.id.progress_indicator).setVisibility(View.GONE);
        dialog.setDialogMessage(getString(R.string.perm_accessibiltiy_dialogbody, getArguments().getString(REASON)));
        dialog.setPosButton(R.string.perm_cta1, view -> requestAccessibility());
    }

    private void animateToStep2() {
        current = ACCESS;
        if (dialog != null) {
            dialog.animateProgressTo(81);
            ((TextView) dialog.getView().findViewById(R.id.progress_text)).setText(getContext() != null ? getString(R.string.perm_progress2) : "");
            dialog.getView().findViewById(R.id.overlay_example).setVisibility(View.GONE);
            dialog.getView().findViewById(R.id.accessibility_example).setVisibility(View.VISIBLE);
            dialog.setPosButton(R.string.perm_cta2, view -> requestAccessibility());
        }
    }

    private void animateToDone() {
        if (dialog != null)
            dialog.animateProgressTo(100);
        getActivity().setResult(RESULT_OK);
        new Handler(Looper.getMainLooper()).postDelayed(() -> getActivity().finish(), getArguments().getInt(STARTWITH) == ACCESS ? 10 : 800);
    }

    private void cancel() {
        Utils.logAnalyticsEvent(getString(current == OVERLAY ? R.string.perms_overlay_cancelled : R.string.perms_accessibility_cancelled), requireContext());
        if (dialog != null) dialog.dismiss();
        getActivity().setResult(RESULT_CANCELED);
        getActivity().finish();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        cancel();
    }
}
