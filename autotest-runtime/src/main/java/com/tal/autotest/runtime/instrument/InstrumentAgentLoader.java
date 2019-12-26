package com.tal.autotest.runtime.instrument;

import com.tal.autotest.runtime.mock.MockFrameWork;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class InstrumentAgentLoader {
    public static void initialize() {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);

        String jarFilePath = getJarPath();
        if(jarFilePath==null){
            throw new RuntimeException("Cannot find either the compilation target folder nor the EvoSuite jar in classpath: "+System.getProperty("java.class.path"));
        } else {
            System.out.println("Using JavaAgent in "+jarFilePath);
        }

        try {
            attachAgent(pid, jarFilePath, InstrumentAgentLoader.class.getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void attachAgent(String pid, String jarFilePath, ClassLoader classLoader) throws Exception {
        Class<?> string = classLoader.loadClass("java.lang.String");
        try {
            Class.forName("com.sun.tools.attach.spi.AttachProvider");
        } catch (ClassNotFoundException e) {
            if (classLoader instanceof URLClassLoader) {
                classLoader = getToolJarLoader();
            }
        }
        Class<?> provider = Class.forName("com.sun.tools.attach.spi.AttachProvider", true, classLoader);
        Method getProviders = provider.getDeclaredMethod("providers");
        List<?> list = (List<?>) getProviders.invoke(null);
        if(list==null || list.isEmpty()){
            String msg = "AttachProvider.providers() failed to return any provider. Tool classloader: "+classLoader;
            throw  new RuntimeException(msg);
        }
        if(list.stream().anyMatch(Objects::isNull)){
            throw new RuntimeException("AttachProvider.providers() returned null values");
        }

        Class<?> clazz = Class.forName("com.sun.tools.attach.VirtualMachine", true, classLoader);
        Method attach = clazz.getMethod("attach", string);

        Object instance = null;
        try {
            instance = attach.invoke(null, pid);
        } catch (Exception e){
            throw new RuntimeException("Failed to attach Java Agent. Tool classloader: "+classLoader,e);
        }
//        MockFrameWork.active();
        Method loadAgent = clazz.getMethod("loadAgent", string, string);
        loadAgent.invoke(instance, jarFilePath, "");
        Method detach = clazz.getMethod("detach");
        detach.invoke(instance);
    }

    private static ClassLoader getToolJarLoader() {
        List<String> locationsToSearch = new ArrayList<>();
        String homeProperty = System.getProperty("java.home");
        if (homeProperty != null) {
            locationsToSearch.add(homeProperty + "/../lib/tools.jar");
            locationsToSearch.add(homeProperty + "/lib/tools.jar");
            locationsToSearch.add(homeProperty + "/../Classes/classes.jar");
        }
        String homeEnv = System.getenv("JAVA_HOME");
        if (homeEnv != null) {
            locationsToSearch.add(homeEnv + "/../lib/tools.jar");
            locationsToSearch.add(homeEnv + "/lib/tools.jar");
            locationsToSearch.add(homeEnv + "/../Classes/classes.jar");
        }
        for (String loc : locationsToSearch) {
            if (Files.exists(Paths.get(loc))) {
                try {
                    ClassLoader classLoader = new URLClassLoader(new URL[] {new File(loc).toURI().toURL()});
                    Class.forName("com.sun.tools.attach.spi.AttachProvider", true, classLoader);
                    return classLoader;
                } catch (MalformedURLException | ClassNotFoundException e) {
                }
            }
        }
        return null;
    }

    private static String getJarPath() {
        String location = findInClassPath();
        if (location == null) {
            location = findInClassLoaderUrls(InstrumentAgentLoader.class.getClassLoader());
        }
        return location;
    }

    private static String findInClassPath() {
        String cpStr = System.getProperty("java.class.path");
        String[] cps = cpStr.split(File.pathSeparator);
        for (String cp : cps) {
            if (isAutotestRuntimeJar(cp)) {
                return cp;
            }
        }
        return null;
    }

    private static boolean isAutotestRuntimeJar(String location) {
        return location.contains("autotest-runtime");
    }

    private static String findInClassLoaderUrls(ClassLoader classLoader) {
        ClassLoader loader = classLoader;
        while (loader != null) {
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) loader;
                URL[] urls = urlClassLoader.getURLs();
                for (URL url : urls) {
                    try {
                        String location = new File(url.toURI()).getAbsolutePath();
                        if (isAutotestRuntimeJar(location)) {
                            return location;
                        }
                    } catch (URISyntaxException e) {
                        System.out.println("continue loop");
                    }
                }
            }
            loader = loader.getParent();
        }
        return null;
    }
}
