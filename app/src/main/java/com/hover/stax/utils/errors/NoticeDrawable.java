package com.hover.stax.utils.errors;

import com.hover.stax.R;

public class NoticeDrawable {
	public static int get(NoticeType type) {
		int drawable;
		switch (type) {
			case ERROR: drawable = R.drawable.ic_error_warning_24dp;
			break;
			case INFO: drawable =  R.drawable.ic_info_24dp;
			break;
			case SUCCESS: drawable = R.drawable.ic_success_check_circle_24;
			break;
			default: drawable = R.drawable.ic_warning_yellow_24;
			break;
		}
		return drawable;
	}
}
