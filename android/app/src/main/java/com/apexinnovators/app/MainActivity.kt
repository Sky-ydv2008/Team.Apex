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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apexinnovators.app.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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
        val BASE_URL: String = BuildConfig.START_URL
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
        setupAdminFeatures()
        checkForAppUpdate()
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
        val appVariant = if (BuildConfig.IS_ADMIN_APP) "ApexAdminAndroid/1.0.0" else "ApexInnovatorsAndroid/1.0.0"
        settings.userAgentString = settings.userAgentString + " " + appVariant + " ApexInnovators"

        // JavaScript interface for explicit app detection in frontend
        webView.addJavascriptInterface(WebAppInterface(), "ApexAndroid")
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
                view?.evaluateJavascript("window.isApexApp = true; if (typeof applyAppModeDOM === 'function') applyAppModeDOM();", null)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                view?.evaluateJavascript("window.isApexApp = true; if (typeof applyAppModeDOM === 'function') applyAppModeDOM();", null)
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

        // Internal platform navigation (GitHub Pages & Render live deployments) remains in WebView
        val host = uri.host ?: ""
        if (host == HOST || host.endsWith("onrender.com") || host.contains("apexinnovators")) {
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

    private fun setupAdminFeatures() {
        if (!BuildConfig.IS_ADMIN_APP) return

        binding.fabAdminAccounts.visibility = View.VISIBLE
        binding.fabAdminAccounts.setOnClickListener {
            showTeamAccountDialog()
        }
    }

    private fun showTeamAccountDialog() {
        val options = arrayOf(
            "Shivam Yadav (Admin — Full Access)",
            "Aryan Gupta (Core Member — Full Stack / Moderation)",
            "Lipsarani Bisoyi (Core Member — Frontend / Moderation)",
            "Custom Login (Enter credentials manually)"
        )

        AlertDialog.Builder(this)
            .setTitle("Core Team Access")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> switchAccount(1, "Shivam Yadav", "apex.innovator.team@gmail.com", "ADMIN", "Java Backend Developer")
                    1 -> switchAccount(3, "Aryan Gupta", "the.aryangupta10@gmail.com", "CORE_MEMBER", "Full Stack Developer")
                    2 -> switchAccount(2, "Lipsarani Bisoyi", "bisoyilipsarani@gmail.com", "CORE_MEMBER", "Frontend Developer")
                    3 -> binding.webView.loadUrl("https://sky-ydv2008.github.io/Team.Apex/login.html?next=admin%2Fdashboard.html")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun switchAccount(id: Int, name: String, email: String, role: String, headline: String) {
        val userJson = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("email", email)
            put("role", role)
            put("status", "ACTIVE")
            put("headline", headline)
            put("bio", "")
            put("github", if (id == 1) "Sky-ydv2008" else null)
            put("linkedin", null)
        }.toString()

        val redirectPage = if (role == "ADMIN") "admin/dashboard.html" else "admin/projects.html"
        val js = """
            localStorage.setItem('ai_token', 'demo-token-$id-${System.currentTimeMillis()}');
            localStorage.setItem('ai_user', JSON.stringify($userJson));
            window.location.assign('$redirectPage');
        """.trimIndent()

        binding.webView.evaluateJavascript(js, null)
        Toast.makeText(this, "Active: $name ($role)", Toast.LENGTH_SHORT).show()
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

    inner class WebAppInterface {
        @JavascriptInterface
        fun isApp(): Boolean = true

        @JavascriptInterface
        fun getAppVersion(): String = "1.0.0"
    }

    private fun checkForAppUpdate() {
        Thread {
            try {
                val url = URL("https://api.github.com/repos/Sky-ydv2008/Team.Apex/releases/latest")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("User-Agent", "ApexInnovatorsApp")
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val stream = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(stream)
                    val latestTag = json.optString("tag_name", "")
                    val currentVersion = "v" + BuildConfig.VERSION_NAME

                    if (latestTag.isNotEmpty() && latestTag != currentVersion) {
                        val apkName = if (BuildConfig.IS_ADMIN_APP) "ApexAdmin.apk" else "ApexInnovators.apk"
                        val downloadUrl = "https://github.com/Sky-ydv2008/Team.Apex/releases/latest/download/$apkName"

                        runOnUiThread {
                            showUpdateAvailableDialog(latestTag, downloadUrl)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore network errors during update check
            }
        }.start()
    }

    private fun showUpdateAvailableDialog(newVersion: String, downloadUrl: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("App Update Available ($newVersion)")
            .setMessage("A new update ($newVersion) for ${if (BuildConfig.IS_ADMIN_APP) "Apex Admin" else "Apex Innovators"} is available with bug fixes and features. Download and install now?")
            .setPositiveButton("Update Now") { _, _ ->
                try {
                    val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                        setMimeType("application/vnd.android.package-archive")
                        addRequestHeader("User-Agent", "ApexInnovatorsApp")
                        setDescription("Downloading latest update ($newVersion)...")
                        setTitle(if (BuildConfig.IS_ADMIN_APP) "ApexAdmin.apk" else "ApexInnovators.apk")
                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            if (BuildConfig.IS_ADMIN_APP) "ApexAdmin.apk" else "ApexInnovators.apk"
                        )
                    }
                    val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.enqueue(request)
                    Toast.makeText(this, "Downloading update... Check notification bar.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not start update download: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Later", null)
            .show()
    }
}
