package com.beggar.hotfix.patch;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.beggar.hotfix.base.ChangeRedirect;

import dalvik.system.DexClassLoader;

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

      // 执行布丁
      boolean patchSucceed = patch(patch);
      Log.i(TAG, "patch path: " + patch.getPatchLocalPath() + ", patch result :" + patchSucceed);
      if (patchSucceed) {
        patch.setApplySucceed(true);
        mHotfixCallback.onPatchApplied(true, patch);
      } else {
        mHotfixCallback.onPatchApplied(false, patch);
      }
    }
  }

  /**
   * 执行布丁
   *
   * @return {@code true} 成功
   */
  private boolean patch(@NonNull Patch patch) {
    Log.i(TAG, "start patch");
    if (!mPatchManipulate.verifyPatch(mContext, patch)) {
      Log.i(TAG, "verifyPatch fail :" + patch.getPatchLocalPath());
      return false;
    }

    File patchCacheDir = getPatchCacheDir(patch);
    DexClassLoader classLoader = new DexClassLoader(
        patch.getPatchLocalPath(),
        patchCacheDir.getAbsolutePath(),
        null,
        PatchExecutor.class.getClassLoader());

    // 获取原类和其对应的补丁类
    Log.i(TAG, "start load patchClassInfoProviderClass");
    String patchClassInfoProviderClassFullName = patch.getPatchClassInfoProviderClassFullName();
    PatchClassInfoProvider patchClassInfoProvider = null;
    try {
      Class<?> patchClassInfoProviderClazz =
          classLoader.loadClass(patchClassInfoProviderClassFullName);
      patchClassInfoProvider = (PatchClassInfoProvider) patchClassInfoProviderClazz.newInstance();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    if (patchClassInfoProvider == null) {
      Log.i(TAG, "patchClassInfoProvider is null");
      return false;
    }

    List<PatchClassInfo> patchClassInfoList = patchClassInfoProvider.getPatchClassInfoList();
    if (patchClassInfoList == null || patchClassInfoList.size() == 0) {
      Log.i(TAG, "patchClassInfoList is empty");
      return true;
    }

    // 是否出错(一个错了，这个值就为true)
    boolean isError = false;
    // 每个类打补丁
    for (PatchClassInfo patchClassInfo : patchClassInfoList) {
      Log.i(TAG, "start patch class, patchClassInfo :" + patchClassInfo.toString());
      // 原类
      String sourceClassName = patchClassInfo.getSourceClassName();
      // 补丁类
      String patchClassName = patchClassInfo.getPatchClassName();

      if (TextUtils.isEmpty(patchClassName) || TextUtils.isEmpty(sourceClassName)) {
        Log.i(TAG, "patchClassName or sourceClassName is empty");
        continue;
      }

      Class<?> sourceClass;
      try {
        sourceClass = classLoader.loadClass(sourceClassName);
      } catch (ClassNotFoundException e) {
        Log.i(TAG, "load sourceClass ClassNotFoundException");
        isError = true;
        e.printStackTrace();
        continue;
      }

      Log.i(TAG, "start find changeRedirect");
      // 代理
      Field changeRedirect = null;
      Field[] fields = sourceClass.getDeclaredFields();
      for (Field field : fields) {
        if (!TextUtils.equals(
            field.getType().getCanonicalName(), ChangeRedirect.class.getCanonicalName())) {
          continue;
        }
        // 要求field所在的声明类是sourceClass: 避免父类的changeRedirect被替换
        if (!TextUtils.equals(
            field.getDeclaringClass().getCanonicalName(), sourceClass.getCanonicalName())) {
          continue;
        }
        changeRedirect = field;
        Log.i(TAG, "find changeRedirect success");
        break;
      }

      if (changeRedirect == null) {
        Log.i(TAG, "can not find changeRedirect");
        continue;
      }

      Log.i(TAG, "start assign changeRedirect");
      try {
        Class<?> patchClass = classLoader.loadClass(patchClassName);
        // TODO: 2022/6/14 无参构造函数
        Object patchObject = patchClass.newInstance();
        changeRedirect.setAccessible(true);
        changeRedirect.set(null, patchObject);
        Log.i(TAG, "assign changeRedirect success");
      } catch (Throwable e) {
        Log.i(TAG, "assign changeRedirect fail: " + e.getMessage());
        isError = true;
        e.printStackTrace();
      }
    }
    return !isError;
  }

  /**
   * 打布丁时的cache文件夹
   */
  private File getPatchCacheDir(@NonNull Patch patch) {
    String key = patch.getName() + patch.getPatchMd5();
    File dir = mContext.getDir("patch_cache" + key, Context.MODE_PRIVATE);
    if (!dir.exists()) {
      dir.mkdir();
    }
    return dir;
  }

}
