package com.playdevsgame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HelpBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_help_bottom_sheet, container, false)
        webView = view.findViewById(R.id.webview)
        // webView.settings.javaScriptEnabled = true // Si tu página necesita JavaScript
        webView.loadUrl("https://sites.google.com/uoc.edu/playdev/ayuda")
        return view
    }

    companion object {
        fun newInstance(): HelpBottomSheetFragment {
            return HelpBottomSheetFragment()
        }
    }
}
