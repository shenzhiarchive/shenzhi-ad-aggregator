package com.shenzhi.adaggregator.banner

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdEcpmInfo
import com.facebook.react.bridge.ReactContext
import com.shenzhi.adaggregator.core.ADCore
import com.shenzhi.adaggregator.utils.UIUtils

/**
 * Banner广告View
 * 用于展示穿山甲Banner广告
 */
class BannerAdView(context: Context) : FrameLayout(context) {
    
    companion object {
        private const val TAG = "BannerAdView"
    }
    
    private var codeId: String? = null
    private var width: Int = 0
    private var height: Int = 0
    private var bannerAd: TTNativeExpressAd? = null
    private var adNativeLoader: TTAdNative? = null
    private var isAdLoaded = false
    
    // 事件回调
    private var onAdClickedCallback: (() -> Unit)? = null
    private var onAdShowCallback: (() -> Unit)? = null
    private var onRenderFailCallback: ((code: Int, msg: String) -> Unit)? = null
    private var onRenderSuccessCallback: ((width: Float, height: Float) -> Unit)? = null
    private var onDislikeCallback: ((position: Int, value: String) -> Unit)? = null
    private var onErrorCallback: ((code: Int, msg: String) -> Unit)? = null
    
    init {
        // 初始化容器
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }
    
    /**
     * 设置广告位ID
     */
    fun setCodeId(codeId: String) {
        if (this.codeId != codeId) {
            this.codeId = codeId
            // 如果已经设置了尺寸，则自动加载广告
            if (width > 0 && height > 0) {
                loadAd()
            }
        }
    }
    
    /**
     * 设置广告尺寸（单位：dp）
     */
    fun setAdSize(widthDp: Float, heightDp: Float) {
        val widthPx = UIUtils.dp2px(context, widthDp)
        val heightPx = UIUtils.dp2px(context, heightDp)
        
        if (this.width != widthPx || this.height != heightPx) {
            this.width = widthPx
            this.height = heightPx
            // 如果已经设置了codeId，则自动加载广告
            if (!codeId.isNullOrEmpty()) {
                loadAd()
            }
        }
    }
    
    /**
     * 设置广告尺寸（单位：px）
     */
    fun setAdSizePx(widthPx: Int, heightPx: Int) {
        if (this.width != widthPx || this.height != heightPx) {
            this.width = widthPx
            this.height = heightPx
            // 如果已经设置了codeId，则自动加载广告
            if (!codeId.isNullOrEmpty()) {
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
            onErrorCallback?.invoke(-1, "codeId is empty")
            return
        }
        
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "ad size is invalid, cannot load ad")
            onErrorCallback?.invoke(-1, "ad size is invalid")
            return
        }
        
        if (!ADCore.isSdkReady()) {
            Log.w(TAG, "SDK is not ready, cannot load ad")
            onErrorCallback?.invoke(-1, "SDK is not ready")
            return
        }
        
        // 销毁之前的广告
        destroyAd()
        
        val adManager = ADCore.getTTAdManager()
        if (adManager == null) {
            Log.e(TAG, "TTAdManager is null")
            onErrorCallback?.invoke(-1, "TTAdManager is null")
            return
        }
        
        // 创建TTAdNative对象
        adNativeLoader = adManager.createAdNative(context)
        
