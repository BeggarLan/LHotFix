package com.beggar.hotfix.base;

/**
 * author: BeggarLan
 * created on: 2022/5/6 22:30
 * description: 用于访问修复补丁
 */
public interface ChangeRedirect {

    String sClassName = "com.beggar.hotfix.base.ChangeRedirect";
    String sObjectName = "changeRedirect";


    /**
     * 是否可以patch
     *
     * @param methodName
     * @param params
     */
    boolean isSupport(String methodName, Object[] params);

    /**
     * patch
     *
     * @param methodName
     * @param params
     */
    Object patch(String methodName, Object[] params);

}
