package com.hover.stax.requests;

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

public abstract class AbstractMessageSendingActivity extends AppCompatActivity {
	public Request currentRequest;
	public List<StaxContact> requestees;
	public Channel channel;

	public void sendSms(View view) {
		if (preventedError()) return;
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_VIEW);
		sendIntent.setData(Uri.parse("smsto:" + currentRequest.generateRecipientString(requestees)));
		sendIntent.putExtra(Intent.EXTRA_TEXT, currentRequest.generateMessage(this));
		sendIntent.putExtra("sms_body", currentRequest.generateMessage(this));
		startActivityForResult(Intent.createChooser(sendIntent, "Request"), Constants.SMS);
	}

	public void sendWhatsapp(View view) {
		if (preventedError()) return;
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_VIEW);

		String whatsapp = "https://api.whatsapp.com/send?phone=" + currentRequest.generateWhatsappRecipientString(requestees, channel) + "&text=" + currentRequest.generateMessage(this);
		sendIntent.setData(Uri.parse(whatsapp));
		startActivityForResult(sendIntent, Constants.SMS);
	}

	public void copyShareLink(View view) {
		if (preventedError()) return;
		TextView copyBtn = view.findViewById(R.id.copylink_share_selection);
		if (Utils.copyToClipboard(currentRequest.generateMessage(this), this)) {
			copyBtn.setActivated(true);
			copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.img_check), null, null);
			copyBtn.setText(getString(R.string.link_copied_label));
		} else {
			copyBtn.setActivated(false);
			copyBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.img_copy), null, null);
			copyBtn.setText(getString(R.string.copyLink_label));
		}
	}

	private boolean preventedError() {
		if (currentRequest == null || requestees == null || channel == null) {
			UIHelper.flashMessage(this, "Something went wrong, please try again.");
			return true;
		}
		return false;
	}
}
