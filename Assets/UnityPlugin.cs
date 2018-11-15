using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class UnityPlugin : MonoBehaviour {
    public Button mBtnCamera;
    public Button mBtnGallery;
    public RawImage mImage;
    public Text mText;
    private Action<Texture> mPhotoAction;

    private static UnityPlugin mInstance;
    public static UnityPlugin Instance
    {
        get
        {
            if(mInstance == null)
            {
                GameObject obj = new GameObject();
                obj.name = "UnityPlugin";
                mInstance = obj.AddComponent<UnityPlugin>();
                DontDestroyOnLoad(obj);
            }

            return mInstance;
        }
    }

	// Use this for initialization
	void Start () {
        mBtnCamera.onClick.AddListener(() =>
        {
            TakePhoto((datas)=>
            {

            });
        });

        mBtnGallery.onClick.AddListener(() =>
        {
            OpenGallery((datas) =>
            {

            });
        });
    }

    public void TakePhoto(Action<Texture> callback)
    {
        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
        jo.Call("TakePhoto");
        mPhotoAction = callback;
    }

    public void OpenGallery(Action<Texture> callback)
    {
        AndroidJavaClass jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
        AndroidJavaObject jo = jc.GetStatic<AndroidJavaObject>("currentActivity");
        jo.Call("OpenGallery");
        mPhotoAction = callback;
    }

    void OnGetPhoto(string name)
    {
        StartCoroutine(LoadPhoto(name));
    }

    IEnumerator LoadPhoto(string name)
    {
        mText.text = name;
        string path = "file://" + Application.persistentDataPath + "/" + name;
        WWW www = new WWW(path);
        yield return www;
        mImage.texture = www.texture;
        if (mPhotoAction != null)
        {
            mPhotoAction(mImage.texture);
            mPhotoAction = null;
            //Destroy(texture);
        }
    }
}
