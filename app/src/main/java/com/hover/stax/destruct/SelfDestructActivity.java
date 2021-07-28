package com.hover.stax.destruct;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.databinding.SelfDestructLayoutBinding;
import com.hover.stax.utils.Utils;

public class SelfDestructActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SelfDestructLayoutBinding binding = SelfDestructLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.continueBtn.setOnClickListener(view -> Utils.openStaxPlaystorePage(this));
    }
}
