package com.hover.stax.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hover.stax.databinding.LibraryListItemBinding;

class LibraryListItem extends LinearLayout {

	LibraryListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		LibraryListItemBinding.inflate(LayoutInflater.from(context), this, true);
	}

	public interface DialListener {
		void dial(String shortCode);
	}
}
