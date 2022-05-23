package com.beggar.hotfix.gradle.plugin.codeinsert;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * author: BeggarLan
 * created on: 2022/5/8 18:07
 * description: Javassist工具类
 */
public class JavassistUtil {

    /**
     * 把inputs中的class转换成 CtClass
     *
     * @param inputs
     * @param classPool
     */
    public static List<CtClass> toCtClasses(@NonNull Collection<TransformInput> inputs, @NonNull ClassPool classPool) {
        List<String> classNames = new ArrayList<>();
        List<CtClass> ctClasses = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        for (TransformInput input : inputs) {
            // 源码
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dirFile = directoryInput.getFile();
                String dirPath = dirFile.getAbsolutePath();
                //将当前路径加入类池,不然找不到这个类
                try {
                    classPool.insertClassPath(dirPath);
                    // 所有的子文件(递归)
                    Collection<File> childFiles = FileUtils.listFiles(dirFile, null, true);
                    for (File childFile : childFiles) {
                        String childFileAbsolutePath = childFile.getAbsolutePath();
                        // .class结尾
                        if (childFileAbsolutePath.endsWith(SdkConstants.DOT_CLASS)) {
                            String className = childFileAbsolutePath
                                    .substring(dirPath.length() + 1, childFileAbsolutePath.length() - SdkConstants.DOT_CLASS.length())
                                    .replaceAll(Matcher.quoteReplacement(File.separator), ".");
                            if (classNames.contains(className)) {
                                throw new RuntimeException("with the same name : " + className + " please remove duplicate classes ");
                            }
                            classNames.add(className);
                        }
                    }
                } catch (NotFoundException e) {
                    e.printStackTrace();
                    System.out.println("class dir not found");
                }

            }

            //jar(aar等)
            for (JarInput jarInput : input.getJarInputs()) {
                try {
                    classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                    JarFile jarFile = new JarFile(jarInput.getFile());
                    Enumeration<JarEntry> classes = jarFile.entries();

                    while (classes.hasMoreElements()) {
                        JarEntry libClass = classes.nextElement();
                        String className = libClass.getName();
                        if (className.endsWith(SdkConstants.DOT_CLASS)) {
                            className = className
                                    .substring(0, className.length() - SdkConstants.DOT_CLASS.length())
                                    .replaceAll("/", ".");
                            if (classNames.contains(className)) {
                                throw new RuntimeException("You have duplicate classes with the same name : " + className + " please remove duplicate classes ");
                            }
                            classNames.add(className);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("jarInput to JarFile IOException");
                } catch (NotFoundException e) {
                    e.printStackTrace();
                    System.out.println("jarInput to JarFile NotFoundException");
                }
            }
        }
        // 花费的时间
        long costTimeSec = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("read all class file cost " + costTimeSec + " second");
        for (String className : classNames) {
            try {
                ctClasses.add(classPool.get(className));
            } catch (NotFoundException e) {
                e.printStackTrace();
                System.out.println("class not found exception class name: " + className);
            }
        }

        Collections.sort(ctClasses, new Comparator<CtClass>() {
            @Override
            public int compare(CtClass class1, CtClass class2) {
                return class1.getName().compareTo(class2.getName());
            }
        });
        return ctClasses;
    }

}
