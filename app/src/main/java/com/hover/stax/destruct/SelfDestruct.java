package com.hover.stax.destruct;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.R;
import com.hover.stax.utils.Utils;

import java.util.Date;

public class SelfDestruct extends AppCompatActivity {
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.self_destruct_layout);
	}

	public static boolean isTime(Context c) {
		long currentTime = new Date().getTime();
		long selfDestructTime = Long.parseLong(Utils.getBuildConfigValue(c, "SELF_DESTRUCT").toString());
		return  currentTime <= selfDestructTime;
	}

	public void downloadLatest(View view) {

	}
}
