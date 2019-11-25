package com.tal.autotest.plugin.maven;

import com.tal.autotest.core.ClassGenerator;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mojo(name = "sayhi", requiresDependencyResolution = ResolutionScope.TEST,
        defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class AutoTestMavenMojo extends AbstractMojo {
    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;
    
    @Override
    public void execute() {
        System.out.println("任务开始");
        List<URL> urls = new ArrayList<>();
        try {
//            List<String> classpathElements = project.getRuntimeClasspathElements();
            Set<String> classpathElements = new HashSet<>();
            classpathElements.addAll(project.getCompileClasspathElements());
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.forEach(path -> {
                try {
                    urls.add(new File(path).toURI().toURL());
                } catch (MalformedURLException e) {
                    System.out.println("定位依赖文件失败: " + path);
                    e.printStackTrace();
                }
            });
            System.out.println("开始生成");
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ClassLoader newCl = new URLClassLoader(urls.toArray(new URL[0]), cl);
            Thread.currentThread().setContextClassLoader(newCl);
            new ClassGenerator(project.getBasedir().getAbsolutePath(), urls.toArray(new URL[0])).launch();
            System.out.println("生成完毕");
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
            System.out.println("解析工程依赖失败");
        }
    }
}
