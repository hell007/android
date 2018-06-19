
## proguard-rules.pro文件细谈

proguard-rules.pro配置文件以及说明

    #==================================【基本配置】==================================
    
    # 代码混淆压缩比，在0~7之间，默认为5,一般不下需要修改
    -optimizationpasses 5
    
    # 混淆时不使用大小写混合，混淆后的类名为小写
    # windows下的同学还是加入这个选项吧(windows大小写不敏感)
    -dontusemixedcaseclassnames
    
    # 指定不去忽略非公共的库的类
    # 默认跳过，有些情况下编写的代码与类库中的类在同一个包下，并且持有包中内容的引用，此时就需要加入此条声明
    -dontskipnonpubliclibraryclasses
    
    # 指定不去忽略非公共的库的类的成员
    -dontskipnonpubliclibraryclassmembers
    
    # 不做预检验，preverify是proguard的四个步骤之一
    # Android不需要preverify，去掉这一步可以加快混淆速度
    -dontpreverify
    
    # 有了verbose这句话，混淆后就会生成映射文件
    -verbose
    
    #apk 包内所有 class 的内部结构
    -dump class_files.txt
    
    #未混淆的类和成员
    -printseeds seeds.txt
    
    #列出从 apk 中删除的代码
    -printusage unused.txt
    
    #混淆前后的映射
    -printmapping proguardMapping.txt
    
    # 指定混淆时采用的算法，后面的参数是一个过滤器
    # 这个过滤器是谷歌推荐的算法，一般不改变
    -optimizations !code/simplification/artithmetic,!field/*,!class/merging/*
    -optimizations !code/simplification/cast,!field/*,!class/merging/*
    
    # 保护代码中的Annotation不被混淆
    # 这在JSON实体映射时非常重要，比如fastJson
    -keepattributes *Annotation*,InnerClasses
    
    # 避免混淆泛型
    # 这在JSON实体映射时非常重要，比如fastJson
    -keepattributes Signature
    
    # 抛出异常时保留代码行号
    -keepattributes SourceFile,LineNumberTable
    
    #忽略警告
    -ignorewarning
    #----------------------------------------------------------------------------
    
    #==================================【项目配置-默认保留区】==================================
    
    # 保留了继承自Activity、Application这些类的子类
    -keep public class * extends android.app.Activity
    -keep public class * extends android.app.Application
    -keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.app.backup.BackupAgentHelper
    -keep public class * extends android.content.ContentProvider
    -keep public class * extends android.preference.Preference
    -keep public class * extends android.view.View
    -keep public class * extends android.database.sqlite.SQLiteOpenHelper{*;}
    -keep public class com.android.vending.licensing.ILicensingService
    -keep class android.support.** {*;}
    -keep class com.actionbarsherlock.** { *; }
    -keep interface com.actionbarsherlock.** { *; }
    -keepattributes *Annotation*

    # 保留所有的本地native方法不被混淆
    -keepclasseswithmembernames class * {
        native <methods>;
    }
    
    # 如果有引用android-support-v4.jar包，可以添加下面这行
    -keep public class com.null.test.ui.fragment.** {*;}
    
    #如果引用了v4或者v7包
    -dontwarn android.support.**
    
    # 保留Activity中的方法参数是view的方法，
    # 从而我们在layout里面编写onClick就不会影响
    -keepclassmembers class * extends android.app.Activity {
        public void * (android.view.View);
    }
    
    # 枚举类不能被混淆
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }
    
    # 保留自定义控件(继承自View)不能被混淆
    -keep public class * extends android.view.View {
        public <init>(android.content.Context);
        public <init>(android.content.Context, android.util.AttributeSet);
        public <init>(android.content.Context, android.util.AttributeSet, int);
        public void set*(***);
        *** get* ();
    }
    
    # 保留Parcelable序列化的类不能被混淆
    -keep class * implements android.os.Parcelable{
        public static final android.os.Parcelable$Creator *;
    }
    
    # 保留Serializable 序列化的类不被混淆
    -keepclassmembers class * implements java.io.Serializable {
       static final long serialVersionUID;
       private static final java.io.ObjectStreamField[] serialPersistentFields;
       !static !transient <fields>;
       private void writeObject(java.io.ObjectOutputStream);
       private void readObject(java.io.ObjectInputStream);
       java.lang.Object writeReplace();
       java.lang.Object readResolve();
    }
    
    # 对R文件下的所有类及其方法，都不能被混淆
    -keepclassmembers class **.R$* {
        *;
    }
    
    # 对于带有回调函数onXXEvent的，不能混淆
    -keepclassmembers class * {
        void *(**On*Event);
    }
    
    
    #==================================【定制化区域】==================================
    
    #---------------------------------1.实体类---------------------------------
    #保留model实体
    -keep class com.jie.flatframe.network.**{*;}
    -keep class com.jie.flatframe.ui.fixationstation.bean.**{*;}
    -keep class com.jie.flatframe.ui.login.bean.**{*;}
    -keep class com.jie.flatframe.ui.mobilestation.bean.**{*;}
    #-------------------------------------------------------------------------
    -keep class com.jie.flatframe.ui.widget.**{*;}
    -keep class com.jie.flatframe.utils.**{*;}
    -keep class com.jie.flatframe.base.**{*;}
    -keep public class  * extends com.jie.flatframe.base.BaseModel
    -keep public class  * extends com.jie.flatframe.base.IBaseControl
    -keepclassmembers public class * extends com.jie.flatframe.base.BaseActivity
    #内部方法
    -keepattributes EnclosingMethod
    
    
    #---------------------------------2.第三方配置--------------------------------- 
    #-libraryjars libs/BASE64Encoder.jar
    #-libraryjars libs/sun.misc.BASE64Decoder.jar
    #保留第三方代码
    -keep class com.qiangxi.checkupdatelibrary.**{*;}
    -keep class com.robin.lazy.cache.**{*;}
    -keep class com.github.yoojia.**{*;}
    -keep class com.nostra13.universalimageloader.**{*;}
    -keep class com.open.androidtvwidget.**{*;}

    #databinding
    -dontwarn android.databinding.**
    -keep class android.databinding.** { *; }

    #Utils工具类
    -keep class com.blankj.utilcode.** { *; }
    -keepclassmembers class com.blankj.utilcode.** { *; }
    -dontwarn com.blankj.utilcode.**

    #glide
    -keep public class * implements com.bumptech.glide.module.GlideModule
    -keep public class * extends com.bumptech.glide.module.AppGlideModule
    -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
      **[] $VALUES;
      public *;
    }
    # for DexGuard only
    #-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

    #zxing
    -dontwarn com.google.zxing.**
    -keep  class com.google.zxing.**{*;}

    # OkHttp3
    -dontwarn okhttp3.logging.**
    -keep class okhttp3.internal.**{*;}
    -dontwarn okio.**
    # Retrofit
    -dontwarn retrofit2.**
    -keep class retrofit2.** { *; }
    -keepattributes Signature
    -keepattributes Exceptions
    # Gson
    -keep class com.google.gson.stream.** { *; }
    -keepattributes EnclosingMethod

    # RxJava RxAndroid
    -dontwarn sun.misc.**
    -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
        long producerIndex;
        long consumerIndex;
    }
    -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
        rx.internal.util.atomic.LinkedQueueNode producerNode;
    }
    -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
        rx.internal.util.atomic.LinkedQueueNode consumerNode;
    }
    #GreenDao
    -keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
        public static void dropTable(org.greenrobot.greendao.database.Database, boolean);
        public static void createTable(org.greenrobot.greendao.database.Database, boolean);
    }
    #----------------------------------其他---------------------------------------
    
    #环信混淆
    -keep class com.easemob.** {*;}
    -keep class org.jivesoftware.** {*;}
    -keep class org.apache.** {*;}
    -dontwarn  com.easemob.**
    #另外，demo中发送表情的时候使用到反射，需要keep SmileUtils
    -keep class com.easemob.chatuidemo.utils.SmileUtils {*;}
    #注意前面的包名，如果把这个类复制到自己的项目底下，比如放在com.example.utils底下，应该这么写（实际要去掉#）
    #-keep class com.example.utils.SmileUtils {*;}
    #如果使用EaseUI库，需要这么写
    -keep class com.easemob.easeui.utils.EaseSmileUtils {*;}
    #2.0.9后加入语音通话功能，如需使用此功能的API，加入以下keep
    -dontwarn ch.imvs.**
    -dontwarn org.slf4j.**
    -keep class org.ice4j.** {*;}
    -keep class net.java.sip.** {*;}
    -keep class org.webrtc.voiceengine.** {*;}
    -keep class org.bitlet.** {*;}
    -keep class org.slf4j.** {*;}
    -keep class ch.imvs.** {*;}
    -keep class com.hyphenate.** {*;}
    -dontwarn  com.hyphenate.**
    #okhttp
    -keep class com.squareup.okhttp.** { *;}
    -dontwarn okio.**
    -keepclassmembers class **.R$* {
        public static <fields>;
    }
    #eventbus
    -keepattributes *Annotation*
    -keepclassmembers class ** {
        @org.greenrobot.eventbus.Subscribe <methods>;
    }
    -keep enum org.greenrobot.eventbus.ThreadMode { *; }
    # Only required if you use AsyncExecutor
    -keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
        <init>(java.lang.Throwable);
    }
    #友盟分享
     -dontwarn com.google.android.maps.**
     -dontwarn android.webkit.WebView
     -dontwarn com.umeng.**
     -dontwarn com.tencent.weibo.sdk.**
     -dontwarn com.facebook.**
     -keep public class javax.**
     -keep public class android.webkit.**
     -dontwarn android.support.v4.**
     -keep class android.support.** {*;}
     -keep enum com.facebook.**
     -keepattributes Exceptions,InnerClasses,Signature
     -keepattributes *Annotation*
     -keepattributes SourceFile,LineNumberTable
     -keep public interface com.facebook.**
     -keep public interface com.tencent.**
     -keep public interface com.umeng.socialize.**
     -keep public interface com.umeng.socialize.sensor.**
     -keep public interface com.umeng.scrshot.**
    #视频直播混淆
    -dontwarn com.gensee.**
    -keep  class  com.gensee.**{*;}
    -dontwarn com.tictactec.ta.**
    #fastjson
    -dontwarn com.alibaba.fastjson.**
    -keep class com.alibaba.fastjson.** { *; }
    # fresco
    -keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
    -keep @com.facebook.common.internal.DoNotStrip class *
    -keepclassmembers class * {@com.facebook.common.internal.DoNotStrip *;}
    -keep class com.facebook.imagepipeline.animated.factory.AnimatedFactoryImpl {
        public AnimatedFactoryImpl(com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory, com.facebook.imagepipeline.core.ExecutorSupplier);
    }
    -keep class com.facebook.animated.gif.** {*;}
    -dontwarn javax.annotation.**
    #保留混淆mapping文件
    -printmapping build/outputs/mapping/release/mapping.txt

    #growingio统计
    -keep class com.growingio.android.sdk.** {
        *;
    }
    -dontwarn com.growingio.android.sdk.**
    -keepnames class * extends android.view.View
    -keep class * extends android.app.Fragment {
        public void setUserVisibleHint(boolean);
        public void onHiddenChanged(boolean);
        public void onResume();
        public void onPause();
    }
    -keep class android.support.v4.app.Fragment {
        public void setUserVisibleHint(boolean);
        public void onHiddenChanged(boolean);
        public void onResume();
        public void onPause();
    }
    -keep class * extends android.support.v4.app.Fragment {
        public void setUserVisibleHint(boolean);
        public void onHiddenChanged(boolean);
        public void onResume();
        public void onPause();
    }
    #-------------------------------------------------------------------------

    #---------------------------------3.与js互相调用的类------------------------
    -keepattributes *JavascriptInterface*

    #-------------------------------------------------------------------------

    #---------------------------------4.反射相关的类和方法-----------------------
    
