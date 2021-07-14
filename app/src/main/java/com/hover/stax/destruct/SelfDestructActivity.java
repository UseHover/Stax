package com.hover.stax.destruct;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hover.stax.databinding.SelfDestructLayoutBinding;

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
