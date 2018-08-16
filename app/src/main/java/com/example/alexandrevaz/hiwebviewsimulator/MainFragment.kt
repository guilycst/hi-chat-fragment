package com.example.alexandrevaz.hiwebviewsimulator

import android.annotation.TargetApi
import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast

class MainFragment : Fragment() {

    var uploadMessage: ValueCallback<Array<Uri>>? = null
    var mUploadMessage: ValueCallback<Uri>? = null
    var bypassLocalSSL: Boolean = true

    companion object {
        @JvmField val REQUEST_SELECT_FILE: Int = 100
        @JvmField val FILECHOOSER_RESULT_CODE: Int = 1
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater!!.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView: WebView = view!!.findViewById(R.id.webView)
        val url = "https://www8.directtalk.com.br/chat31/?idd=F56A01109475102023A5&nome=Hiplatform Hiplatform&email=hiplatform@teste.com&telefone=11111&origem=Android"
        val settings: WebSettings = webView.settings

        webView.loadUrl(url)

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                if (bypassLocalSSL) {
                    handler!!.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)

        webView.webChromeClient = object: WebChromeClient() {
            /**
             * File upload for Android HoneyComb (3.X) versions
             * This is a fallback signature method from some legacy HoneyComb versions
             */
            fun openFileChooser(uploadMsg: ValueCallback<Uri>) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                this@MainFragment.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainFragment.FILECHOOSER_RESULT_CODE)
            }

            /**
             * File upload for Android HoneyComb (3.X) versions
             */
            // Some Android 3.0+ versions use a different method signature
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                this@MainFragment.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainFragment.FILECHOOSER_RESULT_CODE)
            }

            /**
             * File upload for Android JellyBean (4.1) versions
             */
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
            fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {

                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "*/*"
                this@MainFragment.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainFragment.FILECHOOSER_RESULT_CODE)
            }

            /**
             * File upload for Android Lollipop (5.0) or higher versions
             */
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                }

                uploadMessage = filePathCallback

                val contentSelection = Intent(Intent.ACTION_GET_CONTENT)
                contentSelection.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelection.type = "*/*"

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelection)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose your file")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(contentSelection))

                try {
                    startActivityForResult(chooserIntent, MainFragment.REQUEST_SELECT_FILE)
                } catch (e: Exception) {
                    uploadMessage = null
                    Toast.makeText(this@MainFragment.context, e.message, Toast.LENGTH_LONG)
                            .show()
                    return false

                }

                return true
            }

            /**
             * Debugging utils
             */
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d("[WebView JS Console]", consoleMessage?.message())
                return super.onConsoleMessage(consoleMessage)
            }

            /**
             * Neccessary for WebView audio capture permission
             * This REQUIRES Android Lollipop(5.0) version or higher
             * Lower versions of Android WebView doesn't support getUserMedia
             */
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request?.resources!!.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                    request.grant(request.resources)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == MainFragment.REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return
                }
                uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
                uploadMessage = null
            }
        } else if (requestCode == MainFragment.FILECHOOSER_RESULT_CODE) {
            if (mUploadMessage == null) {
                return
            }

            var result: Uri? = null
            if (intent != null || resultCode == AppCompatActivity.RESULT_OK) {
                result = intent?.data
            }

            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else {
            Toast.makeText(this.context, "Failed to upload image", Toast.LENGTH_LONG)
                    .show()
        }
    }
}
