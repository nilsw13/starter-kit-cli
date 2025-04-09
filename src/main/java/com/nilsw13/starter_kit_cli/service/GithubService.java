package com.nilsw13.starter_kit_cli.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class GithubService {
    private static final String GITHUB_REPO_URL = "https://github.com/nilsw13/spring-react.git";

    public Git cloneDefaultRepo(String projectName) throws GitAPIException {



        try{
            Path currentDirectory = Paths.get("./");
            Path projectDirectory = currentDirectory.resolve(projectName);
            File projectFolder = projectDirectory.toFile();
            if (!projectFolder.exists()) {
                boolean created = projectFolder.mkdirs();
                if (!created) {
                    System.out.println("Impossible de créer le dossier : " + projectFolder.getAbsolutePath());
                    return null;
                }
            }

            Git git = Git.cloneRepository()
                    .setURI(GITHUB_REPO_URL)
                    .setDirectory(projectFolder)
                    .call();

             // maintenant il faut naviguer dans le nouveau repos pour supprimer le dossier git et ensuite en crée un nouveau
            File gitDir = new File(projectFolder, ".git");
            if (gitDir.exists()){
                deleteDirectory(gitDir);
            }

            // maintenant il faut init un nouveau depot
            git = Git.init().setDirectory(projectFolder).call();

             return git;

        } catch (GitAPIException | IOException e) {
            System.out.println(e.getMessage());
        }


        return null;
    }

    public void deleteDirectory(File dir) throws IOException {
        if(dir.exists()) {
            File[] files = dir.listFiles();
            if (files!=null){
                for (File file : files) {
                    if(file.isDirectory()){
                        deleteDirectory(file);
                    }else {
                        boolean deleted = file.delete();
                        if(!deleted) {
                            throw new IOException("Error while deleting .git repository");
                        }
                    }
                }

            }
            boolean deleted = dir.delete();
            if(!deleted) {
                throw  new IOException("Error deleting " + dir );
            }


        }


    }
}
