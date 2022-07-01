package com.beggar.hotfix.autopatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.android.annotations.NonNull;

import javassist.CtMethod;

/**
 * author: lanweihua
 * created on: 2022/6/15 12:55 下午
 * description: 打补丁包的配置
 */
public class AutoPatchConfig {

  // TODO: 2022/6/27 新增clss还未处理
  // ADD注解的新增class
  @NonNull
  public final List<String> mNewClassList = new ArrayList<>();

  // 新增的方法,String是CtMethod.longName
  @NonNull
  public final List<String> mNewMethodList = new ArrayList<>();

  // 修改的方法，String是CtMethod.longName
  @NonNull
  public final List<String> mModifyMethodSignatureList = new ArrayList<>();
  // 类中含有modify注解的方法时，CtClass.getName()
  @NonNull
  public final List<String> mModifyClassList = new ArrayList<>();

  // 调用的super方法，key：CtClass.getName()  ----  value：<T>->MethodCall.method
  @NonNull
  public final Map<String, List<CtMethod>> mInvokeSuperMethodMap = new HashMap<>();

  // 经过插桩的方法，key: CtMethod.longName, value: number
  @NonNull
  public final Map<String, Integer> mCodeInsertMethodMap = new LinkedHashMap<>();

  // TODO: 2022/6/23 支持混淆
  // 是否支持混淆
  public boolean mSupportProGuard = true;
  // mapping文件路径
  public String mMappingFilePath;

}
