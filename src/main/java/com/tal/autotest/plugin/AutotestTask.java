package com.tal.autotest.plugin;

import com.tal.autotest.tool.ApplicationKt;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class AutotestTask extends DefaultTask {
    @TaskAction
    public void autotest() {
        String[] args = new String[1];
        System.out.println("Autotest task begin");
        args[0] = getProject().getProjectDir().getAbsolutePath();
        ApplicationKt.main(args);
        System.out.println("Autotest task done");
    }
}
