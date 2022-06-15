package com.beggar.hotfix.autopatch.plugin

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.beggar.hotfix.autopatch.JavassistUtil
import com.beggar.hotfix.base.Constants
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import org.gradle.api.logging.Logger

import java.util.zip.GZIPOutputStream

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

        // 代码插入
        // todo 最好用asm，快
        CodeInsertStrategy codeInsertStrategy = new JavassistCodeInsertImpl(
            mHotfixPackageList, mHotfixMethodList, mExceptPackageList, mExceptMethodList);
        codeInsertStrategy.insertCode(ctClasses, jarFile);
        writeMethodMapToFile(codeInsertStrategy.mMethodMap, Constants.METHOD_MAP_OUT_PATH);

        // 打印
        mLogger.quiet("********** hitFix code insert method list start. *****************")
        for (String method : codeInsertStrategy.mMethodMap.keySet()) {
            int id = codeInsertStrategy.mMethodMap.get(method);
            System.out.println("key is   " + method + "  value is    " + id)
        }
        mLogger.quiet("********** hitFix code insert method list end. *****************")

        long costTimeSec = (System.currentTimeMillis() - startTimeMs) / 1000
        mLogger.quiet("hitFix plugin transform time cost " + costTimeSec + "s")

        mLogger.quiet("******************************AutoPatchTransform transform end*************************");
    }

    // 改造过的方法名写入文件p
    private void writeMethodMapToFile(@NonNull Map<String, Integer> methodMap, @NonNull String path) {
        File file = new File("${mProject.buildDir.path}/${path}")
        // 如果文件不存在的话，创建文件
        if (!file.exists() && (!file.parentFile.mkdirs() || !file.createNewFile())) {
            logger.error(path + " file create error!!")
        }

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream()
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)
        objectOut.writeObject(methodMap)

        FileOutputStream fileOut = new FileOutputStream(file)
        //gzip压缩
        GZIPOutputStream gzip = new GZIPOutputStream(fileOut)
        gzip.write(byteOut.toByteArray())
        objectOut.close()

        gzip.flush()
        gzip.close()

        fileOut.flush()
        fileOut.close()
    }

}
