package com.example.mockcrudwebview

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class WebAppInterface(private val context: Context, private val webView: WebView) {

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun showDialog(title: String, message: String, callback: String) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("Eliminar") { _, _ ->
                // Llamar a la función JS callback
                (context as Activity).runOnUiThread {
                    webView.evaluateJavascript("$callback()", null)
                }
            }
            setNegativeButton("Cancelar", null)
            create().show()
        }
    }
}