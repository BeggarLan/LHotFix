package com.beggar.hotfix.patch;

import java.util.List;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author: BeggarLan
 * created on: 2022/6/9 9:34 下午
 * description: 补丁操作
 */
public interface PatchManipulate {

  /**
   * 获取补丁列表
   */
  @Nullable
  List<Patch> fetchPatchList(@NonNull Context context);

  /**
   * 验证补丁md5
   * 验证不通过会触发下载
   */
  boolean verifyPatch(@NonNull Context context, @NonNull Patch patch);

}
