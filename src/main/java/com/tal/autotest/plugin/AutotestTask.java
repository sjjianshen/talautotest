package com.tal.autotest.plugin;

import com.tal.autotest.tool.ClassGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AutotestTask extends DefaultTask {
    @TaskAction
    public void autotest() {
        System.out.println("Autotest task begin");
        List<URL> urls = new ArrayList<>();
        getProject().getConfigurations().getByName("compile").getFiles().forEach(file -> {
            try {
                System.out.println(file.toURI().toURL().toString());
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
        System.out.println("Autotest task do begin");
        new ClassGenerator(getProject().getProjectDir().getAbsolutePath(), urls.toArray(new URL[0])).launch();
        System.out.println("Autotest task done");
    }
}
