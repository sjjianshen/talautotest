package com.tal.autotest.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AutotestPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().create("autotest", AutotestTask.class);
    }
}
