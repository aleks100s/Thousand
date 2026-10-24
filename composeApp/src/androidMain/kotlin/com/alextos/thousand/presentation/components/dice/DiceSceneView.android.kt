package com.alextos.thousand.presentation.components.dice

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContextWrapper
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun DiceSceneView(
    html: String,
    configuration: DiceSceneConfiguration,
    onEvent: (DiceSceneEvent) -> Unit,
    modifier: Modifier,
) {
    val latestEvent = rememberUpdatedState(onEvent)
    val latestConfiguration = rememberUpdatedState(configuration)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.TRANSPARENT)
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                isFocusable = false
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = false
                    allowFileAccess = false
                    allowContentAccess = false
                    blockNetworkLoads = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                }
                val view = this
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun postMessage(message: String) {
                        view.post {
                            decodeDiceSceneEvent(message)?.let { event ->
                                if (event == DiceSceneEvent.Ready) {
                                    view.tag = true
                                    view.evaluateJavascript(latestConfiguration.value.script(), null)
                                }
                                latestEvent.value(event)
                            }
                        }
                    }
                }, "DiceNative")
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = true
                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse =
                        WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        view?.tag = false
                        latestEvent.value(DiceSceneEvent.Error(null))
                        return true
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        if (request?.isForMainFrame == true) latestEvent.value(DiceSceneEvent.Error(null))
                    }
                }
                loadDataWithBaseURL("https://dice.thousand.invalid/", html, "text/html", "UTF-8", null)
            }
        },
        update = { view ->
            if (configuration.active) view.onResume() else view.onPause()
            if (view.tag == true) view.evaluateJavascript(configuration.script(), null)
        },
        onRelease = { view ->
            view.removeJavascriptInterface("DiceNative")
            view.stopLoading()
            val root = generateSequence(view.context) { (it as? ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>().firstOrNull()?.window?.decorView ?: view.rootView
            view.destroy()
            // WebView removal can leave stale damage regions in a hardware-accelerated host.
            root.post {
                root.requestLayout()
                root.invalidate()
            }
        },
    )
}
