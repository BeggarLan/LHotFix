package com.beggar.hotfix.base;

import androidx.annotation.NonNull;

/**
 * author: BeggarLan
 * created on: 2022/5/6 22:30
 * description: 用于访问修复补丁
 */
public interface ChangeRedirect {

    /**
     * 是否可以patch
     *
     * @param methodName
     * @param params
     */
    boolean isSupport(@NonNull String methodName, Object[] params);

    /**
     * patch
     *
     * @param methodName
     * @param params
     */
    Object patch(@NonNull String methodName, Object[] params);

}
