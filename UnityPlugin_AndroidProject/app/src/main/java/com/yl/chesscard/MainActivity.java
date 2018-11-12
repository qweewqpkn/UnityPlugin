package com.yl.chesscard;
import android.app.Activity;
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

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class MainActivity extends UnityPlayerActivity {

    private static final int TAKE_PHOTO = 1;
    private static final int OPEN_GALLERY = 2;
    private static final int CROP_PHOTO = 3;
    private Uri mPhotoUri;
    private Uri mCropPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void TakePhoto(){
        mPhotoUri = GetUri(CreateFile("temp.png"));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        startActivityForResult(intent, TAKE_PHOTO);

    }

    //调用相册
    public void OpenGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent, OPEN_GALLERY);
    }

    private Uri GetUri(File file)
    {
        Uri uri;
        if(Build.VERSION.SDK_INT >= 24)
        {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        }
        else
        {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

    private File CreateFile(String name)
    {
        File file = new File(Environment.getExternalStorageDirectory(), name);
        try
        {
            if(file.exists())
            {
                file.delete();
            }
            file.createNewFile();
        }catch(IOException e)
        {
            e.printStackTrace();
        }

        return file;
    }

    private void StartCrop(Uri inputUri)
    {
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == Activity.RESULT_CANCELED)
        {
            Log.d("unity","user cancel operator!!");
            return;
        }

        switch (requestCode)
        {
            case TAKE_PHOTO:
            {
                StartCrop(mPhotoUri);
            }
            break;
            case OPEN_GALLERY:
            {
                Uri uri = data.getData();
                StartCrop(uri);
            }
            break;
            case CROP_PHOTO:
            {
                try
                {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mCropPhotoUri));
                    FileOutputStream fOut = null;

                    try
                    {
                        String path = "/mnt/sdcard/Android/data/com.niko.myunityplugin/files";
                        File destDir = new File(path);
                        if(!destDir.exists())
                        {
                            destDir.mkdirs();
                        }

                        fOut = new FileOutputStream(path + "/" + "image.png");
                    }
                    catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    }

                    if(bitmap != null)
                    {
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

                        UnityPlayer.UnitySendMessage("UnityPlugin","OnGetPhoto", "image.png");
                    }
                }
                catch(FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
            break;
        }
    }
}
