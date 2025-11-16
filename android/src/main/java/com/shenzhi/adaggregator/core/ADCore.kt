package com.shenzhi.adaggregator.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.IMediationManager
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig

/**
 * 广告核心管理类
 * 负责SDK初始化、TTAdManager实例管理等基础逻辑
 */
object ADCore {
    
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var mediationManager: IMediationManager? = null
    
    /**
     * 初始化配置参数
     */
    data class InitConfig(
        val appId: String,
        val debug: Boolean = false,
        val supportMultiProcess: Boolean = false,
        val allowLocation: Boolean = true,
        val allowPhoneState: Boolean = true,
        val allowWifiState: Boolean = true,
        val allowAndroidId: Boolean = true,
        val allowWriteExternal: Boolean = true,
        val themeStatus: Int = 0,
        val limitPersonalAds: Boolean = false,
        val limitProgrammaticAds: Boolean = false
    )
    
    /**
     * 初始化回调接口
     */
    interface InitCallback {
        fun onSuccess()
        fun onFail(code: Int, msg: String)
    }
    
    /**
     * 初始化穿山甲融合SDK
     *
     * @param context 应用上下文
     * @param config 配置参数
     * @param callback 初始化回调
     */
    fun init(
        context: Context,
        config: InitConfig,
        callback: InitCallback
    ) {
        if (isInitialized) {
            callback.onSuccess()
            return
        }
        
        // 在主线程中调用初始化
        Handler(Looper.getMainLooper()).post {
            try {
                // 构建TTAdConfig
                val adConfig = TTAdConfig.Builder()
                    .appId(config.appId)
                    .useMediation(true) // 开启聚合功能
                    .supportMultiProcess(config.supportMultiProcess)
                    .debug(config.debug)
                    .titleBarTheme(config.themeStatus)
                    .customController(createCustomController(config))
                    .build()
                
                // 初始化SDK
                TTAdSdk.init(context, adConfig)
                
                // 启动SDK
                TTAdSdk.start(object : TTAdSdk.Callback {
                    override fun success() {
                        isInitialized = true
                        // 保存初始化后的实例
                        mediationManager = TTAdSdk.getMediationManager()
                        
                        // 注意：回调在子线程，需要切换到主线程进行UI操作
                        Handler(Looper.getMainLooper()).post {
                            callback.onSuccess()
                        }
                    }
                    
                    override fun fail(code: Int, msg: String) {
                        Handler(Looper.getMainLooper()).post {
                            callback.onFail(code, msg)
                        }
                    }
                })
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    callback.onFail(-1, "初始化异常: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 获取TTAdManager实例
     * 
     * @return TTAdManager实例，如果未初始化则返回null
     */
    fun getTTAdManager(): TTAdManager? {
        return if (isInitialized) {
            TTAdSdk.getAdManager()
        } else {
            null
        }
    }
    
    /**
     * 获取MediationManager实例
     * 
     * @return IMediationManager实例，如果未初始化则返回null
     */
    fun getMediationManager(): IMediationManager? {
        return mediationManager
    }
    
    /**
     * 检查SDK是否已初始化
     * 
     * @return true表示已初始化，false表示未初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized && TTAdSdk.isSdkReady()
    }
    
    /**
     * 检查SDK是否就绪
     * 
     * @return true表示SDK已就绪，false表示未就绪
     */
    fun isSdkReady(): Boolean {
        return isInitialized && TTAdSdk.isSdkReady()
    }
    
    /**
     * 创建自定义控制器，用于隐私设置
     */
    private fun createCustomController(config: InitConfig): TTCustomController {
        return object : TTCustomController() {
            override fun isCanUseLocation(): Boolean {
                return config.allowLocation
            }
            
            override fun isCanUsePhoneState(): Boolean {
                return config.allowPhoneState
            }
            
            override fun isCanUseWifiState(): Boolean {
                return config.allowWifiState
            }
            
            override fun isCanUseAndroidId(): Boolean {
                return config.allowAndroidId
            }
            
            override fun isCanUseWriteExternal(): Boolean {
                return config.allowWriteExternal
            }
            
            override fun getMediationPrivacyConfig(): MediationPrivacyConfig {
                return object : MediationPrivacyConfig() {
                    override fun isLimitPersonalAds(): Boolean {
                        return config.limitPersonalAds
                    }
                    
                    override fun isProgrammaticRecommend(): Boolean {
                        return !config.limitProgrammaticAds
                    }
                }
            }
        }
    }
    
    /**
     * 重置初始化状态（用于测试或重新初始化）
     */
    fun reset() {
        isInitialized = false
        mediationManager = null
    }
}

