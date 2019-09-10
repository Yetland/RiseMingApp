package com.riseming.app

import android.app.Activity
import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var webView: WebView
    private lateinit var cl404: ConstraintLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)
        cl404 = findViewById(R.id.cl_404)
        webView.webViewClient = CustomWebViewClient(this) { a, b ->
            b?.apply {
                if (this.startsWith("net::")) {
                    cl404.visibility = View.VISIBLE
                    webView.visibility = View.GONE
                }
            }
        }
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

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
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
        private val activity: Activity,
        val callback: (Int, String?) -> Unit
    ) : WebViewClient() {
        private val schemeList = listOf("taobao", "tmall")
        private val schemeAppList = listOf("淘宝", "天猫")
        override fun onReceivedError(p0: WebView?, p1: Int, p2: String?, p3: String?) {
            LogUtils.logD("code = $p1 , description = $p2 , url = $p3")
            // net::ERR_INTERNET_DISCONNECTED
            callback(p1, p2)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            request?.apply {
                url?.apply {
                    LogUtils.logD(this.toString())
                    if (schemeList.contains(scheme)) {
                        AlertDialog.Builder(activity)
                            .setTitle("提示")
                            .setMessage("是否前往${schemeAppList[schemeList.indexOf(scheme)]},url = ${url}？")
                            .setPositiveButton(
                                "确定"
                            ) { p0, p1 ->
                                val intent = Intent()
                                intent.action = Intent.ACTION_VIEW
                                intent.data = url
                                try {
                                    activity.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(activity, "未安装app", Toast.LENGTH_SHORT).show()
                                    e.printStackTrace()
                                }
                            }
                            .setNegativeButton("取消", null)
                            .create()
                            .show()

                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }
}
