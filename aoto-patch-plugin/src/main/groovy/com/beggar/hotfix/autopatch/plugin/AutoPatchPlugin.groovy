package com.beggar.hotfix.autopatch.plugin

import com.beggar.hotfix.autopatch.AutoPatchConfig
import com.beggar.hotfix.autopatch.CodeInsertMethodZipFileParser
import com.beggar.hotfix.autopatch.HotfixXMLParser
import com.beggar.hotfix.base.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

/**
 * author: lanweihua
 * created on: 2022/6/13 1:18 下午
 * description: 打patch包的插件
 */
class AutoPatchPlugin implements Plugin<Project> {

    private Project mProject
    private Logger mLogger

    private AutoPatchConfig mAutoPatchConfig = new AutoPatchConfig()

    @Override
    void apply(Project project) {
        mProject = project
        mLogger = project.getLogger()

        try {
            mLogger.quiet("********** AutoPatchPlugin apply. *****************")
            initConfig()

            project.android.registerTransform(new AutoPatchTransform(project, mAutoPatchConfig))
        } catch (Throwable e) {
            e.printStackTrace()
            mLogger.error("********** hitFix codeInsert plugin parse " + Constants.ROBUST_XML + " error.*********");
        }
    }

    // 初始化配置
    private void initConfig() {
        // 配置文件path
        String hotfixXmlPath = "${mProject.projectDir.path}${File.separator}${Constants.ROBUST_XML}"
        HotfixXMLParser.parse(mAutoPatchConfig, hotfixXmlPath, mLogger)

        // 经过代码插桩的method声明文件
        String methodZipFilePath = "${mProject.projectDir.path}${File.separator}${Constants.METHOD_MAP_OUT_PATH}"
        mAutoPatchConfig.mCodeInsertMethodMap = CodeInsertMethodZipFileParser.parse(methodZipFilePath, mLogger)
    }

}