        // 创建AdSlot
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId!!)
            .setImageAcceptedSize(width, height) // 自渲染尺寸，单位px
            .setExpressViewAcceptedSize(UIUtils.getScreenWidthDp(context), 0f) // 模板广告尺寸，单位dp
            .build()
        
        // 加载广告
        adNativeLoader?.loadBannerExpressAd(adSlot, object : TTAdNative.NativeExpressAdListener {
            override fun onError(code: Int, msg: String) {
                Log.e(TAG, "banner load fail: errCode: $code, errMsg: $msg")
                isAdLoaded = false
                onErrorCallback?.invoke(code, msg)
            }
            
            override fun onNativeExpressAdLoad(ads: MutableList<TTNativeExpressAd>?) {
                if (ads != null && ads.isNotEmpty()) {
                    Log.d(TAG, "banner load success")
                    bannerAd = ads[0]
                    isAdLoaded = true
                    // 自动展示广告
                    showAd()
                } else {
                    Log.w(TAG, "banner load success, but list is null or empty")
                    isAdLoaded = false
                    onErrorCallback?.invoke(-1, "banner load success, but list is null or empty")
                }
            }
        })
    }
    
    /**
     * 展示广告
     */
    private fun showAd() {
        val ad = bannerAd ?: return
        
        // 设置交互监听器
        ad.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View?, type: Int) {
                Log.d(TAG, "banner clicked")
                onAdClickedCallback?.invoke()
            }
            
            override fun onAdShow(view: View?, type: Int) {
                Log.d(TAG, "banner showed")
                onAdShowCallback?.invoke()
            }
            
            override fun onRenderFail(view: View?, msg: String?, code: Int) {
                Log.e(TAG, "banner renderFail, errCode: $code, errMsg: $msg")
                onRenderFailCallback?.invoke(code, msg ?: "unknown error")
            }
            
            override fun onRenderSuccess(view: View?, width: Float, height: Float) {
                Log.d(TAG, "banner render success, width: $width, height: $height")
                onRenderSuccessCallback?.invoke(width, height)
            }
        })
        
        // 设置dislike回调
        val activity = getActivity()
        ad.setDislikeCallback(activity, object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {
                Log.d(TAG, "dislike dialog show")
            }
            
            override fun onSelected(position: Int, value: String?, enforce: Boolean) {
                Log.d(TAG, "banner closed, position: $position, value: $value")
                onDislikeCallback?.invoke(position, value ?: "")
                // 移除广告View
                removeAllViews()
            }
            
            override fun onCancel() {
                Log.d(TAG, "dislike dialog cancel")
            }
        })
        
        // 获取广告View并添加到容器
        val bannerView = ad.getExpressAdView()
        if (bannerView != null) {
            removeAllViews()
            addView(bannerView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            ))
        } else {
            Log.w(TAG, "banner view is null")
            onRenderFailCallback?.invoke(-1, "banner view is null")
        }
    }
    
    /**
     * 设置点击回调
     */
    fun setOnAdClickedCallback(callback: (() -> Unit)?) {
        this.onAdClickedCallback = callback
    }
    
    /**
     * 设置展示回调
     */
    fun setOnAdShowCallback(callback: (() -> Unit)?) {
        this.onAdShowCallback = callback
    }
    
    /**
     * 设置渲染失败回调
     */
    fun setOnRenderFailCallback(callback: ((code: Int, msg: String) -> Unit)?) {
        this.onRenderFailCallback = callback
    }
    
    /**
     * 设置渲染成功回调
     */
    fun setOnRenderSuccessCallback(callback: ((width: Float, height: Float) -> Unit)?) {
        this.onRenderSuccessCallback = callback
    }
    
    /**
     * 设置dislike回调
     */
    fun setOnDislikeCallback(callback: ((position: Int, value: String) -> Unit)?) {
        this.onDislikeCallback = callback
    }
    
    /**
     * 设置错误回调
     */
    fun setOnErrorCallback(callback: ((code: Int, msg: String) -> Unit)?) {
        this.onErrorCallback = callback
    }
    
    /**
     * 销毁广告
     */
    fun destroyAd() {
        bannerAd?.destroy()
        bannerAd = null
        adNativeLoader = null
        isAdLoaded = false
        removeAllViews()
    }
    
    /**
     * 检查广告是否已加载
     */
    fun isAdLoaded(): Boolean {
        return isAdLoaded && bannerAd != null
    }
    
    /**
     * 获取当前的Activity
     */
    private fun getActivity(): Activity? {
        // 如果context本身就是Activity，直接返回
        if (context is Activity) {
            return context as Activity
        }
        // 如果是ReactContext，尝试获取currentActivity
        if (context is ReactContext) {
            return (context as ReactContext).currentActivity
        }
        return null
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // View从窗口移除时销毁广告
        destroyAd()
    }
}

