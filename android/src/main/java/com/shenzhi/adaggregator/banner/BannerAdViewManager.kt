package com.shenzhi.adaggregator.banner

import android.view.View
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

/**
 * Banner广告ViewManager
 * 用于在React Native中管理BannerAdView组件
 * 使用单例模式防止热重载时重复注册
 */
class BannerAdViewManager : SimpleViewManager<BannerAdView>() {

    companion object {
        const val REACT_CLASS = "BannerAdView"
        
        // 单例实例，防止热重载时重复注册
        @Volatile
        private var instance: BannerAdViewManager? = null
        
        /**
         * 获取单例实例
         */
        fun getInstance(): BannerAdViewManager {
            return instance ?: synchronized(this) {
                instance ?: BannerAdViewManager().also { instance = it }
            }
        }
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(reactContext: ThemedReactContext): BannerAdView {
        return BannerAdView(reactContext)
    }

    /**
     * 设置广告位ID
     */
    @ReactProp(name = "codeId")
    fun setCodeId(view: BannerAdView, codeId: String?) {
        view.setCodeId(codeId)
    }

    /**
     * 设置广告尺寸
     * 接收一个包含width和height的ReadableMap
     */
    @ReactProp(name = "adSize")
    fun setAdSize(view: BannerAdView, adSize: ReadableMap?) {
        if (adSize != null && adSize.hasKey("width") && adSize.hasKey("height")) {
            val width = adSize.getDouble("width").toFloat()
            val height = adSize.getDouble("height").toFloat()
            view.setAdSize(width, height)
        }
    }
}

