package com.shenzhi.adaggregator

import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTCustomController
import com.shenzhi.adaggregator.core.ADCore
import com.bytedance.sdk.openadsdk.mediation.init.MediationConfigUserInfoForSegment
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig
import java.util.HashMap

/**
 * 穿山甲广告SDK主管理Module (TurboModule)
 * 负责SDK初始化和为不同类型广告提供初始化实例
 */
@ReactModule(name = PangleAdManagerModule.NAME)
class PangleAdManagerModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), TurboModule {

    override fun getName(): String {
        return NAME
    }

    /**
     * 获取TTAdManager实例
     */
    fun get(): TTAdManager? {
        return ADCore.getTTAdManager()
    }
    /**
     * 初始化穿山甲融合SDK
     *
     * @param config 配置参数
     * @param promise Promise回调
     */
    @ReactMethod
    fun initMediationAdSdk(config: ReadableMap, promise: Promise) {
        try {
            // 解析配置参数
            val appId = config.getString("appId") ?: run {
                promise.reject("INVALID_CONFIG", "appId不能为空")
                return
            }
            // 安全获取布尔值：先检查键是否存在
            val debug = if (config.hasKey("debug")) config.getBoolean("debug") else false
            val supportMultiProcess = if (config.hasKey("supportMultiProcess")) config.getBoolean("supportMultiProcess") else false

            // 隐私设置（默认true）
            val allowLocation = if (config.hasKey("allowLocation")) config.getBoolean("allowLocation") else true
            val allowPhoneState = if (config.hasKey("allowPhoneState")) config.getBoolean("allowPhoneState") else true
            val allowWifiState = if (config.hasKey("allowWifiState")) config.getBoolean("allowWifiState") else true
            val allowAndroidId = if (config.hasKey("allowAndroidId")) config.getBoolean("allowAndroidId") else true
            val allowWriteExternal = if (config.hasKey("allowWriteExternal")) config.getBoolean("allowWriteExternal") else true

            // 进阶设置
            val themeStatus = if (config.hasKey("themeStatus")) config.getInt("themeStatus") else 0
            val limitPersonalAds = if (config.hasKey("limitPersonalAds")) config.getBoolean("limitPersonalAds") else false
            val limitProgrammaticAds = if (config.hasKey("limitProgrammaticAds")) config.getBoolean("limitProgrammaticAds") else false

            // 构建 ADCore 配置
            val adCoreConfig = ADCore.InitConfig(
                appId = appId,
                debug = debug,
                supportMultiProcess = supportMultiProcess,
                allowLocation = allowLocation,
                allowPhoneState = allowPhoneState,
                allowWifiState = allowWifiState,
                allowAndroidId = allowAndroidId,
                allowWriteExternal = allowWriteExternal,
                themeStatus = themeStatus,
                limitPersonalAds = limitPersonalAds,
                limitProgrammaticAds = limitProgrammaticAds
            )

            // 调用 ADCore 进行初始化
            ADCore.init(
                context = reactApplicationContext,
                config = adCoreConfig,
                callback = object : ADCore.InitCallback {
                    override fun onSuccess() {
                        promise.resolve(true)
                    }

                    override fun onFail(code: Int, msg: String) {
                        promise.reject("INIT_FAILED", "初始化失败: code=$code, msg=$msg", null)
                    }
                }
            )
        } catch (e: Exception) {
            promise.reject("INVALID_CONFIG", "配置解析失败: ${e.message}", e)
        }
    }

    /**
     * 检查SDK是否已初始化
     */
    @ReactMethod
    fun isSdkReady(promise: Promise) {
        promise.resolve(ADCore.isSdkReady())
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return ADCore.isInitialized()
    }

    /**
     * TurboModule 接口要求的方法
     * 当模块被销毁时调用
     */
    override fun invalidate() {
        // ADCore 是单例，不需要在这里清理
        // 如果需要重置，可以调用 ADCore.reset()
    }


  /**
   * 获取用户信息用于流量分组（示例方法）
   */
  private fun getUserInfoForSegment(): MediationConfigUserInfoForSegment {
    val userInfo = MediationConfigUserInfoForSegment()
    userInfo.userId = "msdk-demo"
    userInfo.gender = MediationConfigUserInfoForSegment.GENDER_MALE
    userInfo.channel = "msdk-channel"
    userInfo.subChannel = "msdk-sub-channel"
    userInfo.age = 999
    userInfo.userValueGroup = "msdk-demo-user-value-group"

    val customInfos = HashMap<String, String>()
    customInfos["aaaa"] = "test111"
    customInfos["bbbb"] = "test222"
    userInfo.customInfos = customInfos

    return userInfo
  }

  /**
   * 获取TTCustomController（示例方法）
   */
  private fun getTTCustomController(): TTCustomController {
    return object : TTCustomController() {
      override fun isCanUseWifiState(): Boolean {
        return super.isCanUseWifiState()
      }

      override fun getMacAddress(): String? {
        return super.getMacAddress()
      }

      override fun isCanUseWriteExternal(): Boolean {
        return super.isCanUseWriteExternal()
      }

      override fun getDevOaid(): String? {
        return super.getDevOaid()
      }

      override fun isCanUseAndroidId(): Boolean {
        return super.isCanUseAndroidId()
      }

      override fun getAndroidId(): String? {
        return super.getAndroidId()
      }

      override fun getMediationPrivacyConfig(): MediationPrivacyConfig {
        return object : MediationPrivacyConfig() {
          override fun isLimitPersonalAds(): Boolean {
            return super.isLimitPersonalAds()
          }

          override fun isProgrammaticRecommend(): Boolean {
            return super.isProgrammaticRecommend()
          }
        }
      }

      override fun isCanUsePermissionRecordAudio(): Boolean {
        return super.isCanUsePermissionRecordAudio()
      }
    }
  }

  companion object {
        const val NAME = "PangleAdManager"
    }
}

