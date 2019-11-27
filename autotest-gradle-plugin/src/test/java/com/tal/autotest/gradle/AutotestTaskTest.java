package com.tal.autotest.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Test;
import java.io.File;

import static org.junit.Assert.*;

public class AutotestTaskTest {
    @Test
    public void name() {
        Project project = ProjectBuilder.builder()
                .withProjectDir(new File("/Users/jianshen/workspace/autotest"))
                .build();
        project.getPlugins().apply("com.tal.autotest.plugin");

        assertTrue(project.getTasks().getByName("autotest")
                instanceof AutotestTask);
    }
}