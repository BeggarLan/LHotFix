package com.beggar.hotfix.plugin.autopatch

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

    @Override
    void apply(Project project) {
        mProject = project
        mLogger = project.getLogger()

        try {
            mLogger.quiet("********** hitFix codeInsert plugin parse xml start. *****************")
            mLogger.quiet("********** hitFix codeInsert plugin parse xml end. *****************")
            initConfig()
            // 注册transform
            project.android.registerTransform(new AutoPatchTransform(project))
        } catch (Throwable e) {
            e.printStackTrace()
            mLogger.error("********** hitFix codeInsert plugin parse " + Constants.ROBUST_XML + " error.*********");
        }
    }

    private void initConfig() {

    }

}
