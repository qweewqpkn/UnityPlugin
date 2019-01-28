package com.jiuyou.game99;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class MainActivity extends UnityPlayerActivity {

    private static final int TAKE_PHOTO = 1;
    private static final int OPEN_GALLERY = 2;
    private static final int CROP_PHOTO = 3;
    private Uri mPhotoUri;
    private Uri mCropPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("unity", "手机MANUFACTURER : " + Build.MANUFACTURER);
        Log.e("unity", "手机BRAND : " + Build.BRAND);
        Log.e("unity", "手机PRODUCT : " + Build.PRODUCT);
        Log.e("unity", "手机MODEL : " + Build.MODEL);
    }

    public void TakePhoto() {
        mPhotoUri = GetUri(CreateFile("temp.png"));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        startActivityForResult(intent, TAKE_PHOTO);

    }

    //调用相册
    public void OpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, OPEN_GALLERY);
    }

    private Uri GetUri(File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

    private File CreateFile(String name) {
        File file = new File(Environment.getExternalStorageDirectory(), name);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    private void StartCrop(Uri inputUri) {
        mCropPhotoUri = Uri.fromFile(CreateFile("tempCrop.png"));

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setDataAndType(inputUri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCropPhotoUri);
        startActivityForResult(intent, CROP_PHOTO);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("unity", "user cancel operator!!");
            return;
        }

        switch (requestCode) {
            case TAKE_PHOTO: {
                StartCrop(mPhotoUri);
            }
            break;
            case OPEN_GALLERY: {
                Uri uri = data.getData();
                StartCrop(uri);
            }
            break;
            case CROP_PHOTO: {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mCropPhotoUri));
                    FileOutputStream fOut = null;

                    try {
                        String path = "/mnt/sdcard/Android/data/" + getPackageName() + "/files";

                        File destDir = new File(path);
                        if (!destDir.exists()) {
                            destDir.mkdirs();
                        }

                        fOut = new FileOutputStream(path + "/" + "image.png");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        try {
                            fOut.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        UnityPlayer.UnitySendMessage("UnityPlugin", "OnGetPhoto", "image.png");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
    }


    //获取刘海屏的高度(为0则不是刘海屏幕)
    public int GetNotchHeight()
    {
        String str = Build.MANUFACTURER.toUpperCase();
        switch (str)
        {
            case "HUAWEI":
            {
                if(IsNotchHuawei(this))
                {
                    Log.e("unity", "GetNotchHeight HUAWEI enter");
                    return GetNotchHeightHuawei(this);
                }
            }
            break;
            case "XIAOMI":
            {
                if(IsNotchXiaomi(this))
                {
                    Log.e("unity", "GetNotchHeight XIAOMI enter");
                    return GetNotchHeightXiaomi(this);
                }
            }
            break;
            case "OPPO":
            {
                if(IsNotchInOppo(this))
                {
                    Log.e("unity", "GetNotchHeight OPPO enter");
                    return GetNotchHeightOppo(this);
                }
            }
            break;
            case "VIVO":
            {
                if(IsNotchVivo(this))
                {
                    Log.e("unity", "GetNotchHeight VIVO enter");
                    return GetNotchHeightVivo(this);
                }
            }
            break;
        }

        Log.e("unity", "GetNotchHeight is 0");
        return 0;
    }

    //手机适配start
    //判断华为是否是刘海屏
    public static boolean IsNotchHuawei(Context context)
    {
        boolean isNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method hasNotchInScreen = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            if (hasNotchInScreen != null) {
                isNotch = (boolean) ((Method) hasNotchInScreen).invoke(HwNotchSizeUtil);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isNotch;
    }

    //判断oppo是不是刘海屏幕
    public static boolean IsNotchInOppo(Context context)
    {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    //判断vivo是不是有刘海
    public static final int VIVO_NOTCH = 0x00000020;//是否有刘海
    public static final int VIVO_FILLET = 0x00000008;//是否有圆角
    public static boolean IsNotchVivo(Context context)
    {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            Log.e("unity", "GetNotchHeight hasNotchAtVivo ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("unity", "GetNotchHeight hasNotchAtVivo NoSuchMethodException");
        } catch (Exception e) {
            Log.e("unity", "GetNotchHeight hasNotchAtVivo Exception");
        } finally {
            return ret;
        }
    }

    //判断小米是否是刘海屏幕

    private static boolean IsNotchXiaomi(Context context) {
        int result = 0;
        try {
            ClassLoader classLoader = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = classLoader.loadClass("android.os.SystemProperties");
            //参数类型
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;
            Method getInt = SystemProperties.getMethod("getInt", paramTypes);
            //参数
            Object[] params = new Object[2];
            params[0] = new String("ro.miui.notch");
            params[1] = new Integer(0);
            result = (Integer) getInt.invoke(SystemProperties, params);
            Log.e("unity", "GetNotchHeight IsNotchXiaomi result : " + result);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return result == 1 ? true : false;
    }

    //android p版本 判断是否是刘海
    //public static DisplayCutout isAndroidP(Activity activity)
    //{
    //    View decorView = activity.getWindow().getDecorView();
    //    if (decorView != null && android.os.Build.VERSION.SDK_INT >= 28) {
    //        WindowInsets windowInsets = decorView.getRootWindowInsets();
    //        if (windowInsets != null)
    //            return windowInsets.getDisplayCutout();
    //    }
    //    return null;
    //}

    //获取华为刘海屏的高度
    public static int GetNotchHeightHuawei(Context context)
    {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("unity", "GetNotchHeight getNotchSize ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("unity", "GetNotchHeight getNotchSize NoSuchMethodException");
        } catch (Exception e) {
            Log.e("unity", "GetNotchHeight getNotchSize Exception");
        } finally {
            return ret[1];
        }
    }

    public static int GetNotchHeightOppo(Context context)
    {
        return 80;
    }

    public static int GetNotchHeightVivo(Context context)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        //dp转px
        return (int) (27 * scale + 0.5f);
    }

    public static int GetNotchHeightXiaomi(Context context)
    {
        int dimensionPixelSize = 0;
        //获取高度（MIUI 10 新增了获取刘海宽和高的方法，需升级至8.6.26开发版及以上版本。）
        int resourceId = context.getResources().getIdentifier("notch_height", "dimen", "android");
        if (resourceId > 0) {
            Log.e("unity", "GetNotchHeight GetNotchHeightXiaomi resourceId > 0");
            dimensionPixelSize = context.getResources().getDimensionPixelSize(resourceId);
        }
        else {
            Log.e("unity", "GetNotchHeight GetNotchHeightXiaomi resourceId <= 0");
            dimensionPixelSize = GetStatusBarHeight(context);
        }
        return dimensionPixelSize;
    }

    //获取状态栏的高度(对于没有提供获取刘海屏高度的手机使用)
    public static int GetStatusBarHeight(Context context)
    {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            Log.e("unity", "GetNotchHeight GetStatusBarHeight value is : " + statusBarHeight);
        }
        return statusBarHeight ;
    }

    //手机适配end
}

