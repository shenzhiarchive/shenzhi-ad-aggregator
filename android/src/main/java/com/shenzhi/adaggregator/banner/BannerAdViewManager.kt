package com.shenzhi.adaggregator.banner

import android.view.View
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.uimanager.events.RCTEventEmitter

/**
 * Banner广告ViewManager
 * 用于在React Native中管理BannerAdView
 */
class BannerAdViewManager : SimpleViewManager<BannerAdView>() {
    
    companion object {
        const val REACT_CLASS = "ShenzhiBannerAdView"
        
        // 命令ID
        private const val COMMAND_LOAD_AD = 1
        private const val COMMAND_DESTROY = 2
        private const val COMMAND_IS_AD_LOADED = 3
        
        // 事件名称
        const val EVENT_AD_CLICKED = "onAdClicked"
        const val EVENT_AD_SHOW = "onAdShow"
        const val EVENT_RENDER_FAIL = "onRenderFail"
        const val EVENT_RENDER_SUCCESS = "onRenderSuccess"
        const val EVENT_DISLIKE = "onDislike"
        const val EVENT_ERROR = "onError"
        const val EVENT_ECPM_INFO = "onEcpmInfo"
    }
    
    override fun getName(): String {
        return REACT_CLASS
    }
    
    override fun createViewInstance(reactContext: ThemedReactContext): BannerAdView {
        val bannerView = BannerAdView(reactContext)
        
        // 设置事件回调
        bannerView.setOnAdClickedCallback {
            sendEvent(bannerView, EVENT_AD_CLICKED, null)
        }
        
        bannerView.setOnAdShowCallback {
            sendEvent(bannerView, EVENT_AD_SHOW, null)
        }
        
        bannerView.setOnRenderFailCallback { code, msg ->
            val eventData = Arguments.createMap()
            eventData.putInt("code", code)
            eventData.putString("message", msg)
            sendEvent(bannerView, EVENT_RENDER_FAIL, eventData)
        }
        
        bannerView.setOnRenderSuccessCallback { width, height ->
            val eventData = Arguments.createMap()
            eventData.putDouble("width", width.toDouble())
            eventData.putDouble("height", height.toDouble())
            sendEvent(bannerView, EVENT_RENDER_SUCCESS, eventData)
        }
        
        bannerView.setOnDislikeCallback { position, value ->
            val eventData = Arguments.createMap()
            eventData.putInt("position", position)
            eventData.putString("value", value)
            sendEvent(bannerView, EVENT_DISLIKE, eventData)
        }
        
        bannerView.setOnErrorCallback { code, msg ->
            val eventData = Arguments.createMap()
            eventData.putInt("code", code)
            eventData.putString("message", msg)
            sendEvent(bannerView, EVENT_ERROR, eventData)
        }
        
        bannerView.setOnEcpmInfoCallback { ecpmInfo ->
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
            sendEvent(bannerView, EVENT_ECPM_INFO, eventData)
        }
        
        return bannerView
    }
    
    /**
     * 设置广告位ID
     */
    @ReactProp(name = "codeId")
    fun setCodeId(view: BannerAdView, codeId: String?) {
        if (!codeId.isNullOrEmpty()) {
            view.setCodeId(codeId)
        }
    }
    
    /**
     * 设置广告尺寸（单位：dp）
     * 格式：{width: number, height: number}
     */
    @ReactProp(name = "adSize")
    fun setAdSize(view: BannerAdView, adSize: ReadableMap?) {
        if (adSize != null && adSize.hasKey("width") && adSize.hasKey("height")) {
            val width = adSize.getDouble("width").toFloat()
            val height = adSize.getDouble("height").toFloat()
            view.setAdSize(width, height)
        }
    }
    
    /**
     * 接收命令
     */
    override fun receiveCommand(
        root: BannerAdView,
        commandId: Int,
        args: com.facebook.react.bridge.ReadableArray?
    ) {
        when (commandId) {
            COMMAND_LOAD_AD -> root.loadAd()
            COMMAND_DESTROY -> root.destroyAd()
            COMMAND_IS_AD_LOADED -> {
                // isAdLoaded 通过返回值处理，这里不需要实现
            }
        }
    }
    
    /**
     * 获取命令映射
     */
    override fun getCommandsMap(): Map<String, Int>? {
        return MapBuilder.of(
            "loadAd", COMMAND_LOAD_AD,
            "destroy", COMMAND_DESTROY,
            "isAdLoaded", COMMAND_IS_AD_LOADED
        )
    }
    
    /**
     * 发送事件到React Native
     */
    private fun sendEvent(view: View, eventName: String, params: WritableMap?) {
        val reactContext = view.context as? com.facebook.react.bridge.ReactContext
        reactContext?.let { ctx ->
            ctx.getJSModule(RCTEventEmitter::class.java)
                .receiveEvent(view.id, eventName, params ?: Arguments.createMap())
        }
    }
    
    /**
     * 获取导出的事件映射
     */
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Map<String, String>>? {
        return MapBuilder.of(
            EVENT_AD_CLICKED, MapBuilder.of("registrationName", EVENT_AD_CLICKED),
            EVENT_AD_SHOW, MapBuilder.of("registrationName", EVENT_AD_SHOW),
            EVENT_RENDER_FAIL, MapBuilder.of("registrationName", EVENT_RENDER_FAIL),
            EVENT_RENDER_SUCCESS, MapBuilder.of("registrationName", EVENT_RENDER_SUCCESS),
            EVENT_DISLIKE, MapBuilder.of("registrationName", EVENT_DISLIKE),
            EVENT_ERROR, MapBuilder.of("registrationName", EVENT_ERROR),
            EVENT_ECPM_INFO, MapBuilder.of("registrationName", EVENT_ECPM_INFO)
        )
    }
}

