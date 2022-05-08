package com.beggar.hotfix.gradle.plugin.codeinsert;

import com.android.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    protected AtomicInteger insertMethodCount = new AtomicInteger(0);

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
    protected abstract void insertCode(@NonNull List<CtClass> ctClasses, @NonNull File jarFile);

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
     * 打成zip
     *
     * @param classBytes      文件内容
     * @param zipOutputStream
     * @param zipName         zip名
     */
    protected void zipFile(@NonNull byte[] classBytes, @NonNull ZipOutputStream zipOutputStream, @NonNull String zipName) {
        try {
            ZipEntry zipEntry = new ZipEntry(zipName);
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
