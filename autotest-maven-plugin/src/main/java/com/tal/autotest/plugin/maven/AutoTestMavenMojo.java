package com.tal.autotest.plugin.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
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
        String runtimePaths = String.join(File.pathSeparator, paths);
        cmd.add("-cp");
        cmd.add(runtimePaths);

        String mainApp = "com.tal.autotest.core.ApplicationKt";
        cmd.add(mainApp);

        String workDir = project.getBasedir().getAbsolutePath();
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
