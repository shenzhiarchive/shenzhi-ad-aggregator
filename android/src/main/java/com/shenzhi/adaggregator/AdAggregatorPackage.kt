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
    return listOf(
      BannerAdViewManager(),
      // 同时注册Fabric组件（如果启用新架构，React Native会自动使用Fabric组件）
      com.shenzhi.adaggregator.banner.BannerAdViewComponentManager()
    )
  }
}
