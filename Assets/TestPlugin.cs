using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class TestPlugin : MonoBehaviour {

    public Button mBtnCamera;
    public Button mBtnGallery;
    public Text mText;
    public RawImage mImage;

	// Use this for initialization
	void Start () {
        mBtnCamera.onClick.AddListener(() =>
        {
            AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
            jo.Call("TakePhoto");
        });

        mBtnGallery.onClick.AddListener(() =>
        {
            AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
            jo.Call("OpenGallery");
        });
    }

    public void TakePhoto(Action<byte[]> callback)
    {
        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
        jo.Call("TakePhoto");
    }

    void message(string str)
    {
        //在Android插件中通知Unity开始去指定路径中找图片资源
        StartCoroutine(LoadTexture(str));
        
    }

    IEnumerator LoadTexture(string name)
    {
        mText.text = "1231231233！！！";
        //注解1
        string path = "file://" + Application.persistentDataPath + "/" + name;

        WWW www = new WWW(path);
        while (!www.isDone)
        {

        }
        yield return www;
        //为贴图赋值
        mImage.texture = www.texture;
    }
}
