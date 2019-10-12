package com.tal.autotest.plugin;

import com.tal.autotest.tool.ClassGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AutotestTask extends DefaultTask {
    @TaskAction
    public void autotest() {
        System.out.println("Autotest task begin");
        List<URL> urls = getProject().getConfigurations().getByName("compile").getFiles().stream().map(file -> {
            try {
                System.out.println(file.toURI().toURL().toString());
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        System.out.println("Autotest task do begin");
        new ClassGenerator(getProject().getProjectDir().getAbsolutePath(), urls.toArray(new URL[0])).launch();
        System.out.println("Autotest task done");
    }
}
