package com.beggar.hotfix.base;

/**
 * author: BeggarLan
 * created on: 2022/5/7 18:56
 * description: 一些常量
 */
public class Constants {

    // 热修code的声明文件
    public static final String ROBUST_XML = "lHotFix/xml";

    // 代码插入的方法汇总文件
    public static final String METHOD_MAP_OUT_PATH = "/outputs/hotfix/methodsMap.hotfix";

    // 插入的对象信息
    public static final String HOTFIX_INTERFACE_NAME = ChangeRedirect.sClassName;
    public static final String HOTFIX_INSERT_FIELD_NAME = ChangeRedirect.sObjectName;
}
