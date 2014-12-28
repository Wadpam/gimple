package com.wadpam.gimple;

/*
 * #%L
 * com.wadpam.gimple:gimple-maven-plugin
 * %%
 * Copyright (C) 2010 - 2014 Wadpam
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.io.IOException;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Created by sosandstrom on 2014-12-27.
 */
@Mojo(name="gimple")
public class GimpleMojo extends AbstractMojo {

    public static final String SUFFIX_SNAPSHOT = "-SNAPSHOT";
    public static final String GIMPLE_MAVEN_PLUGIN = "[gimple-maven-plugin] ";
    public static final String PREFIX_SCM_GIT = "scm:git:";
    @Parameter(property = "gimple.gitdir", defaultValue = "${basedir}")
    private String gitDir;

    @Parameter(property = "gimple.currentVersion", defaultValue = "${project.version}")
    private String currentVersion;

    @Parameter(property = "gimple.releaseVersion")
    private String releaseVersion;

    @Parameter(property = "gimple.nextVersion")
    private String nextVersion;

    @Component
    private MavenProject mavenProject;

    @Component
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repo = builder.setGitDir(new File(new File(gitDir), ".git"))
                    .readEnvironment()
                    .findGitDir()
                    .build();
            final String branch = repo.getBranch();
            if (null != branch) {
                if (!currentVersion.endsWith(SUFFIX_SNAPSHOT)) {
                    throw new MojoExecutionException("Maven project version not in SNAPSHOT: " + currentVersion);
                }

                getLog().info("gimple executing on " + gitDir);
                getLog().info("branch " + branch);

                if (null == releaseVersion) {
                    releaseVersion = currentVersion.substring(0, currentVersion.length() - SUFFIX_SNAPSHOT.length());
                }
                getLog().info("Transforming version from " + currentVersion + " to release version " + releaseVersion);

                Git git = new Git(repo);
                StatusCommand statusCommand = git.status();
                Status status = statusCommand.call();

                if (!status.isClean()) {
                    throw new MojoExecutionException("Git project is not clean: " + status.getUncommittedChanges());
                }

                // versions:set releaseVersion
                transformPomVersions(git, releaseVersion);

                // tag release
                Ref tagRef = git.tag()
                        .setMessage(GIMPLE_MAVEN_PLUGIN + "tagging release " + releaseVersion)
                        .setName(releaseVersion)
                        .call();

                // next development version
                if (null == nextVersion) {
                    nextVersion = getNextDevelopmentVersion(releaseVersion);
                }

                // versions:set nextVersion
                RevCommit nextRef = transformPomVersions(git, nextVersion);

                // push it all
                String developerConnection = mavenProject.getScm().getDeveloperConnection();
                if (developerConnection.startsWith(PREFIX_SCM_GIT)) {
                    developerConnection = developerConnection.substring(PREFIX_SCM_GIT.length());
                }
                RefSpec spec = new RefSpec(branch + ":" + branch);
                git.push().setRemote(developerConnection).setRefSpecs(spec).add(tagRef).call();
            }
        } catch (IOException e) {
            throw new MojoExecutionException("executing", e);
        } catch (GitAPIException e) {
            throw new MojoExecutionException("status", e);
        }
    }

    private RevCommit transformPomVersions(Git git, String newVersion) throws MojoExecutionException, GitAPIException {
        executeMojo(
                plugin("org.codehaus.mojo", "versions-maven-plugin", "2.1"),
                goal("set"),
                configuration(element(name("newVersion"), newVersion)),
                executionEnvironment(mavenProject, mavenSession, pluginManager));

        StatusCommand statusCommand = git.status();
        Status status = statusCommand.call();

        // git add
        for (String uncommitted : status.getUncommittedChanges()) {
            getLog().info("  adding to git index: " + uncommitted);
            AddCommand add = git.add();
            add.addFilepattern(uncommitted);
            add.call();
        }

        // git commit
        CommitCommand commit = git.commit();
        commit.setMessage(GIMPLE_MAVEN_PLUGIN + "pom version " + newVersion);
        return commit.call();
    }

    protected static String getNextDevelopmentVersion(String releaseVersion) {
        String majmin[] = releaseVersion.split("\\.");
        String token = null;
        StringBuffer newVersion = new StringBuffer();
        for (String mm : majmin) {
            if (null != token) {
                newVersion.append(token);
                newVersion.append('.');
            }
            token = mm;
        }

        // parse minor
        int minor = Integer.parseInt(token);
        newVersion.append(Integer.toString(minor+1));
        newVersion.append(SUFFIX_SNAPSHOT);

        return newVersion.toString();
    }
}
