package com.hover.stax.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.hover.stax.R;

public class StaxDialog extends AlertDialog {

    protected Context context;
    protected View view;
    public AlertDialog dialog;

    protected View.OnClickListener customNegListener;
    protected View.OnClickListener customPosListener;

    public StaxDialog(@NonNull Activity a) {
        this(a, a.getLayoutInflater(), false);
    }

    public StaxDialog(@NonNull Activity a, boolean usePermissionDesign) {
        this(a, a.getLayoutInflater(), usePermissionDesign);
    }

    private StaxDialog(Context c, LayoutInflater inflater, boolean usePermissionDesign) {
        super(c);
        context = c;
        view = inflater.inflate(usePermissionDesign ? R.layout.basic_perm_dialog : R.layout.stax_dialog, null);
        customNegListener = null;
        customPosListener = null;
    }

    public StaxDialog setDialogTitle(int title) {
        if(title == 0) setDialogMessage("");
        else setDialogTitle(context.getString(title));
        return this;
    }

    public StaxDialog setDialogTitle(String title) {
        LinearLayout headerLayout = view.findViewById(R.id.header);
        if(headerLayout !=null) {
            headerLayout.setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.title)).setText(title);
        }
        return this;
    }

    public StaxDialog setDialogMessage(int message) {
        setDialogMessage(context.getString(message));
        return this;
    }

    public StaxDialog setDialogMessage(String message) {
        TextView messageText = view.findViewById(R.id.message);

        if(messageText !=null){
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(message);
        }
        return this;
    }

    public StaxDialog setPosButton(int label, View.OnClickListener listener) {
        ((AppCompatButton) view.findViewById(R.id.pos_btn)).setText(context.getString(label));
        customPosListener = listener;
        view.findViewById(R.id.pos_btn).setOnClickListener(posListener);
        return this;
    }

    public StaxDialog setNegButton(int label, View.OnClickListener listener) {
        view.findViewById(R.id.neg_btn).setVisibility(View.VISIBLE);
        ((AppCompatButton) view.findViewById(R.id.neg_btn)).setText(context.getString(label));
        customNegListener = listener;
        view.findViewById(R.id.neg_btn).setOnClickListener(negListener);
        return this;
    }

    public StaxDialog isDestructive() {
        view.findViewById(R.id.pos_btn).getBackground()
                .setColorFilter(context.getResources().getColor(R.color.stax_state_red), PorterDuff.Mode.SRC);
        return this;
    }

    public StaxDialog highlightPos() {
        ((Button) view.findViewById(R.id.pos_btn)).setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        view.findViewById(R.id.pos_btn).getBackground()
                .setColorFilter(context.getResources().getColor(R.color.brightBlue), PorterDuff.Mode.SRC);
        return this;
    }

    public AlertDialog createIt() {
        return new AlertDialog.Builder(context, R.style.StaxDialog).setView(view).create();
    }

    public AlertDialog showIt() {
        if (dialog == null) dialog = createIt();
        dialog.show();
        return dialog;
    }

    public StaxDialog makeSticky() {
        if (dialog == null) dialog = createIt();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return this;
    }

    private final View.OnClickListener negListener = view -> {
        if (customNegListener != null)
            customNegListener.onClick(view);
        if (dialog != null)
            dialog.dismiss();
    };

    private final View.OnClickListener posListener = view -> {
        if (customPosListener != null)
            customPosListener.onClick(view);
        if (dialog != null)
            dialog.dismiss();
    };

    public View getView() {
        return view;
    }
}
