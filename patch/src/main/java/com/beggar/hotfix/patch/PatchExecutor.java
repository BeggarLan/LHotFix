package com.beggar.hotfix.patch;

import java.util.List;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: lanweihua
 * created on: 2022/6/9 9:42 下午
 * description: 布丁执行
 */
public class PatchExecutor extends Thread {

  private static final String TAG = "PatchExecutor";

  @NonNull
  private final Context mContext;
  @NonNull
  private final PatchManipulate mPatchManipulate;
  @NonNull
  private final HotfixCallback mHotfixCallback;

  public PatchExecutor(
      @NonNull Context context,
      @NonNull PatchManipulate patchManipulate,
      @NonNull HotfixCallback hotfixCallback) {
    mContext = context;
    mPatchManipulate = patchManipulate;
    mHotfixCallback = hotfixCallback;
  }


  @Override
  public void run() {
    List<Patch> patches = mPatchManipulate.fetchPatchList(mContext);
    applyPatches(patches);
  }

  private void applyPatches(@Nullable List<Patch> patches) {
    if (patches == null || patches.size() == 0) {
      Log.i(TAG, "patch list is empty");
      return;
    }
    Log.i(TAG, "patch list size is : " + patches.size());
    for (Patch patch : patches) {
      // 已经apply成功了
      if (patch.isApplySucceed()) {
        Log.i(TAG, "patch isApplySucceed : " + patch.getPatchLocalPath());
        continue;
      }
      if (!mPatchManipulate.ensurePatchExist(patch)) {
        Log.i(TAG, "patch is not exist : " + patch.getPatchLocalPath());
        return;
      }
      boolean patchSucceed = patch(patch);

    }

  }

  /**
   * 执行布丁
   *
   * @return {@code true} 成功
   */
  private boolean patch(@NonNull Patch patch) {
    return false;
  }

}
