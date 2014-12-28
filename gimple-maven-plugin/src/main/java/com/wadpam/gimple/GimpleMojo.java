package com.wadpam.gimple;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by sosandstrom on 2014-12-27.
 */
@Mojo(name="gimple")
public class GimpleMojo extends AbstractMojo {

    @Parameter(property = "gimple.gitdir", defaultValue = "${basedir}")
    private String gitDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("gimple executing on " + gitDir);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            Repository repo = builder.setGitDir(new File(gitDir))
                    .readEnvironment()
                    .findGitDir()
                    .build();

            getLog().info("branch " + repo.getBranch());
        } catch (IOException e) {
            throw new MojoExecutionException("executing", e);
        }
    }
}
