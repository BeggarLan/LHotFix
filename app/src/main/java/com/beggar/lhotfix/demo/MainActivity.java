package com.beggar.lhotfix.demo;

import java.util.List;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.beggar.hotfix.patch.HotfixCallback;
import com.beggar.hotfix.patch.Patch;
import com.beggar.hotfix.patch.PatchExecutor;
import com.beggar.lhotfix.PermissionUtil;
import com.beggar.lhotfix.R;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private TextView mPatchButton;

  private final HotfixCallback mHotfixCallback = new HotfixCallback() {
    @Override
    public void onPatchListFetched(boolean result, boolean isNet, @Nullable List<Patch> patchList) {
      Log.i(TAG, "onPatchListFetched, result:" + result + ", isNet:" + isNet);
    }

    @Override
    public void onPatchApplied(boolean result, @NonNull Patch patch) {
      Log.i(TAG, "onPatchApplied, result:" + result);
    }
  };

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
    new PatchExecutor(
        getApplicationContext(), new PatchManipulateImpl(), mHotfixCallback)
        .run();
  }

}