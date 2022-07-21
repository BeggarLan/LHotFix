package com.beggar.hotfix.patch;

import androidx.annotation.NonNull;

/**
 * author: BeggarLan
 * created on: 2022/6/10 10:59 下午
 * description: 被修补的类和补丁类信息
 */
public class PatchClassInfo {

  // 原类
  @NonNull
  private final String mSourceClassName;
  // 补丁类
  @NonNull
  private final String mPatchClassName;

  public PatchClassInfo(@NonNull String sourceClassName, @NonNull String patchClassName) {
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
