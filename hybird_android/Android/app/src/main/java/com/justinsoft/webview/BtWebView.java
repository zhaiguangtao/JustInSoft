package com.justinsoft.webview;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.justinsoft.util.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * 复写webview，添加进度条显示效果
 */
public class BtWebView extends WebView
{
    // 日志标记
    private static final String TAG = LogUtil.getClassTag(BtWebView.class);
    
    private ProgressBar progressBar;
    
    // private Activity activity;
    
    private WebChromeClient webChromeClient;
    
    private JsCallback jsCallback;
    
    public BtWebView(Context context)
    {
        super(context);
    }
    
    public BtWebView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initProgressBar(context);
        
        this.webChromeClient = new WebChromeClient();
        setWebChromeClient(this.webChromeClient);
        setWebViewClient(new WebViewClient());
        initSettings();
    }
    
    public BtWebView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
    
    public BtWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    /**
     * 绑定activity
     * 
     * @param activity
     */
    public void bind(Activity activity)
    {
        // this.activity = activity;
        this.webChromeClient.bind(activity);
        
        // 添加给js回调的接口
        jsCallback = new JsCallback(activity, this);
        addJavascriptInterface(jsCallback, "jsCallback");
    }
    
    public void cropCallback(int resultCode)
    {
        this.webChromeClient.cropCallback(resultCode, null);
    }
    
    /**
     * 裁剪照片
     * 
     * @param resultCode
     */
    public void cropPicture(int resultCode)
    {
        this.webChromeClient.cropPicture(resultCode);
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        super.onScrollChanged(l, t, oldl, oldt);
    }
    
    private void initProgressBar(Context context)
    {
        this.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        this.progressBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(context, 3), 0, 0));
        ClipDrawable drawable = new ClipDrawable(new ColorDrawable(Color.BLUE), Gravity.LEFT, ClipDrawable.HORIZONTAL);
        this.progressBar.setProgressDrawable(drawable);
        addView(this.progressBar);
    }
    
    /**
     * 方法描述：根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    private int dp2px(Context context, float dpValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5f);
    }
    
    private void initSettings()
    {
        WebSettings webSettings = getSettings();
        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(true);
        // 设置缓存模式,一共有四种模式
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 设置缓存路径
        // webSettings.setAppCachePath("");
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true);
        // 将图片调整到合适的大小
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // 设置webview保存表单数据
        webSettings.setSaveFormData(true);
        webSettings.setSupportMultipleWindows(true);
        // 支持内容重新布局,一共有四种方式
        // 默认的是NARROW_COLUMNS
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置可以被显示的屏幕控制
        webSettings.setDisplayZoomControls(false);
        // 设置默认字体大小
        webSettings.setDefaultFontSize(12);
        // 设置默认编码
        webSettings.setDefaultTextEncodingName("UTF-8");
        // 启用地理定位
        webSettings.setGeolocationEnabled(true);
        // ***最重要的方法，一定要设置，这就是出不来的主要原因
        webSettings.setDomStorageEnabled(true);
        // 是否可访问Content Provider的资源，默认值 true
        webSettings.setAllowContentAccess(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        // 支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        
        // 自定义user agent
        webSettings.setUserAgentString("android");
        
        // 设置允许跨域访问
        allowAcrossRequest(webSettings);
        
        setLongClickable(true);
        setScrollbarFadingEnabled(true);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setDrawingCacheEnabled(true);
    }
    
    /**
     * 重载WebChromeClient，添加进度条
     */
    private class WebChromeClient extends BtWebChromeClient
    {
        @Override
        public void onProgressChanged(android.webkit.WebView view, int newProgress)
        {
            if (newProgress == 100)
            {
                progressBar.setVisibility(GONE);
            }
            else
            {
                if (progressBar.getVisibility() == GONE)
                {
                    progressBar.setVisibility(VISIBLE);
                }
                progressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }
    
    private class WebViewClient extends android.webkit.WebViewClient
    {
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            Log.i(TAG, "start to override url loading 0");
            view.loadUrl(url);
            return true;
        }
        
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse)
        {
            super.onReceivedHttpError(view, request, errorResponse);
            Log.e(TAG, "failed to receive msg");
        }
        
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error)
        {
            // 注意：super句话一定要删除，或者注释掉，否则又走handler.cancel() 默认的不支持https的了。
            // super.onReceivedSslError(view, handler, error);
            Log.e(TAG, "failed to receive ssl msg");
            // 接受所有网站的证书
            handler.proceed();
        }
    }
    
    private void allowAcrossRequest(WebSettings webSettings)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        else
        {
            try
            {
                Class<?> clazz = webSettings.getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null)
                {
                    method.invoke(webSettings, true);
                }
            }
            catch (NoSuchMethodException e)
            {
                Log.e(TAG, "No method error:", e);
            }
            catch (InvocationTargetException e)
            {
                Log.e(TAG, "InvocationTargetException:", e);
            }
            catch (IllegalAccessException e)
            {
                Log.e(TAG, "IllegalAccessException:", e);
            }
        }
    }
}
