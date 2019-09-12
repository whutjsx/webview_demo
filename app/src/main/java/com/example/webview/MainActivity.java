package com.example.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView mTextViewDetail;
    private TextView mTextViewFalse;
    private ProgressBar mProgressBarWeb;

    private Button mButtonDetail;
    private Button mButtonUrl;

    //网页的加载需要ConnectivityManager
    private ConnectivityManager mManager;
    private WebView mWebView;
    private WebSettings mWs;

    public static final  int GET_URL_MESSAGE = 0x99;
    //handler用于接收URLConnection传递的消息
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_URL_MESSAGE:
                    mTextViewDetail.setText(msg.obj.toString());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewDetail = (TextView) findViewById(R.id.text_detail);
        mTextViewFalse = (TextView) findViewById(R.id.text_web_false);

        mProgressBarWeb = (ProgressBar) findViewById(R.id.progressBar_web);

        mButtonDetail = (Button) findViewById(R.id.button_detail);
        mManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mButtonDetail.setOnClickListener(this);

        mButtonUrl = (Button) findViewById(R.id.button_url);
        mButtonUrl.setOnClickListener(this);
        //WebView用来显示网页
        mWebView = (WebView) findViewById(R.id.webView_test);

        //mWs的作用是加载一个Zoom,功能是放大或缩小网页
        mWs = mWebView.getSettings();
        mWs.setSupportZoom(true);
        mWs.setBuiltInZoomControls(true);
        //监听网页加载前后和网页加载错误的事件
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBarWeb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBarWeb.setVisibility(View.INVISIBLE);
            }



        });
        //网页加载中的监听需要调用此方法
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBarWeb.setProgress(newProgress);
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                MainActivity.this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_detail:
                //loadUrl中传入网页地址
                mWebView.loadUrl("https://www.google.com/");
                //NetWorkInfo用来得到网页的一些信息
                NetworkInfo info = mManager.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    mTextViewDetail.setText("网络类型：" + info.getTypeName());
                } else {
                    mTextViewDetail.setText("无网络连接");
                }
                break;
            case R.id.button_url:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        urlConnectServerLet();
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    /**
     * URLConnection加载网页的方法
     */
    private void urlConnectServerLet() {
        try {
            URL url = new URL("https://www.google.com");
            //需要一个URLConnection打开网页
            URLConnection connection = url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            StringBuffer buffer = new StringBuffer();
            while (line != null) {
                buffer.append(line);
                line = br.readLine();
            }
            is.close();
            Message message = mHandler.obtainMessage();
            message.what = GET_URL_MESSAGE;
            message.obj = buffer.toString().trim();
            mHandler.sendMessage(message);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
