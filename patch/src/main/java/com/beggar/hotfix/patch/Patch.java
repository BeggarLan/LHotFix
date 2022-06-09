package com.beggar.hotfix.patch;

/**
 * author: BeggarLan
 * created on: 2022/5/26 11:24 下午
 * description: 补丁
 */
public class Patch {

  // 补丁名
  private String mName;

  // 补丁链接
  private String mPatchUrl;

  // 补丁本地路径
  private String mPatchLocalPath;

  // 补丁的md5
  private int mPatchMd5;

  // TODO: 2022/5/26 app hash值,避免应用内升级导致低版本app的补丁应用到了高版本app上
  private int mAppHash;

  // 是否apply成功
  private boolean mIsApplySucceed;

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }

  public String getPatchUrl() {
    return mPatchUrl;
  }

  public void setPatchUrl(String patchUrl) {
    mPatchUrl = patchUrl;
  }

  public String getPatchLocalPath() {
    return mPatchLocalPath;
  }

  public void setPatchLocalPath(String patchLocalPath) {
    mPatchLocalPath = patchLocalPath;
  }

  public int getPatchMd5() {
    return mPatchMd5;
  }

  public void setPatchMd5(int patchMd5) {
    mPatchMd5 = patchMd5;
  }

  public int getAppHash() {
    return mAppHash;
  }

  public void setAppHash(int appHash) {
    mAppHash = appHash;
  }

  public boolean isApplySucceed() {
    return mIsApplySucceed;
  }

  public void setApplySucceed(boolean applySucceed) {
    mIsApplySucceed = applySucceed;
  }
}
