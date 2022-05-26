package com.beggar.hotfix.patch;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/5/26 11:22 下午
 * description: 回调
 */
public interface LHotfixCallback {

  /**
   * 获取到补丁列表
   *
   * @param result    获取结果
   * @param isNet     是否是网络获取
   * @param patchList 补丁列表
   */
  void onPatchListFetched(boolean result, boolean isNet, @Nullable List<Patch> patchList);

  /**
   * 补丁被应用后
   *
   * @param result 结果
   * @param patch  补丁
   */
  void onPatchApplied(boolean result, @NonNull Patch patch);

}
