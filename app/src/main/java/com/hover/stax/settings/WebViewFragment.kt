package com.hover.stax.settings

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.hover.stax.R
import com.hover.stax.databinding.WebviewFragmentBinding

class WebViewFragment : Fragment() {
    var binding: WebviewFragmentBinding? = null;
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WebviewFragmentBinding.inflate(inflater, container, false)
        loadWebView()
        return binding?.root
    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView() {
        val url : String = arguments?.getString("url") ?: ""
        val webview : WebView? = binding?.webview
        webview?.getSettings()?.setJavaScriptEnabled(true);
        webview?.loadUrl(url)
        webview?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                binding?.webview?.visibility = View.VISIBLE
                binding?.cardRoot?.setTitle(getString(R.string.request_feature))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null;
    }
}