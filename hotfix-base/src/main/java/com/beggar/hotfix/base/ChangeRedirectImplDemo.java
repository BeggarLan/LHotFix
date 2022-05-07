package com.beggar.hotfix.base;

import androidx.annotation.NonNull;

/**
 * author: BeggarLan
 * created on: 2022/5/6 23:11
 * description: 模板类
 */
public class ChangeRedirectImplDemo implements ChangeRedirect{
    @Override
    public boolean isSupport(@NonNull String methodName, Object[] params) {
        return true;
    }

    @Override
    public Object patch(@NonNull String methodName, Object[] params) {
        return null;
    }
}
