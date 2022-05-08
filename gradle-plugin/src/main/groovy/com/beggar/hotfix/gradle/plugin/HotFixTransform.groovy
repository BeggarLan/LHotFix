package com.beggar.hotfix.gradle.plugin;

import com.android.annotations.NonNull;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager
import com.beggar.hotfix.gradle.plugin.javassist.JavassistUtil
import javassist.CtClass;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;

/**
 * author: BeggarLan
 * created on: 2022/5/7 17:48
 * description:
 */
class HotFixTransform extends Transform {
    @NonNull
    private Project mProject;
    @NonNull
    private Logger mLogger;

    private List<String> mHotfixPackageList = new ArrayList<>();
    private List<String> mHotfixMethodList = new ArrayList<>();
    private List<String> mExceptPackageList = new ArrayList<>();
    private List<String> mExceptMethodList = new ArrayList<>();

    HotFixTransform(
            @NonNull Project project,
            @NonNull List<String> hotfixPackageList,
            @NonNull List<String> hotfixMethodList,
            @NonNull List<String> exceptPackageList,
            @NonNull List<String> exceptMethodList) {
        this.mProject = project;
        mLogger = project.getLogger();
        this.mHotfixPackageList = hotfixPackageList;
        this.mHotfixMethodList = hotfixMethodList;
        this.mExceptPackageList = exceptPackageList;
        this.mExceptMethodList = exceptMethodList;
    }

    // Transform对应的task的名称
    @Override
    String getName() {
        return "hotfix-plugin";
    }

    // 输入的类型，这里可以过滤我们想要处理的文件类型
    // 可供我们去处理的有两种类型, 分别是编译后的java代码, 以及资源文件(非res下文件, 而是assests内的资源)
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    /**
     * 处理的数据作用域
     * <p>
     * PROJECT： 只处理当前项目
     * SUB_PROJECTS：只处理子项目
     * PROJECT_LOCAL_DEPS：只处理当前项目的本地依赖,例如jar, aar
     * EXTERNAL_LIBRARIES：只处理外部的依赖库
     * PROVIDED_ONLY：只处理本地或远程以provided形式引入的依赖库
     * TESTED_CODE：测试代码
     */
    @Override
    Set<QualifiedContent.ScopeType> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    // 是否支持增量编译
    // TODO: 2022/5/7 待确认
    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        mLogger.quiet("******************************hotfix plugin transform start*************************");
        long startTime = System.currentTimeMillis();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        // 非增量编译的情况下，删除之前生成的文件
        if (!transformInvocation.isIncremental()) {
            outputProvider.deleteAll();
        }
        // 数出文件
        File jarFile = outputProvider.getContentLocation("main", getOutputTypes(), getScopes(), Format.JAR);
        // TODO: 2022/5/7  getParentFile没懂
//        if(!jarFile.getParentFile().exists()){
//            jarFile.getParentFile().mkdirs();
//        }
        if (jarFile.exists()) {
            jarFile.delete();
        }

        ClassPool classPool = new ClassPool();

        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        mProject.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
        }

        // class --> CtClass
        List<CtClass> ctClasses = JavassistUtil.toCtClasses(transformInvocation.getInputs(), classPool)

        

    }
}
