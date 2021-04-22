package com.hover.stax.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.hover.stax.R;
import com.hover.stax.databinding.StaxDialogBinding;

public class StaxDialog extends AlertDialog {

	protected Context context;
	protected View view;
	public AlertDialog dialog;

	protected View.OnClickListener customNegListener;
	protected View.OnClickListener customPosListener;

	private StaxDialogBinding binding;

	public StaxDialog(@NonNull Activity a) {
		this(a, a.getLayoutInflater());
	}

	public StaxDialog(@NonNull Context c, Fragment frag) {
		this(c, frag.getLayoutInflater());
	}

	private StaxDialog(Context c, LayoutInflater inflater) {
		super(c);
		context = c;

		binding = StaxDialogBinding.inflate(LayoutInflater.from(context));
//		view = inflater.inflate(R.layout.stax_dialog, null);
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
