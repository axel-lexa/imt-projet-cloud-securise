package com.imt.cicd.dashboard.service;

import org.eclipse.jgit.api.Git;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.File;

@Service
public class GitService {

    public void cloneRepository(String repoUrl, String branch, File destination) throws Exception {
        if (destination.exists()) {
            FileSystemUtils.deleteRecursively(destination);
        }

        Git.cloneRepository()
                .setURI(repoUrl)
                .setBranch(branch)
                .setDirectory(destination)
                .call();
    }
}
