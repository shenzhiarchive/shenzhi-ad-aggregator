package com.shenzhi.adaggregator.banner

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.sdk.openadsdk.AdSlot
import com.bytedance.sdk.openadsdk.TTAdDislike
import com.bytedance.sdk.openadsdk.TTAdNative
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTNativeExpressAd
import com.bytedance.sdk.openadsdk.mediation.ad.IMediationNativeAdInfo
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot
import com.bytedance.sdk.openadsdk.mediation.ad.MediationNativeToBannerListener
import com.facebook.react.bridge.ReactContext
import com.shenzhi.adaggregator.R
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
    private var isAdShown = false

    // 布局容器
    private var bannerContainer: FrameLayout? = null

    // 事件回调
    private var onAdClickedCallback: (() -> Unit)? = null
    private var onAdShowCallback: (() -> Unit)? = null
    private var onRenderFailCallback: ((code: Int, msg: String) -> Unit)? = null
    private var onRenderSuccessCallback: ((width: Float, height: Float) -> Unit)? = null
    private var onDislikeCallback: ((position: Int, value: String) -> Unit)? = null
    private var onErrorCallback: ((code: Int, msg: String) -> Unit)? = null
    private var onEcpmInfoCallback: ((ecpmInfo: Map<String, Any?>) -> Unit)? = null

    init {
        // 设置容器布局参数
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        // 确保View可见
        visibility = View.VISIBLE

        // 加载布局文件并添加到当前View
        val inflater = LayoutInflater.from(context)
        val layoutView = inflater.inflate(R.layout.mediation_activity_banner, this, false)

        // 设置layoutView的布局参数
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        layoutView.layoutParams = layoutParams
        addView(layoutView)

        // 查找banner容器
        bannerContainer = findViewById(R.id.banner_container)

        if (bannerContainer == null) {
            Log.e(TAG, "Failed to find banner_container in layout")
        } else {
            Log.d(TAG, "Banner container found successfully")
            // 设置容器的布局参数，确保可以显示内容
            val containerParams = bannerContainer!!.layoutParams
            if (containerParams != null) {
                containerParams.width = FrameLayout.LayoutParams.MATCH_PARENT
                containerParams.height = FrameLayout.LayoutParams.WRAP_CONTENT
                bannerContainer!!.layoutParams = containerParams
            }
            bannerContainer!!.visibility = View.VISIBLE
        }
    }



    fun loadAd() {
        if (codeId.isNullOrEmpty()) {
            Log.w(TAG, "codeId is empty, cannot load ad")
            onErrorCallback?.invoke(-1, "codeId is empty")
            return
        }

        if (width <= 0) {
            Log.w(TAG, "ad width is invalid, cannot load ad")
            onErrorCallback?.invoke(-1, "ad width is invalid")
            return
        }

        if (!ADCore.isSdkReady()) {
            Log.w(TAG, "SDK is not ready, cannot load ad")
            onErrorCallback?.invoke(-1, "SDK is not ready")
            return
        }

        // 销毁之前的广告并重置展示状态
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
        // 将px转换为dp用于模板广告尺寸
        val widthDp = UIUtils.px2dip(context, width.toFloat()).toFloat()
        val heightDp = if (height > 0) UIUtils.px2dip(context, height.toFloat()).toFloat() else 0f
        val adSlot = AdSlot.Builder()
            .setCodeId(codeId!!)
            .setImageAcceptedSize(width, if (height > 0) height else 0) // 自渲染尺寸，单位px
            .setExpressViewAcceptedSize(widthDp, if (heightDp > 0f) heightDp else 0f) // 模板广告尺寸，单位dp
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
     * 将广告View添加到容器
     * @param view 广告View（不能为null）
     * @param renderHeight 渲染成功返回的高度（单位dp），如果为0或负数则使用预设高度
     */
    private fun addAdViewToContainer(view: View, renderHeight: Float = 0f) {
        val container = bannerContainer
        if (container == null) {
            Log.e(TAG, "bannerContainer is null, cannot add ad view")
            return
        }

        val adView = view

        // 如果View已经有父容器，先移除
        val parent = adView.parent
        if (parent != null && parent is android.view.ViewGroup) {
            Log.d(TAG, "Removing adView from existing parent")
            parent.removeView(adView)
        }

        // 确定要使用的高度（优先使用渲染返回的高度）
        val targetHeight = resolveLayoutHeight(renderHeight)
        val targetWidth = resolveLayoutWidth()

        Log.d(TAG, "Adding adView with renderHeight=$renderHeight dp, targetHeight=${describeLayoutValue(targetHeight)}")

        // 使用post确保在布局完成后添加View
        container.post {
            // 添加到容器
            container.removeAllViews()

            // 更新容器高度
            val containerParams = container.layoutParams
            if (containerParams != null) {
                containerParams.width = targetWidth
                containerParams.height = targetHeight
                container.layoutParams = containerParams
                Log.d(TAG, "Updated container layout params: width=${describeLayoutValue(targetWidth)}, height=${describeLayoutValue(targetHeight)}")
            }

            // 创建布局参数，使用实际高度
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                targetHeight
            )
            container.addView(adView, layoutParams)

            Log.d(TAG, "Banner ad view added to container. Container: width=${container.width}, height=${container.height}")
            Log.d(TAG, "AdView type: ${adView.javaClass.simpleName}, parent: ${adView.parent?.javaClass?.simpleName}")
            Log.d(TAG, "AdView visibility: ${adView.visibility}, Container visibility: ${container.visibility}")
            Log.d(TAG, "AdView has ${(adView as? ViewGroup)?.childCount ?: 0} children")

            // 确保容器和View可见
            container.visibility = View.VISIBLE
            adView.visibility = View.VISIBLE

            // 设置背景色以便调试（可选，生产环境可以移除）
            // adView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

            // 强制请求布局和测量
            container.requestLayout()
            container.invalidate()
            adView.requestLayout()
            adView.invalidate()

            // 确保父View也可见
            var parentView: View? = container.parent as? View
            while (parentView != null) {
                if (parentView.visibility != View.VISIBLE) {
                    Log.d(TAG, "Found invisible parent: ${parentView.javaClass.simpleName}, setting to VISIBLE")
                    parentView.visibility = View.VISIBLE
                }
                parentView = parentView.parent as? View
            }

            // 强制测量AdView，使用确定的高度
            if (container.width > 0 && targetHeight > 0) {
                Log.d(TAG, "Forcing measure with width=${container.width}, height=$targetHeight")
                val widthSpec = View.MeasureSpec.makeMeasureSpec(container.width, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(targetHeight, View.MeasureSpec.EXACTLY)
                adView.measure(widthSpec, heightSpec)
                adView.layout(0, 0, adView.measuredWidth, adView.measuredHeight)
                Log.d(TAG, "After forced measure - AdView: width=${adView.width}, height=${adView.height}, measuredWidth=${adView.measuredWidth}, measuredHeight=${adView.measuredHeight}")
            }

            // 延迟检查，确保布局完成后再触发onAdShow
            triggerAdShowIfVisible(adView)
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
     * 设置ECPM信息回调
     */
    fun setOnEcpmInfoCallback(callback: ((ecpmInfo: Map<String, Any?>) -> Unit)?) {
        this.onEcpmInfoCallback = callback
    }

    /**
     * 设置广告位ID
     */
    fun setCodeId(codeId: String?) {
        if (this.codeId != codeId) {
            this.codeId = codeId
            // 如果codeId改变，重新加载广告
            if (!codeId.isNullOrEmpty() && width > 0) {
                loadAd()
            }
        }
    }

    /**
     * 设置广告尺寸（单位：dp）
     */
    fun setAdSize(widthDp: Float, heightDp: Float) {
        val widthPx = UIUtils.dip2px(context, widthDp).toInt()
        val heightPx = UIUtils.dip2px(context, heightDp).toInt()
        
        if (this.width != widthPx || this.height != heightPx) {
            this.width = widthPx
            this.height = heightPx
            // 如果尺寸改变且codeId已设置，重新加载广告
            if (!codeId.isNullOrEmpty() && widthPx > 0) {
                loadAd()
            }
        }
    }

    /**
     * 展示广告
     */
    private fun showAd() {
        if (bannerAd == null) {
            Log.w(TAG, "bannerAd is null, cannot show ad")
            return
        }

        if (isAdShown) {
            Log.w(TAG, "ad already shown")
            return
        }

        val activity = (context as? ReactContext)?.currentActivity
        if (activity == null) {
            Log.e(TAG, "activity is null, cannot show ad")
            return
        }

        // 设置交互监听器
        bannerAd?.setExpressInteractionListener(object : TTNativeExpressAd.ExpressAdInteractionListener {
            override fun onAdClicked(view: View, type: Int) {
                Log.d(TAG, "banner clicked")
                onAdClickedCallback?.invoke()
            }

            override fun onAdShow(view: View, type: Int) {
                Log.d(TAG, "banner showed")
                isAdShown = true
                onAdShowCallback?.invoke()
                
                // 获取ECPM信息
                getEcpmInfo()
            }

            override fun onRenderFail(view: View, msg: String, code: Int) {
                Log.e(TAG, "banner renderFail, errCode: $code, errMsg: $msg")
                onRenderFailCallback?.invoke(code, msg)
            }

            override fun onRenderSuccess(view: View, width: Float, height: Float) {
                Log.d(TAG, "banner render success, width: $width, height: $height")
                onRenderSuccessCallback?.invoke(width, height)
                // 将广告View添加到容器
                addAdViewToContainer(view, height)
            }
        })

        // 设置dislike回调
        bannerAd?.setDislikeCallback(activity, object : TTAdDislike.DislikeInteractionCallback {
            override fun onShow() {
                Log.d(TAG, "dislike dialog show")
            }

            override fun onSelected(position: Int, value: String, enforce: Boolean) {
                Log.d(TAG, "dislike selected, position: $position, value: $value")
                onDislikeCallback?.invoke(position, value)
                // 用户选择不喜欢，销毁广告
                destroyAd()
            }

            override fun onCancel() {
                Log.d(TAG, "dislike cancel")
            }
        })

        // 获取广告View并展示
        val adView = bannerAd?.expressAdView
        if (adView != null) {
            addAdViewToContainer(adView)
        } else {
            Log.w(TAG, "adView is null, waiting for render success")
        }
    }

    /**
     * 销毁广告
     */
    fun destroyAd() {
        if (bannerAd != null) {
            Log.d(TAG, "destroying banner ad")
            bannerAd?.destroy()
            bannerAd = null
        }
        isAdLoaded = false
        isAdShown = false
        adNativeLoader = null
        
        // 清空容器
        bannerContainer?.removeAllViews()
    }

    /**
     * 检查广告是否已加载
     */
    fun isAdLoaded(): Boolean {
        return isAdLoaded && bannerAd != null
    }

    /**
     * 获取ECPM信息
     */
    private fun getEcpmInfo() {
        val mediationManager = bannerAd?.mediationManager
        if (mediationManager != null) {
            val showEcpm = mediationManager.showEcpm
            if (showEcpm != null) {
                val ecpmInfo = mutableMapOf<String, Any?>()
                ecpmInfo["sdkName"] = showEcpm.sdkName
                ecpmInfo["customSdkName"] = showEcpm.customSdkName
                ecpmInfo["slotId"] = showEcpm.slotId
                ecpmInfo["ecpm"] = showEcpm.ecpm
                ecpmInfo["reqBiddingType"] = showEcpm.reqBiddingType
                ecpmInfo["errorMsg"] = showEcpm.errorMsg
                ecpmInfo["requestId"] = showEcpm.requestId
                ecpmInfo["ritType"] = showEcpm.ritType
                ecpmInfo["abTestId"] = showEcpm.abTestId
                ecpmInfo["scenarioId"] = showEcpm.scenarioId
                ecpmInfo["segmentId"] = showEcpm.segmentId
                ecpmInfo["channel"] = showEcpm.channel
                ecpmInfo["subChannel"] = showEcpm.subChannel
                ecpmInfo["customData"] = showEcpm.customData
                onEcpmInfoCallback?.invoke(ecpmInfo)
            }
        }
    }

    /**
     * 解析布局高度
     */
    private fun resolveLayoutHeight(renderHeight: Float): Int {
        return if (renderHeight > 0) {
            UIUtils.dip2px(context, renderHeight)
        } else if (height > 0) {
            height
        } else {
            LayoutParams.WRAP_CONTENT
        }
    }

    /**
     * 解析布局宽度
     */
    private fun resolveLayoutWidth(): Int {
        return if (width > 0) {
            width
        } else {
            LayoutParams.MATCH_PARENT
        }
    }

    /**
     * 描述布局值（用于日志）
     */
    private fun describeLayoutValue(value: Int): String {
        return when (value) {
            LayoutParams.MATCH_PARENT -> "MATCH_PARENT"
            LayoutParams.WRAP_CONTENT -> "WRAP_CONTENT"
            else -> "$value px"
        }
    }

    /**
     * 触发广告展示（如果可见）
     */
    private fun triggerAdShowIfVisible(adView: View) {
        adView.postDelayed({
            if (adView.isShown && !isAdShown && bannerAd != null) {
                // 如果View已显示但还未触发onAdShow，手动触发
                onAdShowCallback?.invoke()
                isAdShown = true
                getEcpmInfo()
            }
        }, 100)
    }



}

