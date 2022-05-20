package com.beggar.hotfix.base;


/**
 * author: BeggarLan
 * created on: 2022/5/6 23:11
 * description: 模板类
 */
public class ChangeRedirectImplDemo implements ChangeRedirect{
    @Override
    public boolean isSupport(String methodName, Object[] params) {
        return true;
    }

    @Override
    public Object patch(String methodName, Object[] params) {
        return null;
    }
}
