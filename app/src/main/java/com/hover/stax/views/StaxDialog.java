package com.hover.stax.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.hover.stax.R;

public class StaxDialog extends AlertDialog {

	protected Context context;
	protected View view;
	public AlertDialog dialog;

	protected View.OnClickListener customNegListener;
	protected View.OnClickListener customPosListener;

	public StaxDialog(@NonNull Activity a) {
		this(a, a.getLayoutInflater());
	}

	public StaxDialog(@NonNull Context c, Fragment frag) {
		this(c, frag.getLayoutInflater());
	}

	private StaxDialog(Context c, LayoutInflater inflater) {
		super(c);
		context = c;
		view = inflater.inflate(R.layout.stax_dialog, null);
		customNegListener = null;
		customPosListener = null;
	}

	public StaxDialog setDialogTitle(int title) {
		setDialogTitle(context.getString(title));
		return this;
	}

	public StaxDialog setDialogTitle(String title) {
		view.findViewById(R.id.header).setVisibility(View.VISIBLE);
		((TextView) view.findViewById(R.id.title)).setText(title);
		return this;
	}

	public StaxDialog setDialogMessage(int message) {
		setDialogMessage(context.getString(message));
		return this;
	}

	public StaxDialog setDialogMessage(String message) {
		view.findViewById(R.id.message).setVisibility(View.VISIBLE);
		((TextView) view.findViewById(R.id.message)).setText(message);
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
		dialog = createIt();
		dialog.show();
		return dialog;
	}

	private View.OnClickListener negListener = view -> {
		if (customNegListener != null)
			customNegListener.onClick(view);
		if (dialog != null)
			dialog.dismiss();
	};

	private View.OnClickListener posListener = view -> {
		if (customPosListener != null)
			customPosListener.onClick(view);
		if (dialog != null)
			dialog.dismiss();
	};

	public View getView() { return view; }
}
