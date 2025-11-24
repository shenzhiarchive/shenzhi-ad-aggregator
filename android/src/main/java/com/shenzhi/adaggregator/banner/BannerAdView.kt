package com.shenzhi.adaggregator.banner

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.shenzhi.adaggregator.core.ADCore
import com.shenzhi.adaggregator.utils.UIUtils

/**
 * Banner广告View组件
 * 基于穿山甲融合SDK实现Banner广告展示
 */
class BannerAdView(context: Context) : FrameLayout(context) {

    companion object {
        private const val TAG = "BannerAdView"
    }

    private val reactContext: ThemedReactContext = context as ThemedReactContext
    private var codeId: String? = null
    private var adWidth: Int = 0
    private var adHeight: Int = 0
    
    private var adNativeLoader: TTAdNative? = null
    private var bannerAd: TTNativeExpressAd? = null
    private var bannerContainer: FrameLayout? = null
    private var isAdLoaded = false

    init {
        // 加载布局文件
        val layoutId = context.resources.getIdentifier(
            "mediation_activity_banner",
            "layout",
            context.packageName
        )
        
        if (layoutId == 0) {
            Log.e(TAG, "mediation_activity_banner layout not found, creating default container")
            // 如果找不到布局文件，创建一个默认的FrameLayout容器
            bannerContainer = FrameLayout(context).apply {
                layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
            }
            addView(bannerContainer)
        } else {
            val inflater = LayoutInflater.from(context)
            val rootView = inflater.inflate(layoutId, this, true)
            
            // 获取banner容器
            val containerId = context.resources.getIdentifier(
                "banner_container",
                "id",
                context.packageName
            )
            
            if (containerId != 0) {
                bannerContainer = rootView.findViewById(containerId) as? FrameLayout
            }
            
            // 如果找不到容器，使用根布局
            if (bannerContainer == null) {
                Log.w(TAG, "banner_container not found, using root view")
                bannerContainer = this
            }
        }
    }

    /**
     * 设置广告位ID
     */
    fun setCodeId(codeId: String?) {
        if (this.codeId == codeId) {
            return
        }
        this.codeId = codeId
        if (!codeId.isNullOrEmpty()) {
            loadAd()
        } else {
            destroyAd()
        }
    }

    /**
     * 设置广告尺寸
     */
    fun setAdSize(width: Float, height: Float) {
        val widthPx = UIUtils.dp2px(context, width).toInt()
        val heightPx = UIUtils.dp2px(context, height).toInt()
        
        if (adWidth != widthPx || adHeight != heightPx) {
            adWidth = widthPx
            adHeight = heightPx
            // 如果广告已加载，重新加载
            if (isAdLoaded && !codeId.isNullOrEmpty()) {
                loadAd()
            }
        }
    }

    /**
     * 加载广告
     */
    fun loadAd() {
        if (codeId.isNullOrEmpty()) {
            Log.w(TAG, "codeId is empty, cannot load ad")
            return
        }

        if (adWidth <= 0 || adHeight <= 0) {
            Log.w(TAG, "adSize is invalid, cannot load ad")
            return
        }

        // 检查SDK是否已初始化
        if (!ADCore.isSdkReady()) {
            Log.e(TAG, "SDK is not ready, please initialize SDK first")
            sendErrorEvent(-1, "SDK未初始化，请先初始化SDK")
            return
        }

        // 获取TTAdManager并创建TTAdNative
        val adManager = ADCore.getTTAdManager()
        if (adManager == null) {
            Log.e(TAG, "TTAdManager is null")
            sendErrorEvent(-1, "TTAdManager未初始化")
            return
        }

        val activity = reactContext.currentActivity
        if (activity == null) {
            Log.e(TAG, "Activity is null")
            sendErrorEvent(-1, "Activity为空")
            return
        }

        // 销毁之前的广告
        destroyAd()

        // 创建TTAdNative对象（保证每次请求的广告对象为新的广告对象）
        adNativeLoader = adManager.createAdNative(activity)

        // 创建AdSlot
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId!!)
            .setImageAcceptedSize(adWidth, adHeight) // 自渲染尺寸，单位px
            .setExpressViewAcceptedSize(UIUtils.getScreenWidthDp(context), 0f) // 模板广告尺寸，单位dp
            .build()

        // 加载广告
        adNativeLoader?.loadBannerExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onError(code: Int, message: String) {
                Log.e(TAG, "Banner ad load failed: code=$code, message=$message")
                isAdLoaded = false
                sendErrorEvent(code, message)
            }

