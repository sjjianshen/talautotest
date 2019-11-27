package com.tal.autotest.plugin.maven;

//import com.tal.autotest.core.AutotestContext;
import com.tal.autotest.core.DirectoryClassLoader;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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
        List<URL> urls = new ArrayList<>();
        try {
            Set<String> classpathElements = new HashSet<>();
            classpathElements.addAll(project.getCompileClasspathElements());
            classpathElements.addAll(project.getRuntimeClasspathElements());
            classpathElements.addAll(project.getTestClasspathElements());
            classpathElements.forEach(path -> {
                try {
                    urls.add(new File(path).toURI().toURL());
                } catch (MalformedURLException e) {
                    System.out.println("定位依赖文件失败: " + path);
                    e.printStackTrace();
                }
            });
            System.out.println("开始生成");
            ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
            ClassLoader targetClassLoader =
                    new DirectoryClassLoader(project.getBasedir().getAbsolutePath(), urls.toArray(new URL[0]), "mvn",
                            oldCl);
            Thread.currentThread().setContextClassLoader(targetClassLoader);
            String[] params = new String[5];
            String workspace = project.getBasedir().getAbsolutePath();
            String projectType = "mvn";
            String outputPath = workspace + "/src/test/java";
            String outputClassPath = workspace + "/target/autotest/classes";
            String configFile = workspace + "/autotest/config.json";
            params[0] = workspace;
            params[1] = projectType;
            params[2] = outputPath;
            params[3] = outputClassPath;
            params[4] = configFile;

//            AutotestContext atc = new AutotestContext(projectType, workspace, configFile, outputPath, outputClassPath);
            Class<?> mainClass = targetClassLoader.loadClass("com.tal.autotest.core.Launcher");
            Constructor ctor = mainClass.getConstructor();
            Object instance = ctor.newInstance();
            Method method = mainClass.getDeclaredMethod("launch", String[].class);
            method.invoke(instance, new Object[] { params });
            System.out.println("生成完毕");
            Thread.currentThread().setContextClassLoader(oldCl);
        } catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
            System.out.println("解析工程依赖失败");
        } catch (ClassNotFoundException e) {
            System.out.println("classpath 中没有找到工具类, 跳过生成");

        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
