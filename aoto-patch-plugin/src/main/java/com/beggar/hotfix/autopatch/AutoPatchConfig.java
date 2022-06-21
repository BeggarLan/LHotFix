package com.beggar.hotfix.autopatch;

import java.util.ArrayList;
import java.util.List;

import com.android.annotations.NonNull;

/**
 * author: lanweihua
 * created on: 2022/6/15 12:55 下午
 * description: 打补丁包的配置
 */
public class AutoPatchConfig {

  // 新增的class：ADD注解的class
  @NonNull
  public final List<String> mNewClassList = new ArrayList<>();

  // 新增的方法
  @NonNull
  public final List<String> mNewMethodList = new ArrayList<>();

  // 修改的方法
  @NonNull
  public final List<String> mModifyMethodList = new ArrayList<>();


}
