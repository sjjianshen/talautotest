package com.tal.autotest.plugin.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mojo(name = "generate", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class AutoTestMavenMojo extends AbstractMojo {
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Override
    public void execute() {
        System.out.println("任务开始");
        List<String> cmd = new ArrayList<>();
        cmd.add("java");

        List<String> paths = new ArrayList<>();

        try {
            Set<String> classpathElements = new HashSet<>();
            classpathElements.addAll(project.getCompileClasspathElements());
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.addAll(project.getTestClasspathElements());
            classpathElements.forEach(path -> paths.add(path));
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
            System.out.println("解析工程依赖失败");
        }
        String sysPaths = System.getProperty("java.class.path");
        if (sysPaths != null) {
            paths.add(sysPaths);
        }
        paths.add(project.getBasedir().getAbsolutePath() + "/target/classes");
        paths.add(detectToolJar());
        String runtimePaths = String.join(File.pathSeparator, paths);
        cmd.add("-cp");
        cmd.add(runtimePaths);

        String mainApp = "com.tal.autotest.core.ApplicationKt";
        cmd.add(mainApp);

        String workDir = project.getBasedir().getAbsolutePath();
        cmd.add(workDir);

        String appClassPath = workDir + "/target/classes";
        cmd.add(appClassPath);

        String appResourcePath = workDir + "/target/classes";
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

    private String detectToolJar() {
        return null;
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
