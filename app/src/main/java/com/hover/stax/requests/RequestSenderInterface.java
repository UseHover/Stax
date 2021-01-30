package com.hover.stax.requests;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.R;
import com.hover.stax.channels.Channel;
import com.hover.stax.contacts.StaxContact;
import com.hover.stax.database.Constants;
import com.hover.stax.utils.UIHelper;
import com.hover.stax.utils.Utils;

import java.util.List;

public interface RequestSenderInterface {

	default void sendSms(Request r, List<StaxContact> requestees, Activity a) {
		if (r == null || requestees == null) { showError(a); return; }
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("smsto:" + r.generateRecipientString(requestees)));
		sendIntent.putExtra(Intent.EXTRA_TEXT, r.generateMessage(a));
		sendIntent.putExtra("sms_body", r.generateMessage(a));
		a.startActivityForResult(Intent.createChooser(sendIntent, "Request"), Constants.SMS);
	}

	default void sendWhatsapp(Request r, List<StaxContact> requestees, Channel channel, Activity a) {
		if (r == null || requestees == null) { showError(a); return; }
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_VIEW);

		String whatsapp = "https://api.whatsapp.com/send?phone=" + r.generateWhatsappRecipientString(requestees, channel) + "&text=" + r.generateMessage(a);
		sendIntent.setData(Uri.parse(whatsapp));
		a.startActivityForResult(sendIntent, Constants.SMS);
	}

	default void copyShareLink(Request r, TextView copyBtn, Activity a) {
		if (r == null) showError(a);
		if (Utils.copyToClipboard(r.generateMessage(a), a)) {
			copyBtn.setActivated(true);
			copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, a.getResources().getDrawable(R.drawable.img_check), null, null);
			copyBtn.setText(a.getString(R.string.link_copied_label));
		} else {
			copyBtn.setActivated(false);
			copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, a.getResources().getDrawable(R.drawable.img_copy), null, null);
			copyBtn.setText(a.getString(R.string.copyLink_label));
		}
	}

	default void showError(Activity a) {
		UIHelper.flashMessage(a, a.getString(R.string.loading_dialoghead));
	}
}
