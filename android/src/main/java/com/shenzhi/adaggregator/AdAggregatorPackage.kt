package com.shenzhi.adaggregator

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager
import com.shenzhi.adaggregator.banner.BannerAdViewManager
import com.shenzhi.adaggregator.banner.BannerAdViewComponentManager
import java.util.HashMap

class AdAggregatorPackage : BaseReactPackage() {
  private fun isFabricEnabledSafely(): Boolean {
    // 用反射避免对较新 RN API 的编译期强依赖（老 RN 可能没有该类）
    return try {
      val clazz = Class.forName("com.facebook.react.defaults.DefaultNewArchitectureEntryPoint")
      val method = clazz.getMethod("getFabricEnabled")
      (method.invoke(null) as? Boolean) ?: false
    } catch (_: Throwable) {
      false
    }
  }

  override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return when (name) {
      PangleAdManagerModule.NAME -> PangleAdManagerModule(reactContext)
      else -> null
    }
  }

  override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
    return ReactModuleInfoProvider {
      val moduleInfos: MutableMap<String, ReactModuleInfo> = HashMap()

      moduleInfos[PangleAdManagerModule.NAME] = ReactModuleInfo(
        PangleAdManagerModule.NAME,
        PangleAdManagerModule.NAME,
        false,  // canOverrideExistingModule
        false,  // needsEagerInit
        false,  // isCxxModule
        true // isTurboModule (使用TurboModule)
      )
      moduleInfos
    }
  }
  
  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    // 注意：旧架构(Non-Fabric) 和 新架构(Fabric) 的 ViewManager 不能同时用相同的 name 注册，
    // 否则会触发：Tried to register two views with the same name ShenzhiBannerAdView
    return if (isFabricEnabledSafely()) {
      listOf(BannerAdViewComponentManager())
    } else {
      listOf(BannerAdViewManager())
    }
  }
}
