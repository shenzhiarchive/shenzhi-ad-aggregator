package com.shenzhi.adaggregator.banner

import android.content.Context
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.uimanager.events.RCTModernEventEmitter
import com.facebook.react.views.view.ReactViewGroup

/**
 * Banner广告Fabric Component
 * 用于在React Native Fabric架构中管理BannerAdView
 */
class BannerAdViewComponent(context: Context) : ReactViewGroup(context) {

    private val bannerAdView: BannerAdView = BannerAdView(context)

  init {

      // 设置布局参数，确保BannerAdView能够正确显示
      val layoutParams = LayoutParams(
          LayoutParams.MATCH_PARENT,
          LayoutParams.WRAP_CONTENT
      )
      bannerAdView.layoutParams = layoutParams

      addView(bannerAdView)

      // 设置事件回调
      setupEventCallbacks()
    }

    /**
     * 设置事件回调
     */
    private fun setupEventCallbacks() {
        bannerAdView.setOnAdClickedCallback {
            sendEvent("onAdClicked", null)
        }

        bannerAdView.setOnAdShowCallback {
            sendEvent("onAdShow", null)
        }

        bannerAdView.setOnRenderFailCallback { code, msg ->
            val eventData = Arguments.createMap()
            eventData.putInt("code", code)
            eventData.putString("message", msg)
            sendEvent("onRenderFail", eventData)
        }

        bannerAdView.setOnRenderSuccessCallback { width, height ->
            val eventData = Arguments.createMap()
            eventData.putDouble("width", width.toDouble())
            eventData.putDouble("height", height.toDouble())
            sendEvent("onRenderSuccess", eventData)
        }

        bannerAdView.setOnDislikeCallback { position, value ->
            val eventData = Arguments.createMap()
            eventData.putInt("position", position)
            eventData.putString("value", value)
            sendEvent("onDislike", eventData)
        }

        bannerAdView.setOnErrorCallback { code, msg ->
            val eventData = Arguments.createMap()
            eventData.putInt("code", code)
            eventData.putString("message", msg)
            sendEvent("onError", eventData)
        }

        bannerAdView.setOnEcpmInfoCallback { ecpmInfo ->
            val eventData = Arguments.createMap()
            ecpmInfo.forEach { (key, value) ->
                when (value) {
                    is String -> eventData.putString(key, value)
                    is Int -> eventData.putInt(key, value)
                    is Long -> eventData.putDouble(key, value.toDouble())
                    is Double -> eventData.putDouble(key, value)
                    is Float -> eventData.putDouble(key, value.toDouble())
                    is Boolean -> eventData.putBoolean(key, value)
                    null -> eventData.putNull(key)
                    else -> eventData.putString(key, value.toString())
                }
            }
            sendEvent("onEcpmInfo", eventData)
        }
    }

    /**
     * 设置广告位ID
     */
    fun setCodeId(codeId: String?) {
        bannerAdView.setCodeId(codeId)
    }

    /**
     * 设置广告尺寸
     */
    fun setAdSize(adSize: ReadableMap?) {
        if (adSize != null && adSize.hasKey("width") && adSize.hasKey("height")) {
            val width = adSize.getDouble("width").toFloat()
            val height = adSize.getDouble("height").toFloat()
            bannerAdView.setAdSize(width, height)
        }
    }

    /**
     * 加载广告
     */
    fun loadAd() {
        bannerAdView.loadAd()
    }

    /**
     * 销毁广告
     */
    fun destroy() {
        bannerAdView.destroyAd()
    }

    /**
     * 检查广告是否已加载
     */
    fun isAdLoaded(): Boolean {
        return bannerAdView.isAdLoaded()
    }

    /**
     * 发送事件到React Native
     */
    private fun sendEvent(eventName: String, params: WritableMap?) {
        val reactContext = context as? com.facebook.react.bridge.ReactContext
        reactContext?.let { ctx ->
            ctx.getJSModule(RCTModernEventEmitter::class.java)
                .receiveEvent(id, eventName, params ?: Arguments.createMap())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 组件从窗口分离时销毁广告
        bannerAdView.destroyAd()
    }
}

