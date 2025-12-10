package com.shenzhi.adaggregator.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Display
import android.view.DisplayCutout
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * UI工具类
 * Created by bytedance on 2019/9/5.
 */
object UIUtils {
    
    /**
     * 获取屏幕宽度（单位dp）
     */
    fun getScreenWidthDp(context: Context): Float {
        val scale = context.resources.displayMetrics.density
        val width = context.resources.displayMetrics.widthPixels
        return width / (if (scale <= 0) 1f else scale) + 0.5f
    }
    
    /**
     * 全面屏、刘海屏适配
     * 获取屏幕高度（单位dp）
     */
    fun getHeight(activity: Activity): Float {
        hideBottomUIMenu(activity)
        val realHeight = getRealHeight(activity)
        return if (hasNotchScreen(activity)) {
            px2dip(activity, (realHeight - getStatusBarHeight(activity)).toFloat()).toFloat()
        } else {
            px2dip(activity, realHeight.toFloat()).toFloat()
        }
    }
    
    /**
     * 隐藏底部UI菜单
     */
    @Suppress("DEPRECATION")
    fun hideBottomUIMenu(activity: Activity?) {
        if (activity == null) {
            return
        }
        try {
            // 隐藏虚拟按键，并且全屏
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
                // lower api
                val v = activity.window.decorView
                v.systemUiVisibility = View.GONE
            } else if (Build.VERSION.SDK_INT >= 19) {
                // for new api versions
                val decorView = activity.window.decorView
                val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        or View.SYSTEM_UI_FLAG_IMMERSIVE)
                decorView.systemUiVisibility = uiOptions
                activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取屏幕真实高度，不包含下方虚拟导航栏
     */
    @Suppress("DEPRECATION")
    fun getRealHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm)
        } else {
            display.getMetrics(dm)
        }
        return dm.heightPixels
    }
    
    /**
     * 获取状态栏高度
     */
    fun getStatusBarHeight(context: Context): Float {
        var height = 0f
        val resourceId = context.applicationContext.resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            height = context.applicationContext.resources.getDimensionPixelSize(resourceId).toFloat()
        }
        return height
    }
    
    /**
     * px转dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / (if (scale <= 0) 1f else scale) + 0.5f).toInt()
    }
    
    /**
     * dp转px
     */
    fun dp2px(context: Context, dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
    
    /**
     * 判断是否是刘海屏
     */
    fun hasNotchScreen(activity: Activity): Boolean {
        return isAndroidPHasNotch(activity)
                || getInt("ro.miui.notch", activity) == 1
                || hasNotchAtHuawei(activity)
                || hasNotchAtOPPO(activity)
                || hasNotchAtVivo(activity)
    }
    
    /**
     * Android P 刘海屏判断
     */
    fun isAndroidPHasNotch(activity: Activity): Boolean {
        var result = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            var displayCutout: DisplayCutout? = null
            try {
                val windowInsets = activity.window.decorView.rootWindowInsets
                displayCutout = windowInsets?.displayCutout
                if (displayCutout != null) {
                    result = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return result
    }
    
    /**
     * 小米刘海屏判断
     * @return 0 if it is not notch; return 1 means notch
     * @throws IllegalArgumentException if the key exceeds 32 characters
     */
    fun getInt(key: String, activity: Activity): Int {
        var result = 0
        if (isMiui()) {
            try {
                val classLoader = activity.classLoader
                val systemProperties = classLoader.loadClass("android.os.SystemProperties")
                val paramTypes = arrayOf<Class<*>>(
                    String::class.java,
                    Int::class.javaPrimitiveType ?: Int::class.java
                )
                val getIntMethod = systemProperties.getMethod("getInt", *paramTypes)
                val params = arrayOf<Any>(key, 0)
                result = getIntMethod.invoke(systemProperties, *params) as Int
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
        return result
    }
    
    /**
     * 华为刘海屏判断
     */
    fun hasNotchAtHuawei(context: Context): Boolean {
        var ret = false
        try {
            val classLoader = context.classLoader
            val hwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil")
            val getMethod = hwNotchSizeUtil.getMethod("hasNotchInScreen")
            ret = getMethod.invoke(hwNotchSizeUtil) as Boolean
        } catch (e: ClassNotFoundException) {
            // ignore
        } catch (e: NoSuchMethodException) {
            // ignore
        } catch (e: Exception) {
            // ignore
        }
        return ret
    }
    
    /**
     * VIVO刘海屏判断常量
     */
    const val VIVO_NOTCH = 0x00000020 // 是否有刘海
    const val VIVO_FILLET = 0x00000008 // 是否有圆角
    
    /**
     * VIVO刘海屏判断
     */
    fun hasNotchAtVivo(context: Context): Boolean {
        var ret = false
        try {
            val classLoader = context.classLoader
            val ftFeature = classLoader.loadClass("android.util.FtFeature")
            val method = ftFeature.getMethod("isFeatureSupport", Int::class.javaPrimitiveType)
            ret = method.invoke(ftFeature, VIVO_NOTCH) as Boolean
        } catch (e: ClassNotFoundException) {
            // ignore
        } catch (e: NoSuchMethodException) {
            // ignore
        } catch (e: Exception) {
            // ignore
        }
        return ret
    }
    
    /**
     * OPPO刘海屏判断
     */
    fun hasNotchAtOPPO(context: Context): Boolean {
        val temp = "com.kllk.feature.screen.heteromorphism"
        val name = getKllkDecryptString(temp)
        return context.packageManager.hasSystemFeature(name)
    }
    
    /**
     * 判断是否是MIUI系统
     */
    fun isMiui(): Boolean {
        var sIsMiui = false
        try {
            val clz = Class.forName("miui.os.Build")
            if (clz != null) {
                sIsMiui = true
                return sIsMiui
            }
        } catch (e: Exception) {
            // ignore
        }
        return sIsMiui
    }
    
    /**
     * 用于OPPO版本隐私协议
     */
    fun getKllkDecryptString(encryptionString: String): String {
        if (TextUtils.isEmpty(encryptionString)) {
            return ""
        }
        var decryptTag = ""
        val decryptCapitalized = "O" + "P" + "P" + "O"
        val decrypt = "o" + "p" + "p" + "o"
        decryptTag = when {
            encryptionString.contains("KLLK") -> encryptionString.replace("KLLK", decryptCapitalized)
            encryptionString.contains("kllk") -> encryptionString.replace("kllk", decrypt)
            else -> decryptTag
        }
        return decryptTag
    }
    
    /**
     * 设置View的尺寸
     */
    fun setViewSize(view: View, width: Int, height: Int) {
        when (val parent = view.parent) {
            is FrameLayout -> {
                val lp = view.layoutParams as FrameLayout.LayoutParams
                lp.width = width
                lp.height = height
                view.layoutParams = lp
                view.requestLayout()
            }
            is RelativeLayout -> {
                val lp = view.layoutParams as RelativeLayout.LayoutParams
                lp.width = width
                lp.height = height
                view.layoutParams = lp
                view.requestLayout()
            }
            is LinearLayout -> {
                val lp = view.layoutParams as LinearLayout.LayoutParams
                lp.width = width
                lp.height = height
                view.layoutParams = lp
                view.requestLayout()
            }
        }
    }
    
    /**
     * 获取屏幕宽度（单位px）
     */
    fun getScreenWidthInPx(context: Context): Int {
        val dm = context.applicationContext.resources.displayMetrics
        return dm.widthPixels
    }
    
    /**
     * 获取屏幕高度（单位px）
     */
    fun getScreenHeightInPx(context: Context): Int {
        val dm = context.applicationContext.resources.displayMetrics
        return dm.heightPixels
    }
    
    /**
     * 获取屏幕高度（包含状态栏）
     */
    fun getScreenHeight(context: Context): Int {
        return (getScreenHeightInPx(context) + getStatusBarHeight(context)).toInt()
    }
    
    /**
     * 从父View中移除
     */
    fun removeFromParent(view: View?) {
        if (view != null) {
            val vp = view.parent
            if (vp is ViewGroup) {
                vp.removeView(view)
            }
        }
    }
    
    /**
     * 获取全面屏宽高
     * @return int数组，[0]为宽度，[1]为高度
     */
    @Suppress("DEPRECATION")
    fun getScreenSize(context: Context?): IntArray {
        val size = intArrayOf(0, 0)
        if (context == null) {
            return size
        }
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val dm = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm)
        } else {
            display.getMetrics(dm)
        }
        size[0] = dm.widthPixels
        size[1] = dm.heightPixels
        return size
    }
}

