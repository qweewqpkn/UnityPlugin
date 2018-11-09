package com.niko.myunityplugin;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.KeyEvent;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends UnityPlayerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    public void TakePhoto(){
        //File file = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
        //Uri photoUri = FileProvider.getUriForFile(this, "com.niko.myunityplugin.fileprovider", file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, 1);

    }

    //调用相册
    public void OpenGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK,null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(intent, 2);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
        {
            //当用户点击返回键是 通知Unity开始在"/mnt/sdcard/Android/data/com.xys/files";路径中读取图片资源，并且现在在Unity中
            //UnityPlayer.UnitySendMessage("Main Camera","message", "image.png");

        }
        return super.onKeyDown(keyCode, event);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1)
        {
            Bundle extras = data.getExtras();
            if(extras != null)
            {
                Bitmap bitmap = extras.getParcelable("data");
                FileOutputStream fOut = null;
                String path = "/mnt/sdcard/Android/data/com.niko.myunityplugin/files";

                try {
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

                UnityPlayer.UnitySendMessage("Main Camera","message", "image.png");
            }
        }
        else if(requestCode == 3)
        {
            File file = new File(Environment.getExternalStorageDirectory(), "temp.jpg");
            Uri photoUri = FileProvider.getUriForFile(this, "com.niko.myunityplugin.fileprovider", file);

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(photoUri, "image/*");
            intent.putExtra("crop", "true");
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, 1);
        }
    }

}
