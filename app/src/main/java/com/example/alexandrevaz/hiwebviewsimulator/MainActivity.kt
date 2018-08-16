package com.example.alexandrevaz.hiwebviewsimulator

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var uploadMessage: ValueCallback<Array<Uri>>? = null
    var mUploadMessage: ValueCallback<Uri>? = null
    var bypassLocalSSL: Boolean = true

    companion object {
        @JvmField val REQUEST_SELECT_FILE: Int = 100
        @JvmField val FILECHOOSER_RESULT_CODE: Int = 1
    }

    @SuppressLint("SetJavaScriptEnabled", "InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // val botUrl = "${getString(R.string.base_url)}/dist/index.html#!/?token=${getString(R.string.token)}${getString(R.string.bot_params)}"
        val botUrl = "file:///android_asset/chat.html"
        val webViewSettings: WebSettings = webView.settings

        webView.loadUrl(botUrl)
        /**
         * Set of WebView permissions needed for HiBot to work
         */
        webViewSettings.javaScriptEnabled = true
        webViewSettings.domStorageEnabled = true
        webViewSettings.allowFileAccess = true

        webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val reqUrl = request?.url

                if (reqUrl.toString() != botUrl) {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = reqUrl
                    startActivity(i)
                    return true
                } else {
                    return super.shouldOverrideUrlLoading(view, request)
                }

            }

            // Since we have problems dealing with certificates on QA and Localhost,
            // we need this to bypass SSL errors
            // If you need to debug any possible SSL problems, set this flag to false
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                if (bypassLocalSSL) {
                    handler!!.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }
        }

        /**
         * Since Android 6.0, permissions considered "Dangerous" are no longer granted
         * at install time.
         * Due to these changes, and considering that users can manually revoke permissions
         * granted before, it's important that we always check their validity and ask the user for
         * access.
         * This is OPTIONAL and it's only use case is for users who need FileUpload block
         * or have enabled AudioMessages
         */
        // File upload permissions
        val camPermission = Manifest.permission.CAMERA
        val wePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val rePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        // Audio recording permissions
        val audioPerm = Manifest.permission.RECORD_AUDIO
        val audioStPerm = Manifest.permission.MODIFY_AUDIO_SETTINGS

        val grantedCam = ContextCompat.checkSelfPermission(this, camPermission)
        val grantedWe = ContextCompat.checkSelfPermission(this, wePermission)
        val grantedRe = ContextCompat.checkSelfPermission(this, rePermission)
        val grantedAudio = ContextCompat.checkSelfPermission(this, audioPerm)
        val grantedAudioSt = ContextCompat.checkSelfPermission(this, audioStPerm)

        if (Build.VERSION.SDK_INT >= 23 && (grantedAudio != PackageManager.PERMISSION_GRANTED
                || grantedAudioSt != PackageManager.PERMISSION_GRANTED
                || grantedCam != PackageManager.PERMISSION_GRANTED
                || grantedWe != PackageManager.PERMISSION_GRANTED
                || grantedRe != PackageManager.PERMISSION_GRANTED)){
            val permissionList = arrayOf(audioPerm, audioStPerm, camPermission, wePermission, rePermission)
            ActivityCompat.requestPermissions(this, permissionList, 1)
        }

        WebView.setWebContentsDebuggingEnabled(true)
        /*
        if (Build.VERSION.SDK_INT >= 21) {
            webViewSettings.mixedContentMode = 0
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }*/

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
                this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULT_CODE)
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
                this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULT_CODE)
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
                this@MainActivity.startActivityForResult(Intent.createChooser(i, "File Chooser"), MainActivity.FILECHOOSER_RESULT_CODE)
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
                    startActivityForResult(chooserIntent, REQUEST_SELECT_FILE)
                } catch (e: Exception) {
                    uploadMessage = null
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG)
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
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) {
                    return
                }
                uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULT_CODE) {
            if (mUploadMessage == null) {
                return
            }

            var result: Uri? = null
            if (intent != null || resultCode == RESULT_OK) {
                result = intent?.data
            }

            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_LONG)
                    .show()
        }
    }
}
