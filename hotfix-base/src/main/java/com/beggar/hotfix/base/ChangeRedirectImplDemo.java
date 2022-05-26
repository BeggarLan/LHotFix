package com.beggar.hotfix.base;


/**
 * author: BeggarLan
 * created on: 2022/5/6 23:11
 * description: 模板类
 */
public class ChangeRedirectImplDemo implements ChangeRedirect{
    @Override
    public boolean isSupport(String patchMethodName, Object[] params) {
        return true;
    }

  @Override
  public Object accessDispatch(String patchMethodName, Object[] params) {
    return null;
  }

}
