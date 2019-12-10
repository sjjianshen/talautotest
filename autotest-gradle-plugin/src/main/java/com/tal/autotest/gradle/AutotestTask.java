package com.tal.autotest.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

        String appClassPath = workDir + "/build/classes/java/main";
        cmd.add(appClassPath);

        String appResourcePath = workDir + "/build/resources/main";
        cmd.add(appResourcePath);

        System.out.println("生成引擎开始运行, 运行命令");

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true); // redirect error stream to output stream
        builder.directory(new File(workDir));
        try {
            Process process = builder.start();
            handleOutput(process);
            int exitCode = process.waitFor();
            if(exitCode != 0){
                System.out.println("Error in autotest");
            } else {
                System.out.println("autotest terminated");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("跳过该模块的生成");
        }
        System.out.println("生成完毕");
    }

    private void handleOutput(Process process) {
        Thread reader = new Thread(){
            @Override
            public void run(){
                try{
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    while(!this.isInterrupted()){
                        String line = in.readLine();
                        if(line!=null && !line.isEmpty()){
                            System.out.println(line);
                        }
                    }
                } catch(Exception e){
                    System.out.println("Exception while reading spawn process output: "+ e.toString());
                }
            }
        };
        reader.start();
        System.out.println("Started thread to read spawn process output");
    }
}
