package com.beggar.hotfix.base;


import java.util.Map;
import java.util.WeakHashMap;

/**
 * author: BeggarLan
 * created on: 2022/5/6 23:11
 * description: 模板类
 */
public class PatchTemplate implements ChangeRedirect {

  private static final Map<Object, Object> keyToValueRelation = new WeakHashMap<>();

  @Override
  public boolean isSupport(String patchMethodDesc, Object[] params) {
    return true;
  }

  @Override
  public Object accessDispatch(String patchMethodDesc, Object[] params) {
    return null;
  }

}
