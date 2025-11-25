package com.example.himovieinterceptor;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView textView = findViewById(R.id.textView);
        textView.setText("华为视频接口拦截器\n\n模块已安装，请在LSPosed中启用并重启华为视频应用。\n\n目标接口: /poservice/getUserContracts?");
    }
}
