package com.beggar.hotfix.autopatch;

import java.io.File;

import com.beggar.hotfix.base.Constants;

/**
 * author: BeggarLan
 * created on: 2022/6/15 11:35 下午
 * description: 常量
 */
public class AutoPatchConstants {

  // patch包生成文件夹
  public static final String PATCH_GENERATE_DIR = "output/hotfix";
  // patch包打成的jar包路径
  public static final String HOTFIX_JAR_FILE_PATH = PATCH_GENERATE_DIR + "/hotfix.jar";

  // patch包
  public static final String PATCH_DEX_NAME = "patch.dex";
  public static final String PATCH_JAR_NAME = "patch.jar";

  // 补丁类的构造器名
  public static final String PATCH_CLASS_CONSTRUCTOR_NAME = "Patch";
  // 补丁类的<SourceClass mSourceClassInstance>属性
  public static final String PATCH_CLASS_FIELD_SOURCE_CLASS_INSTANCE = "mSourceClassInstance";

  // Assist后缀
  public static final String ASSIST_SUFFIX = "HotfixAssist";

  // Public后缀, 访问private方法的时候会生成一个public的方法去访问
  public static final String PUBLIC_SUFFIX = "HotfixPublic";


  // patch类名的后缀
  public static final String PATCH_CLASS_NAME_SUFFIX = "Patch";
  // patch控制类名的后缀
  public static final String PATCH_CONTROL_CLASS_NAME_SUFFIX = "PatchControl";

  // 方法名
  public static final String GET_REAL_PARAMETER = "getRealParameter";

  // jar工具文件存放的位置
  // 放在项目的文件下下${mProject.projectDir.path}/Constants.HOTFIX_DIR/xx
  public static final String DX_TOOL_FILE_PATH =
      Constants.HOTFIX_DIR + File.separator + "dx.jar";
  public static final String BAK_SMALI_TOOL_FILE_PATH =
      Constants.HOTFIX_DIR + File.separator + "baksmali-2.1.3.jar";
  public static final String SMALI_TOOL_FILE_PATH =
      Constants.HOTFIX_DIR + File.separator + "smali-2.1.3.jar";
}
