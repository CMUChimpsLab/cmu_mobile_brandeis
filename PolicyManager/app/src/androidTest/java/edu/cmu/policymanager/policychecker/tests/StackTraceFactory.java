package edu.cmu.policymanager.policychecker.tests;

import java.util.ArrayList;
import java.util.List;

/**
 * Create different kinds of stacktraces for unit testing. These
 * are based on stacktraces I already collected in the past from
 * the stacktrace test app.
 * */
public class StackTraceFactory {
    public static List<StackTraceElement[]> createNoPurposeStackraces() {
        StackTraceElement[] stacktraces = new StackTraceElement[12];
        stacktraces[0] = new StackTraceElement("java.lang.Object", "wait", "", 1);
        stacktraces[1] = new StackTraceElement("java.lang.Thread", "parkFor$", "Thread.java", 2142);
        stacktraces[2] = new StackTraceElement("sun.misc.Unsafe", "park", "Unsafe.java", 358);
        stacktraces[3] = new StackTraceElement("java.util.concurrent.locks.LockSupport", "parkNanos", "LockSupport.java", 230);
        stacktraces[4] = new StackTraceElement("java.util.concurrent.SynchronousQueue$TransferStack", "awaitFulfill", "SynchronousQueue.java",461);
        stacktraces[5] = new StackTraceElement("java.util.concurrent.SynchronousQueue$TransferStack", "transfer", "SynchronousQueue.java", 362);
        stacktraces[6] = new StackTraceElement("java.util.concurrent.SynchronousQueue", "poll", "SynchronousQueue.java", 937);
        stacktraces[7] = new StackTraceElement("java.util.concurrent.ThreadPoolExecutor", "getTask", "ThreadPoolExecutor.java", 1086);
        stacktraces[8] = new StackTraceElement("java.util.concurrent.ThreadPoolExecutor", "runWorker", "ThreadPoolExecutor.java", 1147);
        stacktraces[9] = new StackTraceElement("java.util.concurrent.ThreadPoolExecutor$Worker", "run", "ThreadPoolExecutor.java", 636);
        stacktraces[10] = new StackTraceElement("X.0Y7", "run", "", 88624);
        stacktraces[11] = new StackTraceElement("java.lang.Thread", "run", "Thread.java", 766);

        List<StackTraceElement[]> stacktraceList = new ArrayList<StackTraceElement[]>(1);
        stacktraceList.add(stacktraces);

        return stacktraceList;
    }

    public static List<StackTraceElement[]> createAdvertisingStacktrace() {
        StackTraceElement[] stacktraces = new StackTraceElement[19];
        stacktraces[0] = new StackTraceElement("android.location.LocationManager", "getLastKnownLocation", "LocationManager.java",1414);
        stacktraces[1] = new StackTraceElement("com.mopub.common.LocationService", "getLocationFromProvider", "LocationService.java",218);
        stacktraces[2] = new StackTraceElement("com.mopub.common.LocationService", "getLastKnownLocation", "LocationService.java",181);
        stacktraces[3] = new StackTraceElement("com.mopub.common.AdUrlGenerator", "setLocation", "AdUrlGenerator.java",207);
        stacktraces[4] = new StackTraceElement("com.mopub.common.AdUrlGenerator", "addBaseParams", "AdUrlGenerator.java",336);
        stacktraces[5] = new StackTraceElement("com.mopub.mobileads.WebViewAdUrlGenerator", "generateUrlString", "WebViewAdUrlGenerator.java",30);
        stacktraces[6] = new StackTraceElement("com.mopub.mobileads.AdViewController", "generateAdUrl", "AdViewController.java",582);
        stacktraces[7] = new StackTraceElement("com.mopub.mobileads.AdViewController", "internalLoadAd", "AdViewController.java",262);
        stacktraces[8] = new StackTraceElement("com.mopub.mobileads.AdViewController", "loadAd", "AdViewController.java",244);
        stacktraces[9] = new StackTraceElement("com.mopub.mobileads.MoPubView", "loadAd", "MoPubView.java",213);
        stacktraces[10] = new StackTraceElement("edu.cmu.chimpslab.stacktracetest.MainActivity$2", "onInitializationFinished", "MainActivity.java",89);
        stacktraces[11] = new StackTraceElement("com.mopub.common.MoPub$1", "run", "MoPub.java",377);
        stacktraces[12] = new StackTraceElement("android.os.Handler", "handleCallback", "Handler.java",873);
        stacktraces[13] = new StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java",99);
        stacktraces[14] = new StackTraceElement("android.os.Looper", "loop", "Looper.java",193);
        stacktraces[15] = new StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java",6718);
        stacktraces[16] = new StackTraceElement("java.lang.reflect.Method", "invoke", "",1);
        stacktraces[17] = new StackTraceElement("com.android.internal.os.RuntimeInit$MethodAndArgsCaller", "run", "RuntimeInit.java",493);
        stacktraces[18] = new StackTraceElement("com.android.internal.os.ZygoteInit", "main", "ZygoteInit.java", 858);

        List<StackTraceElement[]> stacktraceList = new ArrayList<StackTraceElement[]>(1);
        stacktraceList.add(stacktraces);

        return stacktraceList;
    }

    public static List<StackTraceElement[]> createInternalUseMicrophoneStacktrace() {
        StackTraceElement[] stacktraces = new StackTraceElement[8];
        stacktraces[0] = new StackTraceElement("android.media.MediaRecorder", "start", "", 1);
        stacktraces[1] = new StackTraceElement("edu.cmu.chimpslab.stacktracetest.MainActivity", "onCreate", "MainActivity.java", 1);
        stacktraces[2] = new StackTraceElement("android.os.Handler", "dispatchMessage", "Handler.java", 106);
        stacktraces[3] = new StackTraceElement("android.os.Looper", "loop", "Looper.java", 164);
        stacktraces[4] = new StackTraceElement("android.app.ActivityThread", "main", "ActivityThread.java", 6494);
        stacktraces[5] = new StackTraceElement("java.lang.reflect.Method", "invoke", "", 1);
        stacktraces[6] = new StackTraceElement("com.android.internal.os.RuntimeInit$MethodAndArgsCaller", "run", "RuntimeInit.java", 438);
        stacktraces[7] = new StackTraceElement("com.android.internal.os.ZygoteInit", "main", "ZygoteInit.java", 807);

        List<StackTraceElement[]> stacktraceList = new ArrayList<StackTraceElement[]>(1);
        stacktraceList.add(stacktraces);

        return stacktraceList;
    }
}