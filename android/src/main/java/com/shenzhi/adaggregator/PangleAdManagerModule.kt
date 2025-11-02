package com.shenzhi.adaggregator

import android.os.Handler
import android.os.Looper
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.IMediationManager
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

    private var isInitialized = false
    private var mediationManager: IMediationManager? = null

    override fun getName(): String {
        return NAME
    }

    /**
     * 获取TTAdManager实例
     */
    fun get(): TTAdManager? {
      return TTAdSdk.getAdManager()
    }
    /**
     * 初始化穿山甲融合SDK
     *
     * @param config 配置参数
     * @param promise Promise回调
     */
    @ReactMethod
    fun initMediationAdSdk(config: ReadableMap, promise: Promise) {
        if (isInitialized) {
            promise.resolve(true)
            return
        }

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

            // 在主线程中调用初始化
            Handler(Looper.getMainLooper()).post {
                try {
                    // 构建TTAdConfig
                    val adConfig = TTAdConfig.Builder()
                        .appId(appId)
                        .useMediation(true) // 开启聚合功能
                        .supportMultiProcess(supportMultiProcess)
                        .debug(debug)
                        .titleBarTheme(themeStatus)
                        .customController(createCustomController(
                            allowLocation,
                            allowPhoneState,
                            allowWifiState,
                            allowAndroidId,
                            allowWriteExternal,
                            limitPersonalAds,
                            limitProgrammaticAds
                        ))
                        .build()
                    // 初始化SDK
                    TTAdSdk.init(reactApplicationContext, adConfig)
                    // 启动SDK
                    TTAdSdk.start(object : TTAdSdk.Callback {
                        override fun success() {
                            isInitialized = true
                            // 保存初始化后的实例
                            mediationManager = TTAdSdk.getMediationManager()

                            // 注意：回调在子线程，需要切换到主线程进行UI操作
                            Handler(Looper.getMainLooper()).post {
                                promise.resolve(true)
                            }
                        }

                        override fun fail(code: Int, msg: String) {
                            Handler(Looper.getMainLooper()).post {
                                promise.reject("INIT_FAILED", "初始化失败: code=$code, msg=$msg", null)
                            }
                        }
                    })
                } catch (e: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        promise.reject("INIT_EXCEPTION", "初始化异常: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            promise.reject("INVALID_CONFIG", "配置解析失败: ${e.message}", e)
        }
    }

    /**
     * 创建自定义控制器，用于隐私设置
     */
    private fun createCustomController(
        allowLocation: Boolean,
        allowPhoneState: Boolean,
        allowWifiState: Boolean,
        allowAndroidId: Boolean,
        allowWriteExternal: Boolean,
        limitPersonalAds: Boolean,
        limitProgrammaticAds: Boolean
    ): TTCustomController {
        return object : TTCustomController() {
            override fun isCanUseLocation(): Boolean {
                return allowLocation
            }

            override fun isCanUsePhoneState(): Boolean {
                return allowPhoneState
            }

            override fun isCanUseWifiState(): Boolean {
                return allowWifiState
            }

            override fun isCanUseAndroidId(): Boolean {
                return allowAndroidId
            }

            override fun isCanUseWriteExternal(): Boolean {
                return allowWriteExternal
            }

            override fun getMediationPrivacyConfig(): MediationPrivacyConfig {
                return object : MediationPrivacyConfig() {
                    override fun isLimitPersonalAds(): Boolean {
                        return limitPersonalAds
                    }

                    override fun isProgrammaticRecommend(): Boolean {
                        return !limitProgrammaticAds
                    }
                }
            }
        }
    }

    /**
     * 检查SDK是否已初始化
     */
    @ReactMethod
    fun isSdkReady(promise: Promise) {
        promise.resolve(isInitialized && TTAdSdk.isSdkReady())
    }


    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }

    /**
     * TurboModule 接口要求的方法
     * 当模块被销毁时调用
     */
    override fun invalidate() {
        // 清理资源
        isInitialized = false
        mediationManager = null
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

