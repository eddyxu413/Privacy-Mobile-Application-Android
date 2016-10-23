package unimelb.dix1.fuzzylocation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class About extends AppCompatActivity {
    private WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about_layout);

        myWebView = (WebView) findViewById(R.id.about);
        myWebView.setVerticalScrollBarEnabled(false);
        myWebView.loadDataWithBaseURL("", getString(R.string.about),
                "text/html", "utf-8", "");


    }
}
