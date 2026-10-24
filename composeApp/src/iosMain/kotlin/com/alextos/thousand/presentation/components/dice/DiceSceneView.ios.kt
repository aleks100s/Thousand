@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.alextos.thousand.presentation.components.dice

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

private class SceneHandler : NSObject(), WKScriptMessageHandlerProtocol, WKNavigationDelegateProtocol {
    var onEvent: (DiceSceneEvent) -> Unit = {}
    var configuration: DiceSceneConfiguration? = null
    var webView: WKWebView? = null
    var ready = false
    private var initialNavigationLoaded = false

    override fun userContentController(userContentController: WKUserContentController, didReceiveScriptMessage: WKScriptMessage) {
        if (!didReceiveScriptMessage.frameInfo.mainFrame) return
        val event = decodeDiceSceneEvent(didReceiveScriptMessage.body as? String ?: return) ?: return
        if (event == DiceSceneEvent.Ready) {
            ready = true
            configuration?.let { webView?.evaluateJavaScript(it.script(), null) }
        }
        onEvent(event)
    }

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        // loadHTMLString's initial document is the only permitted navigation.
        val url = decidePolicyForNavigationAction.request.URL?.absoluteString
        val initial = !initialNavigationLoaded && (url == "about:blank" || url == "https://dice.thousand.invalid/")
        if (initial) initialNavigationLoaded = true
        decisionHandler(if (initial) WKNavigationActionPolicy.WKNavigationActionPolicyAllow else WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
    }

    override fun webViewWebContentProcessDidTerminate(webView: WKWebView) {
        onEvent(DiceSceneEvent.Error(null))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun DiceSceneView(
    html: String,
    configuration: DiceSceneConfiguration,
    onEvent: (DiceSceneEvent) -> Unit,
    modifier: Modifier,
) {
    val latestEvent = rememberUpdatedState(onEvent)
    val handler = remember { SceneHandler() }
    handler.onEvent = { latestEvent.value(it) }
    handler.configuration = configuration
    UIKitView(
        modifier = modifier,
        factory = {
            val settings = WKWebViewConfiguration()
            settings.userContentController.addScriptMessageHandler(handler, "diceScene")
            WKWebView(frame = CGRectZero.readValue(), configuration = settings).apply {
                handler.webView = this
                navigationDelegate = handler
                scrollView.scrollEnabled = false
                scrollView.bounces = false
                scrollView.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
                userInteractionEnabled = true
                loadHTMLString(html, baseURL = NSURL(string = "https://dice.thousand.invalid/"))
            }
        },
        update = { view ->
            if (handler.ready) view.evaluateJavaScript(configuration.script(), null)
        },
        onRelease = { view ->
            view.configuration.userContentController.removeScriptMessageHandlerForName("diceScene")
            view.navigationDelegate = null
            view.stopLoading()
            handler.webView = null
            handler.onEvent = {}
        },
        properties = UIKitInteropProperties(isNativeAccessibilityEnabled = true),
    )
}
