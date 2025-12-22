package com.shenzhi.adaggregator

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import com.shenzhi.adaggregator.config.SDKConfigParams
import com.shenzhi.adaggregator.config.TTAdManagerHolder
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.facebook.react.bridge.ReactContextBaseJavaModule

@ReactModule(name = AdAggregatorModule.NAME)
class AdAggregatorModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  fun initialize(appId: String, config: ReadableMap?, promise: Promise) {
    try {
      // 检查是否已初始化
      if (TTAdManagerHolder.isInitialized()) {
        promise.resolve(null)
        return
      }

      // 验证appId
      if (appId.isBlank()) {
        promise.reject("INVALID_APP_ID", "appId cannot be empty")
        return
      }


      // 构建配置参数
      val configParams = buildConfigParams(appId, config)

      // 获取应用上下文
      val context = reactApplicationContext.applicationContext
        ?: run {
          promise.reject("NO_CONTEXT", "Application context is not available")
          return
        }

      // 初始化SDK（在主线程执行，完成后resolve promise）
      TTAdManagerHolder.initialize(
        context,
        configParams,
        callback = {
          promise.resolve(null)
        },
        errorCallback = { error ->
          promise.reject("INIT_ERROR", "Failed to initialize SDK: ${error.message}", error)
        }
      )
    } catch (e: Exception) {
      promise.reject("INIT_ERROR", "Failed to initialize SDK: ${e.message}", e)
    }
  }

  /**
   * 从ReadableMap构建SDKConfigParams
   */
  private fun buildConfigParams(appId: String, config: ReadableMap?): SDKConfigParams {
    if (config == null) {
      // 使用默认配置
      return SDKConfigParams(
        appId = appId,
        useMediation = true // 聚合模式默认开启
      )
    }

    return SDKConfigParams(
      appId = appId,
      appName = if (config.hasKey("appName")) config.getString("appName") else null,
      debug = if (config.hasKey("debug")) config.getBoolean("debug") else false,
      useMediation = if (config.hasKey("useMediation")) config.getBoolean("useMediation") else true,
      allowShowNotify = if (config.hasKey("allowShowNotify")) config.getBoolean("allowShowNotify") else true,
      supportMultiProcess = if (config.hasKey("supportMultiProcess"))
        config.getBoolean("supportMultiProcess") else false,
      titleBarTheme = if (config.hasKey("titleBarTheme"))
        config.getInt("titleBarTheme") else TTAdConstant.TITLE_BAR_THEME_DARK
    )
  }

  companion object {
    const val NAME = "AdAggregator"
  }
}