            override fun onNativeExpressAdLoad(ads: MutableList<TTNativeExpressAd>?) {
                if (ads != null && ads.isNotEmpty()) {
                    Log.d(TAG, "Banner ad load success")
                    bannerAd = ads[0]
                    isAdLoaded = true
                    // 自动展示广告
                    showAd()
                } else {
                    Log.w(TAG, "Banner ad load success, but list is null or empty")
                    isAdLoaded = false
                    sendErrorEvent(-1, "广告加载成功但列表为空")
                }
            }
        })
    }

    /**
     * 展示广告
     */
    private fun showAd() {
        val ad = bannerAd ?: run {
            Log.w(TAG, "Banner ad is null, cannot show")
            return
        }

        val container = bannerContainer ?: run {
            Log.w(TAG, "Banner container is null")
            return
        }

        val activity = reactContext.currentActivity ?: run {
            Log.w(TAG, "Activity is null")
            return
        }

        // 设置交互监听器
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View?, type: Int) {
                Log.d(TAG, "Banner ad clicked")
                sendEvent("onAdClicked", null)
            }

            override fun onAdShow(view: View?, type: Int) {
                Log.d(TAG, "Banner ad showed")
                sendEvent("onAdShow", null)
            }

            override fun onRenderFail(view: View?, msg: String?, code: Int) {
                Log.e(TAG, "Banner ad render failed: code=$code, message=$msg")
                val eventData = Arguments.createMap().apply {
                    putInt("code", code)
                    putString("message", msg ?: "")
                }
                sendEvent("onRenderFail", eventData)
            }

            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                Log.d(TAG, "Banner ad render success: width=$width, height=$height")
                
                // 根据文档说明：onRenderSuccess方法内返回的view为null需要通过getAdView获取view
                val adView = view ?: ad.getExpressAdView()
                if (adView != null && bannerContainer != null) {
                    bannerContainer?.removeAllViews()
                    bannerContainer?.addView(adView)
                }
                
                val eventData = Arguments.createMap().apply {
                    putDouble("width", width.toDouble())
                    putDouble("height", height.toDouble())
                }
                sendEvent("onRenderSuccess", eventData)
            }
        })

        // 设置dislike回调
        ad.setDislikeCallback(activity, object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {
                Log.d(TAG, "Dislike dialog shown")
            }

            override fun onSelected(position: Int, value: String?, enforce: Boolean) {
                Log.d(TAG, "Dislike selected: position=$position, value=$value")
                val eventData = Arguments.createMap().apply {
                    putInt("position", position)
                    putString("value", value ?: "")
                }
                sendEvent("onDislike", eventData)
                // 移除广告视图
                container.removeAllViews()
                destroyAd()
            }

            override fun onCancel() {
                Log.d(TAG, "Dislike dialog cancelled")
            }
        })

        // 检查广告是否就绪（根据文档建议）
        val mediationManager = ad.mediationManager
        if (mediationManager != null && !mediationManager.isReady) {
            Log.w(TAG, "Ad is not ready yet, waiting for render")
            // 广告可能还未就绪，等待渲染完成
            return
        }

        // 获取广告View并添加到容器
        val bannerView = ad.getExpressAdView()
        if (bannerView != null) {
            container.removeAllViews()
            container.addView(bannerView)
        } else {
            Log.w(TAG, "Banner ad view is null, ad may need to render first")
            // 根据文档说明：onRenderSuccess方法内返回的view为null需要通过getAdView获取view
            // 这里先记录日志，等待onRenderSuccess回调后再获取view
        }
    }

    /**
     * 销毁广告
     */
    fun destroyAd() {
        bannerAd?.destroy()
        bannerAd = null
        adNativeLoader = null
        isAdLoaded = false
        
        bannerContainer?.removeAllViews()
    }

    /**
     * 检查广告是否已加载
     */
    fun isAdLoaded(): Boolean {
        return isAdLoaded && bannerAd != null
    }

    /**
     * 发送事件到React Native
     */
    private fun sendEvent(eventName: String, data: WritableMap?) {
        val event = Arguments.createMap().apply {
            if (data != null) {
                putMap("nativeEvent", data)
            }
        }
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, eventName, event)
    }

    /**
     * 发送错误事件
     */
    private fun sendErrorEvent(code: Int, message: String) {
        val eventData = Arguments.createMap().apply {
            putInt("code", code)
            putString("message", message)
        }
        sendEvent("onError", eventData)
    }

    /**
     * 组件销毁时清理资源
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        destroyAd()
    }
}

