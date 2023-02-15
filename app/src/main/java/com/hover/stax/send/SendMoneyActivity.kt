package com.hover.stax.send

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Surface
import androidx.core.view.WindowCompat
import com.hover.stax.navigation.MainNavHost

class SendMoneyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            Surface {
                MainNavHost()
            }
        }
    }
}