package com.shenzhi.adaggregator.banner

import android.view.View
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.shenzhi.adaggregator.banner.BannerAdViewComponent

/**
 * Banner广告Fabric Component Manager
 * 用于在React Native Fabric架构中管理BannerAdViewComponent
 * 
 * 注意：为了避免热重载导致的组件名称冲突，我们使用静态唯一名称
 */
class BannerAdViewComponentManager : SimpleViewManager<BannerAdViewComponent>() {
    
    companion object {
        // 使用唯一名称避免热重载冲突
        // 在开发环境中，如果遇到组件名称冲突，可以考虑添加时间戳
        private const val REACT_CLASS = "ShenzhiBannerAdView"
        
        // 命令ID
        private const val COMMAND_LOAD_AD = 1
        private const val COMMAND_DESTROY = 2
        private const val COMMAND_IS_AD_LOADED = 3
    }
    
    override fun getName(): String {
        return REACT_CLASS
    }
    
    override fun createViewInstance(reactContext: ThemedReactContext): BannerAdViewComponent {
        return BannerAdViewComponent(reactContext)
    }
    
    @ReactProp(name = "codeId")
    fun setCodeId(view: BannerAdViewComponent, codeId: String?) {
        view.setCodeId(codeId)
    }
    
    @ReactProp(name = "adSize")
    fun setAdSize(view: BannerAdViewComponent, adSize: ReadableMap?) {
        view.setAdSize(adSize)
    }
    
    @Deprecated("This method overrides a deprecated member")
    override fun receiveCommand(
        root: BannerAdViewComponent,
        commandId: Int,
        args: com.facebook.react.bridge.ReadableArray?
    ) {
        when (commandId) {
            COMMAND_LOAD_AD -> root.loadAd()
            COMMAND_DESTROY -> root.destroy()
            COMMAND_IS_AD_LOADED -> {
                // isAdLoaded 通过返回值处理，这里不需要实现
            }
        }
    }
    
    override fun getCommandsMap(): Map<String, Int>? {
        return mapOf(
            "loadAd" to COMMAND_LOAD_AD,
            "destroy" to COMMAND_DESTROY,
            "isAdLoaded" to COMMAND_IS_AD_LOADED
        )
    }
    
    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Map<String, String>>? {
        return mapOf(
            "onAdClicked" to mapOf("registrationName" to "onAdClicked"),
            "onAdShow" to mapOf("registrationName" to "onAdShow"),
            "onRenderFail" to mapOf("registrationName" to "onRenderFail"),
            "onRenderSuccess" to mapOf("registrationName" to "onRenderSuccess"),
            "onDislike" to mapOf("registrationName" to "onDislike"),
            "onError" to mapOf("registrationName" to "onError"),
            "onEcpmInfo" to mapOf("registrationName" to "onEcpmInfo")
        )
    }
}

