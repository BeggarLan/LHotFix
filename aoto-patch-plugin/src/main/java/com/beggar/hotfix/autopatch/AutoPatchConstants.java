package com.beggar.hotfix.autopatch;

/**
 * author: BeggarLan
 * created on: 2022/6/15 11:35 下午
 * description: 常量
 */
public class AutoPatchConstants {

  // patch包生成文件夹
  public static final String PATCH_GENERATE_DIR = "output/hotfix";

  // 补丁类的构造器名
  public static final String PATCH_CLASS_CONSTRUCTOR_NAME = "Patch";
  // 补丁类的<SourceClass mSourceClassInstance>属性
  public static final String PATCH_CLASS_FIELD_SOURCE_CLASS_INSTANCE = "mSourceClassInstance";

  // Assist后缀
  public static final String ASSIST_SUFFIX = "HotfixAssist";

  // Public后缀, 访问private方法的时候会生成一个public的方法去访问
  public static final String PUBLIC_SUFFIX = "HotfixPublic";

  // patch类所在的package
  public static final String PATCH_CLASS_PACKAGE_NAME = "com.beggar.hotfix.patch";
  // patch类名的后缀
  public static final String PATCH_CLASS_NAME_SUFFIX = "Patch";
  // patch控制类名的后缀
  public static final String PATCH_CONTROL_CLASS_NAME_SUFFIX = "PatchControl";

  // 方法名
  public static final String GET_REAL_PARAMETER = "getRealParameter";
}
