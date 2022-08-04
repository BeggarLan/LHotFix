package com.beggar.hotfix.gradle.plugin.codeinsert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.android.annotations.NonNull;

import javassist.CannotCompileException;
import javassist.CtClass;

/**
 * author: BeggarLan
 * created on: 2022/5/8 21:00
 * description: 代码插入
 */
public abstract class CodeInsertStrategy {

  @NonNull
  protected List<String> mHotfixPackageList = new ArrayList<>();
  @NonNull
  protected List<String> mHotfixMethodList = new ArrayList<>();
  @NonNull
  protected List<String> mExceptPackageList = new ArrayList<>();
  @NonNull
  protected List<String> mExceptMethodList = new ArrayList<>();

  protected AtomicInteger mInsertMethodCount = new AtomicInteger(0);

  // 代码插入的方法，key:CtBehavior.getLongName() , value: 方法number
  public Map<String, Integer> mMethodMap = new LinkedHashMap<>();

  public CodeInsertStrategy(
      @NonNull List<String> hotfixPackageList,
      @NonNull List<String> hotfixMethodList,
      @NonNull List<String> exceptPackageList,
      @NonNull List<String> exceptMethodList) {
    this.mHotfixPackageList = hotfixPackageList;
    this.mHotfixMethodList = hotfixMethodList;
    this.mExceptPackageList = exceptPackageList;
    this.mExceptMethodList = exceptMethodList;
  }

  /**
   * 具体的代码插入，子类实现
   *
   * @param ctClasses 所有需要打入apk中的类
   * @param jarFile   所有插桩处理过的class都会被输出到jarFile
   */
  public abstract void insertCode(@NonNull List<CtClass> ctClasses, @NonNull File jarFile)
      throws IOException, CannotCompileException;

  /**
   * 该类是否需要插入代码
   *
   * @param className 类
   * @return {@code true} 该类需要插入代码
   */
  protected boolean isNeedInsert(@NonNull String className) {
    for (String exceptPackageName : mExceptPackageList) {
      if (className.startsWith(exceptPackageName)) {
        return false;
      }
    }
    for (String hotfixPackageName : mHotfixPackageList) {
      if (className.startsWith(hotfixPackageName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 将文件打入zip
   *
   * @param classBytes 文件内容
   * @param entryName  zip名
   */
  protected void zipFile(
      @NonNull byte[] classBytes,
      @NonNull ZipOutputStream zipOutputStream,
      @NonNull String entryName) {
    try {
      ZipEntry zipEntry = new ZipEntry(entryName);
      zipOutputStream.putNextEntry(zipEntry);
      zipOutputStream.write(classBytes, 0, classBytes.length);
      zipOutputStream.closeEntry();
      zipOutputStream.flush();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    } finally {
      try {
        zipOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
