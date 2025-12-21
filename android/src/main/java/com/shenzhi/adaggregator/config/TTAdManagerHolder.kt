package com.shenzhi.adaggregator.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.bytedance.sdk.openadsdk.TTAdConfig
import com.bytedance.sdk.openadsdk.TTAdManager
import com.bytedance.sdk.openadsdk.TTAdSdk
import com.bytedance.sdk.openadsdk.TTCustomController
import com.bytedance.sdk.openadsdk.mediation.init.MediationConfigUserInfoForSegment
import java.util.HashMap

class TTAdManagerHolder {
    companion object {
        private const val TAG = "TTAdManagerHolder"

        private var sInit = false
        private var sStart = false

        fun get(): TTAdManager {
            return TTAdSdk.getAdManager()
        }

        fun init(context: Context) {
            //初始化穿山甲SDK
            doInit(context)
        }

        //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
        private fun doInit(context: Context) {
            if (sInit) {
                Toast.makeText(context, "您已经初始化过了", Toast.LENGTH_LONG).show()
                return
            }
            //TTAdSdk.init(context, buildConfig(context));
            //setp1.1：初始化SDK

            TTAdSdk.init(context, buildConfig(context))
            sInit = true
            Toast.makeText(context, "初始化成功", Toast.LENGTH_LONG).show()
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



        private fun buildConfig(context: Context): TTAdConfig {
            return TTAdConfig.Builder()
                /**
                 * 注：需要替换成在媒体平台申请的appID ，切勿直接复制
                 */
                .appId("5001121")
                .appName("APP测试媒体")
                /**
                 * 上线前需要关闭debug开关，否则会影响性能
                 */
                .debug(true)
                /**
                 * 使用聚合功能此开关必须设置为true，默认为false
                 */
                .useMediation(true)
                //                .customController(getTTCustomController()) //如果您需要设置隐私策略请参考该api
                //                .setMediationConfig(MediationConfig.Builder() //可设置聚合特有参数详细设置请参考该api
                //                        .setMediationConfigUserInfoForSegment(getUserInfoForSegment())//如果您需要配置流量分组信息请参考该api
                //                        .build())
                .build()
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
