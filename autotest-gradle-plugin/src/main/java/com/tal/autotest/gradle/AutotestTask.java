package com.tal.autotest.gradle;

import com.tal.autotest.core.DirectoryClassLoader;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.tasks.TaskAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AutotestTask extends DefaultTask {
    @TaskAction
    public void autotest() {
        List<URL> urls = new ArrayList<>();
        ConfigurationContainer configurations = getProject().getConfigurations();
//        configurations.getByName("compile").getFiles().forEach(file -> {
//            try {
//                urls.add(file.toURI().toURL());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//        });
//        configurations.getByName("test").getFiles().forEach(file -> {
//            try {
//                urls.add(file.toURI().toURL());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//        });
        List<String> configToSkip = Arrays.asList("apiElements", "implementation", "runtimeElements", "runtimeOnly");
        for (Map.Entry<String, Configuration> it : configurations.getAsMap().entrySet()) {
            System.out.println("loading configurations for " + it.getKey());
            if (!configToSkip.contains(it.getKey())) {
                try {
                    it.getValue().getFiles().forEach(file -> {
                        try {
                            urls.add(file.toURI().toURL());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    System.out.println("loading configurations failed for " + it.getKey());
                }
            }
        }

        System.out.println("开始生成");
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        ClassLoader targetClassLoader =
                new DirectoryClassLoader(getProject().getProjectDir().getAbsolutePath(),
                        urls.toArray(new URL[0]), "gradle", oldCl);
        Thread.currentThread().setContextClassLoader(targetClassLoader);
        String[] params = new String[5];
        String workspace = getProject().getProjectDir().getAbsolutePath();
        String projectType = "mvn";
        String outputPath = workspace + "/src/test/java";
        String outputClassPath = workspace + "/build/autotest/classes";
        String configFile = workspace + "/autotest/config.json";
        params[0] = workspace;
        params[1] = projectType;
        params[2] = outputPath;
        params[3] = outputClassPath;
        params[4] = configFile;
        try {
            Class<?> mainClass = targetClassLoader.loadClass("com.tal.autotest.core.Launcher");
            Constructor ctor = mainClass.getConstructor();
            Object instance = ctor.newInstance();
            Method method = mainClass.getDeclaredMethod("launch", String[].class);
            method.invoke(instance, new Object[] { params });
            Thread.currentThread().setContextClassLoader(oldCl);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            System.out.println(e.getMessage());
            System.out.println("跳过该模块的生成");
            e.printStackTrace();
        }
        System.out.println("生成完毕");
    }
}
