package com.beggar.hotfix.patch;

import androidx.annotation.NonNull;

/**
 * author: BeggarLan
 * created on: 2022/6/10 10:59 下午
 * description: patch的类信息
 */
public class PatchedClassInfo {

  // 原类
  @NonNull
  private final String mSourceClassName;
  // 补丁类，注意这里其实是patchControl类
  @NonNull
  private final String mPatchClassName;

  public PatchedClassInfo(@NonNull String sourceClassName, @NonNull String patchClassName) {
    mSourceClassName = sourceClassName;
    mPatchClassName = patchClassName;
  }

  @NonNull
  public String getSourceClassName() {
    return mSourceClassName;
  }

  @NonNull
  public String getPatchClassName() {
    return mPatchClassName;
  }

  @Override
  public String toString() {
    return "PatchClassInfo{" +
        "mSourceClassName='" + mSourceClassName + '\'' +
        ", mPatchClassName='" + mPatchClassName + '\'' +
        '}';
  }
}
