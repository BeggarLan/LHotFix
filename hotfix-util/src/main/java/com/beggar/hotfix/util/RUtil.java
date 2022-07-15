package com.beggar.hotfix.util;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

/**
 * author: lanweihua
 * created on: 2022/7/14 7:10 下午
 * description: R相关工具
 */
public class RUtil {

  // R文件的名称枚举
  private static final Set<String> RFileNameSet = new HashSet<>();

  static {
    RFileNameSet.add("R$array");
    RFileNameSet.add("R$xml");
    RFileNameSet.add("R$styleable");
    RFileNameSet.add("R$style");
    RFileNameSet.add("R$string");
    RFileNameSet.add("R$raw");
    RFileNameSet.add("R$menu");
    RFileNameSet.add("R$layout");
    RFileNameSet.add("R$integer");
    RFileNameSet.add("R$id");
    RFileNameSet.add("R$drawable");
    RFileNameSet.add("R$dimen");
    RFileNameSet.add("R$color");
    RFileNameSet.add("R$bool");
    RFileNameSet.add("R$attr");
    RFileNameSet.add("R$anim");
  }

  /**
   * 是否是R
   * todo 这里到时候test的时候补充完整描述
   */
  public static boolean isRFile(@NonNull String name) {
    int index = name.indexOf("R");
    if (index < 0) {
      return false;
    }
    return RFileNameSet.contains(name.substring(index));
  }

}
