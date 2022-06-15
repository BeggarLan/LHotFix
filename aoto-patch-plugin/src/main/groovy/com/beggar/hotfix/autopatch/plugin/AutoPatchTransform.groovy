package com.beggar.hotfix.autopatch.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.beggar.hotfix.autopatch.AutoPatchConstants
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * author: lanweihua
 * created on: 2022/6/13 1:21 下午
 * description: 用来打patch包
 */
class AutoPatchTransform extends Transform {
    @NonNull
    private Project mProject;
    @NonNull
    private Logger mLogger;

    AutoPatchTransform(@NonNull Project project) {
        this.mProject = project
        mLogger = project.getLogger()
    }

    // Transform对应的task的名称
    @Override
    String getName() {
        return "AutoPatchTransform";
    }

    // 输入的类型，这里可以过滤我们想要处理的文件类型
    // 可供我们去处理的有两种类型, 分别是编译后的java代码, 以及资源文件(非res下文件, 而是assets内的资源)
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
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
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 是否支持增量编译
    // TODO: 2022/5/7 待确认
    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        mLogger.quiet("******************************AutoPatchTransform transform.*************************");
        long startTimeMs = System.currentTimeMillis();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        // 非增量编译的情况下，删除之前生成的文件
        if (!transformInvocation.isIncremental()) {
            outputProvider.deleteAll();
        }

        ClassPool classPool = new ClassPool();

        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        mProject.android.bootClasspath.each {
            classPool.appendClassPath((String) it.absolutePath)
//            println("bootClasspath" + (String) it.absolutePat)
        }

        // class --> CtClass
        List<CtClass> ctClasses = JavassistUtil.toCtClasses(transformInvocation.getInputs(), classPool)

        def costTimeMs = System.currentTimeMillis() - startTimeMs
        mLogger.quiet("add all CtClasses cost $costTimeMs ms , CtClass count : ${ctClasses.size()}")

        autoPatch(ctClasses)

        costTimeMs = (System.currentTimeMillis() - startTimeMs) / 1000
        mLogger.quiet("AutoPatchTransform transform time cost $costTimeMs ms")
        mLogger.quiet("******************************AutoPatchTransform transform end*************************")
        // 本来就是给项目打patch包用的，打完了直接终止
        throw new RuntimeException("auto patch successfully")
    }

    void autoPatch(@NonNull List<CtClass> ctClasses) {
        // patch包生成文件夹
        String patchGeneratePath =
            mProject.buildDir.getAbsolutePath() + File.separator + AutoPatchConstants.PATCH_GENERATE_DIR + File.separator;
        // 先清理一下文件夹
        FileUtils.deleteDirectory(new File(patchGeneratePath))

        // todo 找出add、modify
    }

}
