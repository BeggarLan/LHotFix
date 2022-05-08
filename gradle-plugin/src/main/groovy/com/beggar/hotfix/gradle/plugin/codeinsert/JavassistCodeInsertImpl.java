package com.beggar.hotfix.gradle.plugin.codeinsert;

import com.android.annotations.NonNull;
import com.beggar.hotfix.base.Constants;
import com.beggar.hotfix.gradle.plugin.codeinsert.CodeInsertStrategy;

import org.gradle.internal.impldep.com.jcraft.jsch.HASH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;

/**
 * author: BeggarLan
 * created on: 2022/5/8 21:18
 * description: JavaAssist字节码织入
 */
public class JavassistCodeInsertImpl extends CodeInsertStrategy {

    public JavassistCodeInsertImpl
            (@NonNull List<String> hotfixPackageList,
             @NonNull List<String> hotfixMethodList,
             @NonNull List<String> exceptPackageList,
             @NonNull List<String> exceptMethodList) {
        super(hotfixPackageList, hotfixMethodList, exceptPackageList, exceptMethodList);
    }

    @Override
    public void insertCode(@NonNull List<CtClass> ctClasses, @NonNull File jarFile) throws IOException, CannotCompileException {
        ZipOutputStream zipOutputStream = new JarOutputStream(new FileOutputStream(jarFile));
        for (CtClass ctClass : ctClasses) {
            // 插入code
            if (isNeedInsert(ctClass.getName())) {
                // 给类修饰符加上public
                ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));

                // 接口跳过(因此default方法没法修复)、自身无方法的跳过
                if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
                    // 把class文件打入zip
                    zipFile(ctClass.toBytecode(), zipOutputStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                    continue;
                }

                // 是否插入了field
                boolean hasInsertField = false;
                // 遍历该类声明的所有method(包括构造方法)
                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    // 添加field
                    if (!hasInsertField) {
                        hasInsertField = true;
                        ClassPool classPool = ctBehavior.getDeclaringClass().getClassPool();
                        CtClass fieldClassName = classPool.getOrNull(Constants.HOTFIX_INTERFACE_NAME);
                        CtField ctField = new CtField(fieldClassName, Constants.HOTFIX_INSERT_FIELD_NAME, ctClass);
                        ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
                        ctClass.addField(ctField);
                    }

                    if (!isQualifiedMethod(ctBehavior)) {
                        continue;
                    }

                }
            }
            // 把class文件打入zip
            zipFile(ctClass.toBytecode(), zipOutputStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
        }
    }

    /**
     * 判断方法是否合格
     */
    private boolean isQualifiedMethod(@NonNull CtBehavior ctBehavior) {
        // 方法是否是静态块构造
        if (ctBehavior.getMethodInfo().isStaticInitializer()) {
            return false;
        }

        // TODO: 2022/5/8 继续写
        // 合成方法
        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
        if ((ctBehavior.getModifiers() & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(ctBehavior.getModifiers())) {
            return false;
        }
        if (ctBehavior.getMethodInfo().isConstructor()) {
            return false;
        }

        if ((ctBehavior.getModifiers() & AccessFlag.ABSTRACT) != 0) {
            return false;
        }
        if ((ctBehavior.getModifiers() & AccessFlag.NATIVE) != 0) {
            return false;
        }
        if ((ctBehavior.getModifiers() & AccessFlag.INTERFACE) != 0) {
            return false;
        }

        if (ctBehavior.getMethodInfo().isMethod()) {
            if (AccessFlag.isPackage(ctBehavior.getModifiers())) {
                ctBehavior.setModifiers(AccessFlag.setPublic(ctBehavior.getModifiers()));
            }
            boolean flag = isMethodWithExpression((CtMethod) ctBehavior);
            if (!flag) {
                return false;
            }
        }
        //方法过滤
        if (isExceptMethodLevel && exceptMethodList != null) {
            for (String exceptMethod : exceptMethodList) {
                if (ctBehavior.getName().matches(exceptMethod)) {
                    return false;
                }
            }
        }

        if (isHotfixMethodLevel && hotfixMethodList != null) {
            for (String name : hotfixMethodList) {
                if (ctBehavior.getName().matches(name)) {
                    return true;
                }
            }
        }
        return true;
    }


}
