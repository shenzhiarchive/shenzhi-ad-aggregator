# 穿山甲广告SDK混淆规则

# 穿山甲主SDK
-keep class com.bytedance.sdk.** { *; }
-keep class com.pgl.sys.ces.** { *; }

# GDT优量汇
-keep class com.qq.e.** { *; }
-dontwarn com.qq.e.**

# 百度广告
-keep class com.baidu.mobads.** { *; }
-dontwarn com.baidu.mobads.**

# 快手广告
-keep class com.kwad.** { *; }
-dontwarn com.kwad.**

# Sigmob
-keep class com.sigmob.** { *; }
-dontwarn com.sigmob.**

# AdMob
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# 广告相关回调接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# 保留Serializable序列化的类不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留WebView相关类
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

