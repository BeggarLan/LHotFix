package com.beggar.hotfix.autopatch;

/**
 * author: lanweihua
 * created on: 2022/6/15 11:35 下午
 * description: 常量
 */
public class AutoPatchConstants {

  // patch包生成文件夹
  public static final String PATCH_GENERATE_DIR = "output/hotfix";

  // 补丁类的构造器名
  public static final String PATCH_CLASS_CONSTRUCTOR_NAME = "Patch";
  // 补丁类的<SourceClass mSourceClass>属性
  public static final String PATCH_CLASS_FIELD_SOURCE_CLASS = "mSourceClass";

}
