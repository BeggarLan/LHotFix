package com.beggar.lhotfix.demo;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.beggar.lhotfix.PermissionUtil;
import com.beggar.lhotfix.R;

public class
MainActivity extends AppCompatActivity {

  private TextView mPatchButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
  }

  private void initView() {
    mPatchButton = findViewById(R.id.patch_btn);
    mPatchButton.setOnClickListener(view -> {
      // 需要申请读写sd卡权限
      PermissionUtil.requestPermission(
          MainActivity.this,
          isSuccess -> {
            if (isSuccess) {
              // 开始热修复
              startHotfix();
            }
          },
          throwable -> {},
          Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
    });
  }

  private void startHotfix() {

  }

}