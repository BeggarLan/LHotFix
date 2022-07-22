package com.beggar.lhotfix;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;

import io.reactivex.functions.Consumer;

/**
 * author: BeggarLan
 * description: 权限工具类
 */
public class PermissionUtil {

  /**
   * 获取权限
   *
   * @param onNext      权限获取结果的回掉
   * @param onError     权限获取错误的回掉
   * @param permissions 要申请的权限
   */
  @SuppressLint("CheckResult")
  public static void requestPermission(
      @NonNull Activity activity,
      Consumer<Boolean> onNext,
      Consumer<? super Throwable> onError,
      final String... permissions) {
//    RxPermissions rxPermissions = new RxPermissions(activity);
//    rxPermissions
//        .request(permissions)
//        .subscribe(onNext, onError);
  }

}
