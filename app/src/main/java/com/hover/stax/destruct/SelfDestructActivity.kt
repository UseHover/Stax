package com.hover.stax.destruct

import com.hover.stax.utils.Utils.openStaxPlaystorePage
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.hover.stax.databinding.SelfDestructLayoutBinding

class SelfDestructActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = SelfDestructLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueBtn.setOnClickListener { openStaxPlaystorePage(this) }
    }
}