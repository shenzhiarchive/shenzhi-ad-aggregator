package com.shenzhi.adaggregator.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdConstant
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.init.MediationConfigUserInfoForSegment
import java.util.HashMap

data class SDKConfigParams(
    val appId: String,
    val appName: String? = null,
    val debug: Boolean = false,
    val useMediation: Boolean = true,
    val allowShowNotify: Boolean = true,
    val supportMultiProcess: Boolean = false,
    val titleBarTheme: Int = TTAdConstant.TITLE_BAR_THEME_DARK
)

class TTAdManagerHolder {
    companion object {
        private const val TAG = "TTAdManagerHolder"

        @Volatile
        private var sInit = false

        @Volatile
        private var sInitInProgress = false

        private var sStart = false

        fun get(): TTAdManager {
            return TTAdSdk.getAdManager()
        }

        fun init(context: Context) {
            //初始化穿山甲SDK
            doInit(context, SDKConfigParams(appId = "5001121", appName = "APP测试媒体", debug = true, useMediation = true))
        }

        /**
         * 初始化SDK，支持从RN传递配置参数
         * @param context 应用上下文
         * @param config 配置参数
         * @param callback 初始化完成回调（可选，成功时调用）
         * @param errorCallback 初始化失败回调（可选，失败时调用）
         * @throws IllegalStateException 如果SDK已经初始化
         */
        fun initialize(
            context: Context,
            config: SDKConfigParams,
            callback: (() -> Unit)? = null,
            errorCallback: ((Throwable) -> Unit)? = null
        ) {
            // 确保在主线程执行
            if (Looper.myLooper() != Looper.getMainLooper()) {
                android.os.Handler(Looper.getMainLooper()).post {
                    try {
                        doInit(context, config)
                        callback?.invoke()
                    } catch (e: Exception) {
                        errorCallback?.invoke(e)
                    }
                }
            } else {
                try {
                    doInit(context, config)
                    callback?.invoke()
                } catch (e: Exception) {
                    errorCallback?.invoke(e)
                }
            }
        }

        /**
         * 检查SDK是否已初始化
         */
        fun isInitialized(): Boolean {
            return sInit
        }

        //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
        private fun doInit(context: Context, config: SDKConfigParams) {
            synchronized(this) {
                if (sInit) {
                    Log.w(TAG, "SDK已经初始化过了")
                    return
                }

                if (sInitInProgress) {
                    Log.w(TAG, "SDK正在初始化中，请勿重复调用")
                    return
                }

                sInitInProgress = true
            }

            try {
                Log.i(TAG, "开始初始化穿山甲SDK，appId: ${config.appId}")
                //setp1.1：初始化SDK
                val adConfig = buildConfig(context, config)
                TTAdSdk.init(context, adConfig)

                synchronized(this) {
                    sInit = true
                    sInitInProgress = false
                }

                Log.i(TAG, "穿山甲SDK初始化成功")
            } catch (e: Exception) {
                synchronized(this) {
                    sInitInProgress = false
                }
                Log.e(TAG, "穿山甲SDK初始化失败", e)
                throw e
            }
        }

        fun start(context: Context) {
            if (!sInit) {
                Toast.makeText(context, "还没初始化SDK，请先进行初始化", Toast.LENGTH_LONG).show()
                return
            }
            if (sStart) {
                return
            }
            //setp1.2：启动SDK

            TTAdSdk.start(object : TTAdSdk.Callback {
                override fun success() {
                    Log.i(TAG, "success: ${TTAdSdk.isSdkReady()}")
                }

                override fun fail(code: Int, msg: String) {
                    sStart = false
                    Log.i(TAG, "fail:  code = $code msg = $msg")
                }
            })
            sStart = true
        }



        private fun buildConfig(context: Context, config: SDKConfigParams): TTAdConfig {
            val builder = TTAdConfig.Builder()
                .appId(config.appId)

            // 设置应用名称（如果提供）
            config.appName?.let {
                builder.appName(it)
            }

            // 设置调试模式
            builder.debug(config.debug)

            // 使用聚合功能此开关必须设置为true，默认为false
            builder.useMediation(config.useMediation)

            // 允许显示通知
            builder.allowShowNotify(config.allowShowNotify)

            // 支持多进程
            builder.supportMultiProcess(config.supportMultiProcess)

            // 标题栏主题
            builder.titleBarTheme(config.titleBarTheme)

            // 可选：自定义控制器（隐私策略等）
            // builder.customController(getTTCustomController())

            // 可选：聚合配置
            // builder.setMediationConfig(MediationConfig.Builder()
            //     .setMediationConfigUserInfoForSegment(getUserInfoForSegment())
            //     .build())

            return builder.build()
        }

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
    }
}
