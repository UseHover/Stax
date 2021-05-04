package com.hover.stax.views;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.hover.stax.R;
import com.hover.stax.databinding.StaxDialogBinding;

public class StaxDialog extends AlertDialog {

	protected Context context;
	protected View view;
	public AlertDialog dialog;

	protected View.OnClickListener customNegListener;
	protected View.OnClickListener customPosListener;

	private StaxDialogBinding binding;

	public StaxDialog(Context c) {
		super(c);
		context = c;

		binding = StaxDialogBinding.inflate(LayoutInflater.from(context));

		customNegListener = null;
		customPosListener = null;
	}

	public StaxDialog setDialogTitle(int title) {
		setDialogTitle(context.getString(title));
		return this;
	}

	public StaxDialog setDialogTitle(String title) {
		binding.header.setVisibility(View.VISIBLE);
		binding.title.setText(title);
		return this;
	}

	public StaxDialog setDialogMessage(int message) {
		setDialogMessage(context.getString(message));
		return this;
	}

	public StaxDialog setDialogMessage(String message) {
		binding.message.setVisibility(View.VISIBLE);
		binding.message.setText(message);
		return this;
	}

	public StaxDialog setPosButton(int label, View.OnClickListener listener) {
		binding.posBtn.setText(context.getString(label));
		customPosListener = listener;
		binding.posBtn.setOnClickListener(posListener);
		return this;
	}

	public StaxDialog setNegButton(int label, View.OnClickListener listener) {
		binding.negBtn.setVisibility(View.VISIBLE);
		binding.negBtn.setText(context.getString(label));
		customNegListener = listener;
		binding.negBtn.setOnClickListener(negListener);
		return this;
	}

	public StaxDialog isDestructive() {
		binding.posBtn.getBackground()
				.setColorFilter(context.getResources().getColor(R.color.stax_state_red), PorterDuff.Mode.SRC);
		return this;
	}

	public StaxDialog highlightPos() {
		binding.posBtn.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
		binding.posBtn.getBackground()
			.setColorFilter(context.getResources().getColor(R.color.brightBlue), PorterDuff.Mode.SRC);
		return this;
	}

	public AlertDialog createIt() {
		return new AlertDialog.Builder(context, R.style.StaxDialog).setView(binding.getRoot()).create();
	}

	public AlertDialog showIt() {
		dialog = createIt();
		dialog.show();
		return dialog;
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

	public View getView() { return view; }
}
