package com.nilsw13.starter_kit_cli.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

@Component
public class GithubService {
    private static final String GITHUB_REPO_URL = "https://github.com/nilsw13/spring-react";

    public Git cloneDefaultRepo(String projectName) {
        Git git = new Git()
    }
}
