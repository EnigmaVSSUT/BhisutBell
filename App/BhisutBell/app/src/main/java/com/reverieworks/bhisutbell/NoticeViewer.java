package com.reverieworks.bhisutbell;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

public class NoticeViewer extends AppCompatActivity {

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_viewer);

        Intent intent = getIntent();
        String url = intent.getStringExtra("EXTRA-urlOfNotice");

        webView = (WebView) findViewById(R.id.webView_listView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
