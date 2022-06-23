package com.beggar.hotfix.autopatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

  // 新增的方法,String是CtMethod.longName
  @NonNull
  public final List<String> mNewMethodList = new ArrayList<>();

  // 修改的方法，String是CtMethod.longName
  @NonNull
  public final List<String> mModifyMethodList = new ArrayList<>();


  // 经过插桩的方法，key: CtMethod.longName, value: number
  @NonNull
  public final Map<String, Integer> mCodeInsertMethodMap = new LinkedHashMap<>();

  // TODO: 2022/6/23 支持混淆
  // 是否支持混淆
  public boolean mSupportProGuard = true;
  // mapping文件路径
  public String mMappingFilePath;

}
