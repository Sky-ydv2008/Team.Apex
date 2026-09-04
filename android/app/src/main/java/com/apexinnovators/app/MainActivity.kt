package com.apexinnovators.app

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apexinnovators.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var backPressedTime: Long = 0

    private val filePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (fileUploadCallback != null) {
                val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
                fileUploadCallback?.onReceiveValue(uris)
                fileUploadCallback = null
            }
        }

    companion object {
        const val BASE_URL = "https://sky-ydv2008.github.io/Team.Apex/"
        const val HOST = "sky-ydv2008.github.io"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch from Splash theme to primary app theme
        setTheme(R.style.Theme_ApexInnovators)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSwipeRefresh()
        setupWebView()
        setupOfflineHandling()
        setupBackNavigation()

        if (savedInstanceState != null) {
            binding.webView.restoreState(savedInstanceState)
        } else {
            loadWebsite()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView
        val settings = webView.settings

        // Enable JavaScript, DOM storage (required for localStorage sessions), and database
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.setSupportZoom(false)

        // User agent branding
        settings.userAgentString = settings.userAgentString + " ApexInnovatorsAndroid/1.0.0"

        // Cache policy: use cache if network is unavailable
        settings.cacheMode = if (isNetworkConnected()) {
            WebSettings.LOAD_DEFAULT
        } else {
            WebSettings.LOAD_CACHE_ELSE_NETWORK
        }

        // Enable cookies
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        // Set WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleUrl(url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                showWebView()
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true && !isNetworkConnected()) {
                    showOfflineScreen()
                }
            }
        }

        // Set WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                }
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }

                try {
                    filePickerLauncher.launch(intent)
                } catch (e: Exception) {
                    fileUploadCallback = null
                    return false
                }
                return true
            }
        }

        // Download listener for files/certificates
        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimeType)
                    addRequestHeader("User-Agent", userAgent)
                    setDescription("Downloading file from Apex Innovators...")
                    setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        URLUtil.guessFileName(url, contentDisposition, mimeType)
                    )
                }
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "Downloading file...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Could not start download: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleUrl(url: String): Boolean {
        val uri = Uri.parse(url)

        // Internal platform navigation remains in WebView
        if (uri.host == HOST || (uri.scheme == "https" && uri.host?.contains("apexinnovators") == true)) {
            return false
        }

        // Handle protocols like tel:, mailto:, sms:, whatsapp:
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:") || url.startsWith("whatsapp:")) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                Toast.makeText(this, "No application found to handle this link", Toast.LENGTH_SHORT).show()
            }
            return true
        }

        // External URLs (GitHub, LinkedIn, Render, etc.) open in external browser
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent_cyan))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.bg_surface))
        binding.swipeRefresh.setOnRefreshListener {
            if (isNetworkConnected()) {
                binding.webView.reload()
            } else {
                binding.swipeRefresh.isRefreshing = false
                showOfflineScreen()
            }
        }
    }

    private fun setupOfflineHandling() {
        binding.btnRetry.setOnClickListener {
            if (isNetworkConnected()) {
                showWebView()
                binding.webView.reload()
            } else {
                Toast.makeText(this, "Still offline. Please check connection.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.layoutOffline.visibility == View.VISIBLE) {
                    finish()
                    return
                }

                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, getString(R.string.exit_prompt), Toast.LENGTH_SHORT).show()
                        backPressedTime = System.currentTimeMillis()
                    }
                }
            }
        })
    }

    private fun loadWebsite() {
        if (isNetworkConnected()) {
            showWebView()
            binding.webView.loadUrl(BASE_URL)
        } else {
            showOfflineScreen()
        }
    }

    private fun showWebView() {
        binding.layoutOffline.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
    }

    private fun showOfflineScreen() {
        binding.swipeRefresh.visibility = View.GONE
        binding.layoutOffline.visibility = View.VISIBLE
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }
}
