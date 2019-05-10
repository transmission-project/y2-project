package com.example.huntertalk;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebTestActivity extends AppCompatActivity {

    private static final int MICROPHONE_REQUEST = 1888;
    private WebView voiceWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_test);

        startVoiceWebView();
    }

    private void startVoiceWebView() {

        //Check for record audio permission and ask for it
        if(ContextCompat.checkSelfPermission(WebTestActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(WebTestActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_REQUEST);
        }

        //Check for record audio permission and ask for it
        if(ContextCompat.checkSelfPermission(WebTestActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(WebTestActivity.this,
                    new String[]{Manifest.permission.MODIFY_AUDIO_SETTINGS}, MICROPHONE_REQUEST);
        }

        voiceWebView = findViewById(R.id.voice_webview);

        WebSettings settings = voiceWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        WebView.setWebContentsDebuggingEnabled(true); //TODO: disable this for production

        voiceWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onConsoleMessage(ConsoleMessage m) {
                Log.d("getUserMedia, WebView", m.message() + " -- From line "
                        + m.lineNumber() + " of "
                        + m.sourceId());

                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                WebTestActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }

        });

        voiceWebView.loadUrl("file:///android_asset/index.html");
        // TODO: JS interface for choosing group to connect to, push to talk, stream activity
    }

    @Override //Callback function after permission has been requested
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MICROPHONE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startVoiceWebView();

                } else {
                    // microphone permission denied...
                    this.finish();
                }
            }
        }
    }
}
