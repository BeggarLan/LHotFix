package com.beggar.hotfix.autopatch;

/**
 * author: lanweihua
 * created on: 2022/6/27 10:44 下午
 * description: 构造补丁
 */
public class PatchFactory {

  private static PatchFactory sInstance = new PatchFactory();

  public static PatchFactory getInstance() {
    return sInstance;
  }

}
