package de.reckendrees.systems.alert;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;
import android.os.Build;
import android.os.Handler;


import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressWarnings("unused")
public class Hook implements IXposedHookLoadPackage {
    private static final Class<?> ACTIVITY_THREAD_CLS = XposedHelpers.findClass("android.app.ActivityThread", null);
    public XSharedPreferences pref;
    private boolean camera, mic, clipboard = true;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        XSharedPreferences pref = new XSharedPreferences(Hook.class.getPackage().getName(), "pref_mine");
        pref.makeWorldReadable();
        pref.reload();

        camera = pref.getBoolean("hook_Camera",true);
        mic = pref.getBoolean("hook_Microphone",true);

        clipboard = pref.getBoolean("hook_Clipboard",true);
        if(camera){
            hookCameraApi(param.classLoader);
            hookCamera2Api(param.classLoader);
        }
        if(mic){
            log("hooked mic!");
            hookMediaRecorder(param.classLoader);
        }
        if(clipboard){
            hookClipboard(param.classLoader);
        }

    }


    private void hookClipboard(ClassLoader classLoader){
        //final Class<?> clazz = XposedHelpers.findClass("android.text.ClipboardManager", classLoader);
        final XC_MethodHook edit = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                sendBroadcast((Context) AndroidAppHelper.currentApplication(), "edited clipboard");

            }
        };
        final XC_MethodHook access = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param)
                    throws Throwable {
                sendBroadcast((Context) AndroidAppHelper.currentApplication(), "accessed clipboard");

            }
        };
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            try {
                XposedHelpers.findAndHookMethod(ClipboardManager.class, "getPrimaryClip", access);
            } catch (java.lang.NoSuchMethodError e) {
                log("failed to hook getPrimaryClip");
                log(e.toString());
            }
            try {
                XposedHelpers.findAndHookMethod(ClipboardManager.class, "setPrimaryClip", ClipData.class, edit);
            } catch (java.lang.NoSuchMethodError e) {
                log("failed to hook setPrimaryClip");
                log(e.toString());
            }
        }
        else{
            //final Class<?> clazz2 = XposedHelpers.findClass("android.content.ClipboardManager", classLoader);
            try{
                XposedHelpers.findAndHookMethod(ClipData.class, "getText", access);
            }catch(java.lang.NoSuchMethodError e){
                log("failed to hook getPrimaryClip legacy");
                log(e.toString());
            }
            try{
                XposedHelpers.findAndHookMethod(ClipData.class, "setText", ClipData.class, edit);
            }catch(java.lang.NoSuchMethodError e){
                log("failed to hook setPrimaryClip legacy");
                log(e.toString());
            }
        }

    }

    @SuppressWarnings("deprecation")
    private void hookCameraApi(ClassLoader classLoader) {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) {
                    return;
                }

                final int sourceId = (int) param.args[0];
                final Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(sourceId, cameraInfo);

                String facingStr;
                switch (cameraInfo.facing) {
                    case Camera.CameraInfo.CAMERA_FACING_FRONT:
                        facingStr = "front";
                        break;
                    case Camera.CameraInfo.CAMERA_FACING_BACK:
                        facingStr = "back";
                        break;
                    default:
                        facingStr = "unknown";
                        break;
                }

                sendBroadcast(getCurrentApplication(), String.format("used camera(%s)", facingStr));
            }
        };
        try{
            XposedHelpers.findAndHookConstructor(Camera.class, int.class, hook);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook camera legacy");
        }
    }

    private void hookMediaRecorder(ClassLoader classLoader) {
        final XC_MethodHook hooker = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log("HOOOOOK");
                final Context context = getCurrentApplication();
                sendBroadcast(context, "used mic");
                log("used mic!");
            }
        };
        try{
            XposedHelpers.findAndHookMethod(MediaRecorder.class, "setAudioSource", "int", hooker);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook setAudioSource");
        }
       /* try{
            XposedHelpers.findAndHookMethod(AudioRecord.class, "read", short[].class,"int","int",hooker);
            XposedHelpers.findAndHookMethod(AudioRecord.class, "read", short[].class,"int","int", "int",hooker);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook AudioRecord read");
            log(e.toString());
        }
        try{
            XposedHelpers.findAndHookMethod(AudioRecord.class, "native_read_in_short_array",short[].class ,"int", "int", "boolean",hooker);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook AudioRecord native_read_in_short_array");
            log(e.toString());
        }*/

        try{
            XposedHelpers.findAndHookMethod(AudioRecord.class, "startRecording", hooker);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook startRecording");
        }
        try{
            XposedHelpers.findAndHookMethod(AudioRecord.class, "startRecording", MediaSyncEvent.class, hooker);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook startRecording MediaSyncEvent");
        }
    }

    private void hookCamera2Api(ClassLoader classLoader) {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.hasThrowable()) {
                    return;
                }
                final CameraManager manager = (CameraManager) param.thisObject;
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics((String) param.args[0]);
                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing == null) {
                    return;
                }

                String facingStr;
                switch (facing) {
                    case CameraCharacteristics.LENS_FACING_FRONT:
                        facingStr = "front";
                        break;
                    case CameraCharacteristics.LENS_FACING_BACK:
                        facingStr = "back";
                        break;
                    case CameraCharacteristics.LENS_FACING_EXTERNAL:
                        facingStr = "external";
                        break;
                    default:
                        facingStr = "unknown";
                        break;
                }

                sendBroadcast((Context) AndroidAppHelper.currentApplication(), String.format("used camera(%s)", facingStr));
            }
        };
        try{
            XposedHelpers.findAndHookMethod(CameraManager.class, "openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, hook);
        }catch(java.lang.NoSuchMethodError e){
            log("failed to hook camera");
        }
    }

    private static Application getCurrentApplication() {
        return (Application) XposedHelpers.callStaticMethod(ACTIVITY_THREAD_CLS, "currentApplication");
    }

    private static void log(String text){
        XposedBridge.log("[TurtleAlert]: "+text);
    }
    private static void log(int text){ XposedBridge.log(String.valueOf(text));}

    private static void sendBroadcast(Context context, String part) {
        final String message = String.format("%s %s", getApplicationName(context),part);
        log(message);
        Intent i = new Intent();
        i.setComponent(new ComponentName("de.reckendrees.systems.alert", "de.reckendrees.systems.alert.HUD"));
        i.putExtra("message", message);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ComponentName c = context.startForegroundService(i);
        }else{
            ComponentName c = context.startService(i);
        }
        /*try{
            Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            View toastView = toast.getView();
            TextView textView = (TextView)toastView.findViewById(android.R.id.message);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(Color.parseColor("#1f1f1f"));
            toastView.setBackgroundColor(Color.parseColor("#F44336"));
            toast.show();
        }catch(Exception e){
            log("failed to display toast in context. next try with main looper.");
            log(e.toString());
            try{
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Toast toast = Toast.makeText(getCurrentApplication(), message, Toast.LENGTH_SHORT);
                            View toastView = toast.getView();
                            TextView textView = (TextView)toastView.findViewById(android.R.id.message);
                            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            textView.setTextColor(Color.parseColor("#1f1f1f"));
                            toastView.setBackgroundColor(Color.parseColor("#F44336"));
                            toast.show();
                        }catch(Exception e){
                            log("failed to display toast");
                        }

                    }
                });
            }catch(Exception f){
                log("failed to display toast");
            }
        }*/

    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}
