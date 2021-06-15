package com.hover.stax.destruct;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.R;
import com.hover.stax.databinding.SelfDestructLayoutBinding;
import com.hover.stax.permissions.PermissionUtils;
import com.hover.stax.utils.Utils;

import java.util.Date;

public class SelfDestructActivity extends AppCompatActivity {

    private SelfDestructLayoutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SelfDestructLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.continueBtn.setOnClickListener(view -> Utils.openStaxPlaystorePage(this));
    }
}
