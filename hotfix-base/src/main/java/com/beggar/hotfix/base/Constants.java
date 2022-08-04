package com.beggar.hotfix.base;

/**
 * author: BeggarLan
 * created on: 2022/5/7 18:56
 * description: 一些常量
 */
public class Constants {

  // 热修配置文件
  public static final String ROBUST_XML = "LHotfix.xml";

  // 存放热修相关的文件夹
  public static final String HOTFIX_DIR = "hotfix";
  // 代码插入的方法汇总文件
  // 放在项目的文件下下${mProject.projectDir.path}/
  public static final String METHOD_MAP_OUT_PATH = HOTFIX_DIR + "/methodsMap.hotfix";

  // patch类所在的package
  public static final String PATCH_CLASS_PACKAGE_NAME = "com.beggar.hotfix.patch";

  // PatchedClassInfoProvider的实现类全名
  public static final String PATCHED_CLASS_INFO_PROVIDER_IMPL_FULL_NAME =
      PATCH_CLASS_PACKAGE_NAME + "PatchedClassInfoProviderImpl";

  // 两个注解的类
  public static final String ADD_CLASS_NAME = "com.beggar.hotfix.base.annotation.Add";
  public static final String MODIFY_CLASS_NAME = "com.beggar.hotfix.base.annotation.Modify";


  // 插入的对象信息
  public static final String HOTFIX_INTERFACE_NAME = ChangeRedirect.sClassName;
  public static final String HOTFIX_INSERT_FIELD_NAME = ChangeRedirect.sObjectName;

  // ChangeRedirect的两个方法的第一个参数：方法描述的内容分割符
  public static final String PROXY_METHOD_DESC_CONTENT_SPLIT = ":";

  public static final String LANG_VOID = "java.lang.Void";
  public static final String VOID = "void";
  public static final String LANG_BOOLEAN = "java.lang.Boolean";
  public static final String BOOLEAN = "boolean";
  public static final String LANG_INT = "java.lang.Integer";
  public static final String INT = "int";
  public static final String LANG_LONG = "java.lang.Long";
  public static final String LONG = "long";
  public static final String LANG_DOUBLE = "java.lang.Double";
  public static final String DOUBLE = "double";
  public static final String LANG_FLOAT = "java.lang.Float";
  public static final String FLOAT = "float";
  public static final String LANG_SHORT = "java.lang.Short";
  public static final String SHORT = "short";
  public static final String LANG_BYTE = "java.lang.Byte";
  public static final String BYTE = "byte";
  public static final String LANG_CHARACTER = "Character";
  public static final String CHAR = "char";
}
