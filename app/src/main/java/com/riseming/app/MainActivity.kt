package com.riseming.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.designpatterns.R
import com.riseming.app.utils.LogUtils
import com.tencent.smtt.export.external.interfaces.WebResourceRequest
import com.tencent.smtt.sdk.WebSettings
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var webView: WebView
    private lateinit var cl404: ConstraintLayout
    private lateinit var dialogBuilder: AlertDialog.Builder
    private var dialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)
        cl404 = findViewById(R.id.cl_404)

        dialogBuilder = AlertDialog.Builder(this@MainActivity)
            .setTitle("${getString(R.string.app_name)}温馨提示")
            .setCancelable(false)
            .setNegativeButton("取消", null)
        webView.webViewClient = CustomWebViewClient(object : Callback {
            override fun onError(p1: Int, p2: String?) {
                p2?.apply {
                    if (this.startsWith("net::")) {
                        cl404.visibility = View.VISIBLE
                        webView.visibility = View.GONE
                    }
                }
            }

            override fun onInterceptor(s: String, url: Uri) {
                if (dialog == null) {
                    showDialog(s, url)
                } else if (!dialog!!.isShowing) {
                    showDialog(s, url)
                }
            }
        })
//        swipeRefreshLayout.setOnRefreshListener {
//            webView.reload()
//            GlobalScope.launch {
//                delay(2000)
//                swipeRefreshLayout.isRefreshing = false
//            }
//        }
        cl404.setOnClickListener {
            cl404.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.loadUrl(URL)
        }
        initWebViewSetting(webView)
        webView.loadUrl(URL)
        findViewById<View>(R.id.iv_back).setOnClickListener {
            onBackPressed()
        }
        findViewById<View>(R.id.iv_reload).setOnClickListener {
            cl404.visibility = View.GONE
            webView.visibility = View.VISIBLE
            webView.reload()
        }
        findViewById<View>(R.id.iv_close).setOnClickListener {
            finish()
        }
    }

    private fun showDialog(s: String, url: Uri) {
        dialogBuilder.setMessage("是否前往“$s”购买该商品？")
            .setPositiveButton(
                "立刻前往"
            ) { _, _ ->
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.data = url
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "未安装“$s”app", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

        dialog = dialogBuilder.create()
        dialog?.show()
    }

    private fun initWebViewSetting(webView: WebView) {
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true  // 设置支持JavaScript脚本
        webSettings.useWideViewPort = true // 打开页面时， 自适应屏幕
        webSettings.allowFileAccess = true  // 设置可以访问文件
        webSettings.useWideViewPort = true // 打开页面时， 自适应屏幕
        webSettings.loadWithOverviewMode = true// 打开页面时， 自适应屏幕
        webSettings.loadsImagesAutomatically = true  // 支持自动加载图片
        webSettings.domStorageEnabled = true
        webSettings.setAppCacheEnabled(true)
        webSettings.setAppCacheMaxSize(1024 * 1024 * 20)
        webSettings.setAppCachePath(filesDir.absolutePath + "cache/")
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT  // 设置缓冲模式
        webSettings.textSize = WebSettings.TextSize.NORMAL
    }

    private var last: Long = 0
    private var exit = false

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            val interval = System.currentTimeMillis() - last
            if (exit && interval < 2000) {
                super.onBackPressed()
            } else {
                exit = true
                last = System.currentTimeMillis()
                Toast.makeText(this, "再按一次返回退出APP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.clearCache(true)
    }

    companion object {
        private const val URL = "http://zhe.riseming.cn/"
    }

    internal class CustomWebViewClient(
        private val callback: Callback
    ) : WebViewClient() {
        private val schemeList = listOf("tmall")
        private val schemeAppList = listOf("天猫")
        override fun onReceivedError(p0: WebView?, p1: Int, p2: String?, p3: String?) {
            LogUtils.logD("code = $p1 , description = $p2 , url = $p3")
            // net::ERR_INTERNET_DISCONNECTED
            callback.onError(p1, p2)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.apply {
                url?.apply {
                    LogUtils.logD(this.toString())
                    if (schemeList.contains(scheme)) {
                        callback.onInterceptor(
                            schemeAppList[schemeList.indexOf(scheme)],
                            url
                        )
                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    interface Callback {
        fun onError(p1: Int, p2: String?)
        fun onInterceptor(s: String, url: Uri)
    }
}
