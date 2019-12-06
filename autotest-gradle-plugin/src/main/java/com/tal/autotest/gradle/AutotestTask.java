package com.tal.autotest.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutotestTask extends DefaultTask {
    @TaskAction
    public void autotest() {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");

        List<String> paths = new ArrayList<>();
        ConfigurationContainer configurations = getProject().getConfigurations();
        List<String> configToSkip = Arrays.asList("apiElements", "implementation", "runtimeElements", "runtimeOnly");
        for (Map.Entry<String, Configuration> it : configurations.getAsMap().entrySet()) {
            System.out.println("loading configurations for " + it.getKey());
            if (!configToSkip.contains(it.getKey())) {
                try {
                    it.getValue().getFiles().forEach(file -> paths.add(file.getAbsolutePath()));
                } catch (Exception e) {
                    System.out.println("loading configurations failed for " + it.getKey());
                }
            }
        }
        String runtimePaths = String.join(File.pathSeparator, paths);
        String sysPaths = System.getProperty("java.class.path");
        if (sysPaths != null) {
            runtimePaths = runtimePaths + File.pathSeparator + sysPaths;
        }
        runtimePaths += File.pathSeparator + getProject().getBuildDir() + "/classes";
        cmd.add("-cp");
        cmd.add(runtimePaths);

        String mainApp = "com.tal.autotest.core.ApplicationKt";
        cmd.add(mainApp);

        String workDir = getProject().getProjectDir().getAbsolutePath();
        cmd.add(workDir);

        System.out.println("生成引擎开始运行, 运行命令");
//        System.out.println(String.join(" ", cmd));

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true); // redirect error stream to output stream
        builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        builder.directory(new File(workDir));
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("跳过该模块的生成");
        }
        System.out.println("生成完毕");
    }
}
