package com.nilsw13.starter_kit_cli;

import com.nilsw13.starter_kit_cli.records.DatabaseConfig;
import com.nilsw13.starter_kit_cli.records.FrontendFrameworkConfig;
import com.nilsw13.starter_kit_cli.records.MailServiceConfig;
import com.nilsw13.starter_kit_cli.records.ProjectSetUp;
import com.nilsw13.starter_kit_cli.service.FilesEditorService;
import com.nilsw13.starter_kit_cli.service.GithubService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jline.console.impl.Builtins;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.shell.Command;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ApplicationCustomStartup {

    ProjectSetUp projectConfig;
    private ApplicationContext applicationContext;

    // Constantes de couleurs ANSI
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String CYAN = "\033[36m";
    public static final String BLACK = "\033[30m";
    public static final String GREEN = "\033[32m";
    public static final String YELLOW = "\033[33m";
    public static final String BLUE = "\033[34m";
    public static final String PURPLE = "\033[35m";
    public static final String RED = "\033[31m";
    public static final String BG_BLUE = "\033[44m";
    public static final String BG_PURPLE = "\033[45m";  // Fond violet standard
    public static final String BG_BRIGHT_PURPLE = "\033[105m";  // Fond violet clair
    public static final String BG_GREEN = "\033[42m";        // Fond vert standard
    public static final String BG_BRIGHT_GREEN = "\033[102m"; // Fond vert clair
    public static final String BG_DARK_GREEN = "\033[48;5;22m"; // Fond vert foncé (Palette étendue)
    public static final String MAGENTA = "\u001B[35m";
    final String DB_QUESTION = String.format(
            GREEN + BOLD + "Choose your Database :" + RESET + "\n" +
                    CYAN + "[1] - PostgreSQL" + RESET + "\n" +
                    CYAN + "[2] - MySQL" + RESET + "\n" +
                    CYAN + "[3] - MariaDB" + RESET + "\n\n" +
                    GREEN + "Your choice (1-3): " + RESET
    );

    final String MAIL_QUESTION = String.format(
          GREEN + BOLD + "Choose your Mail service : " + RESET + "\n" +
                  CYAN + "[1] - Mailgun " + RESET + "\n" +
                  CYAN + "[2] - Resend " + RESET + "\n" +
                  CYAN + "[3] - Sendgrid" + RESET + "\n" + "\n"
                  +
                  GREEN + "Your choice (1-3): " + RESET
    );

    final String FRONT_QUESTION = String.format(
         PURPLE + BOLD + "Choose your Frontend framework : " + RESET + "\n" +
                 CYAN + "[1] - ReactJs " + RESET + "\n" +
                 CYAN + "[2] - VueJs " + RESET + "\n" +
                 CYAN + "[3] - Angular" + RESET + "\n" + "\n"
                 +
                 GREEN + "Your choice (1-3): " + RESET
    );

    final int[] VALID_ANSWER = {1, 2, 3};
    private boolean isPostgreSQL = false;
    private boolean isMySql = false;
    private boolean isMariaDb = false;
    private String projectName = "";



    private final LineReader lineReader;
    private final Terminal terminal;
    private final GithubService githubService;
    private final FilesEditorService filesEditorService;



    public ApplicationCustomStartup(GithubService githubService, LineReader lineReader, Terminal terminal, FilesEditorService filesEditorService) {
        this.githubService = githubService;
        this.lineReader = lineReader;
        this.terminal = terminal;
        this.filesEditorService = filesEditorService;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onStartUp() throws InterruptedException, GitAPIException, IOException {
        int boxWidth = 40;
        String title = "Spring Boot SaaS Starter";
        String author = "by @nilsw13";
        int titlePadding = (boxWidth - title.length()) / 2;
        int authorPadding = (boxWidth - author.length()) / 2;
        terminal.writer().println("\n" + GREEN + "┌" + "─".repeat(boxWidth) + "┐" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(titlePadding) + title + " ".repeat(boxWidth - titlePadding - title.length()) + "│" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(boxWidth) + "│" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(authorPadding) + author + " ".repeat(boxWidth - authorPadding - author.length()) + "│" + RESET);
        terminal.writer().println(GREEN + "└" + "─".repeat(boxWidth) + "┘" + RESET);
        terminal.writer().println();
        boolean isFolderCreated = false;


        // on demande le nom de package et le nom du projet
        String packageName = lineReader.readLine(
                PURPLE + BOLD + "Package name" + RESET + " (no spaces): " + CYAN
        );
        
        terminal.writer().print(RESET);
        projectName = lineReader.readLine(
                PURPLE + BOLD + "Project name" + RESET + " (no spaces): " + CYAN
        );
        terminal.writer().print(RESET);
        System.out.println(packageName + "."+ projectName);
        if (packageName == null | projectName == null) {
            System.out.println("Error, required infos for cloning remote project not found.");
            return;
        }

        try {

            Git git = githubService.cloneDefaultRepo(projectName);
            isFolderCreated = true;

            // First Stone : change name project in properties and XML => new project name set up in config files
            Map<String , String> newNameProperties = new HashMap<>();
            newNameProperties.put("spring.application.name", projectName);
            filesEditorService.updateApplicationProperties(projectName, newNameProperties);
            filesEditorService.setUpProjectRefs(projectName,  packageName);


        // maintenant on passe a la phase ou l'user doit faire des choix


            DatabaseConfig db =  dbConfig();
            MailServiceConfig mail = mailServiceConfig();
            FrontendFrameworkConfig frontend = frontendFrameworkConfig();
            projectConfig = new ProjectSetUp(packageName, projectName, db.desc(), mail.desc(), frontend.desc());
            getSummary();
            getProjectSetupLoading();



        } catch (Exception e) {
            // ici on gere l'erreur en supprimant toute ce qui a etait crée depuis le depart sur le post local. !!! CETTE PARTIE POSE PROBLEME A CHAQUE FOIS ON RENTRE DANS CETTE EXECUTION MEME SI IL N'Y PAS EU D'ERREUR CONCRETE
            System.out.println("Error while creating new project. Please try again or contact us.");
            if (isFolderCreated && packageName != null) {


              try {
                  File projectDir = new File(projectName);
                  if (projectDir.exists()) {
                      System.out.println("Rolling back ... Deleting project directory..");
                      githubService.deleteDirectory(projectDir);
                  }
              } catch (Exception cleanError) {
                  System.out.println("Failed to delete created directory");
                  System.out.println("Please manually delete the directory " + new File(projectName).getAbsolutePath());
              }
            }
        }



    }

    public void getSummary() {
        System.out.printf("Project summary : \n" +
              BG_DARK_GREEN +BOLD+  "========================================================================================" + RESET + "\n" +
              BG_DARK_GREEN +BOLD+  "- PROJECT PACKAGE : " + projectConfig.packageName() + "                                 " + RESET + "\n" +
              BG_DARK_GREEN +BOLD+  "- PROJECT NAME : " + projectConfig.projectName()    + "                                 " + RESET + "\n" +
              BG_DARK_GREEN +BOLD+  "- PROJECT DATABASE : " + projectConfig.databaseName() + RESET + "\n" +
              BG_DARK_GREEN +BOLD+  "- PROJECT MAIL SERVICE : " + projectConfig.mailServiceName() + RESET + "\n" +
              BG_DARK_GREEN +BOLD+  "- PROJECT FRONT-END FRAMEWORK : " + projectConfig.frontEndFrameworkName() + RESET + "\n" +
              BG_DARK_GREEN +BOLD+   "========================================================================================" + RESET + "\n\n"
        );

    }

    public void getProjectSetupLoading() throws InterruptedException {
        terminal.writer().print("\033[s");
        terminal.writer().println("Creating files with good configuration. Please be patient you will soon be able to griiiiiiind !");
        terminal.writer().flush();
        Thread.sleep(3000);;
        terminal.writer().print("\033[u\033[K");
        terminal.writer().println("Files created. Your project is ready to go you can now open it in your IDE.\n\n\n");
        terminal.writer().println( "========================================================================================\n");
        terminal.writer().println("cd " + projectConfig.projectName());
        terminal.writer().println("idea . " + " - [Or any PATH ENV that you have configured on your system}\n");
        terminal.writer().println( "========================================================================================\n");
        terminal.writer().flush();
    }

    public DatabaseConfig dbConfig() throws Exception {
        int choice = 0;
        boolean validInput = false;

      while (!validInput) {
          try {
            choice = Integer.parseInt(lineReader.readLine(DB_QUESTION));
              for (int i = 0; i< VALID_ANSWER.length; i++) {
                  if (VALID_ANSWER[i] == choice) {
                      validInput = true;
                      break;
                  }
              }
              if (!validInput) {
                  System.out.println("Error ! please enter valid format answer");
              }

          } catch (NumberFormatException e) {
              System.out.println("Error ! please enter valid format answer");
          }
      }

        switch (choice) {

          /*
          * When user make a database choice we need to do 2 things :
          *  - update database properties
          *  - update pom.xml Database dependency
          */
            case 1:
                isPostgreSQL = true;
                List<String> pathElements = new ArrayList<>();
                pathElements.add("src");
                pathElements.add("main");
                pathElements.add("java");
                pathElements.add("com");
                Map<String, String> postgresProperties = new HashMap<>();
                postgresProperties.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/your_db");
                DatabaseConfig psqlConfig = new DatabaseConfig(1, "PostgreSQL");
                filesEditorService.updateApplicationProperties(projectName,  postgresProperties);
                System.out.println("Updating files to set-up postgreSQL configuration");
                Thread.sleep(3000);
                return psqlConfig;
            case 2:
                isMySql = true;
                Map<String , String> mysqlProperties = new HashMap<>();
                mysqlProperties.put("spring.datasource.url", "jdbc:mysql://localhost:3306/your_db");
                mysqlProperties.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
                DatabaseConfig msqlConfig = new DatabaseConfig(1, "MySql");
                filesEditorService.updateApplicationProperties(projectName, mysqlProperties);
                filesEditorService.updateDatabaseDependencyInXml(projectName, "mysql", "mysql-connnector-java");
                System.out.println("Updating files to set-up MySql configuration");
                Thread.sleep(3000);
                return msqlConfig;
            case 3:
                isMariaDb = true;
                Map<String, String> mariaProperties = new HashMap<>();
                mariaProperties.put("spring.datasource.url", "jdbc:mariadb://localhost:3306/your_db");
                mariaProperties.put("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");

                DatabaseConfig mariaConfig = new DatabaseConfig(1, "MariaDB");
                filesEditorService.updateApplicationProperties(projectName, mariaProperties);
                filesEditorService.updateDatabaseDependencyInXml(projectName, "org.mariadb.jdbc", "mariadb-java-client");
                System.out.println("Updating files to set-up MariaDB configuration");
                Thread.sleep(3000);
                return mariaConfig;

            default:
                isPostgreSQL = true;
                Map<String, String> defaultProperties = new HashMap<>();
                defaultProperties.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/your_db");
                DatabaseConfig defaultConfig = new DatabaseConfig(1, "PostgreSQL");
                filesEditorService.updateApplicationProperties(projectName, defaultProperties);
                System.out.println("Updating files to set-up postgreSQL configuration");
                Thread.sleep(3000);
                return defaultConfig;
        }
    }


    /*
     * When user make a mailService choice we need to do 2 things :
     *  - update mailService properties
     *  - update pom.xml mailService dependency
     */
    public MailServiceConfig mailServiceConfig() throws Exception {
        int choice = 0 ;
        boolean validInput = false;

        while (!validInput) {
            try {
                choice = Integer.parseInt(lineReader.readLine(MAIL_QUESTION));
                for (int i = 0; i< VALID_ANSWER.length; i++) {
                    if (VALID_ANSWER[i] == choice) {
                        validInput = true;
                        break;
                    }
                }
                if (!validInput) {
                    System.out.println("Error ! please enter valid format answer");
                }

            } catch (NumberFormatException e) {
                System.out.println("Error ! please enter valid format answer");
            }
        }



        switch (choice) {
            case 1:
                MailServiceConfig mailGunConfig = new MailServiceConfig(1, "Mailgun");
                filesEditorService.createMailServiceDependencyInXml(projectName, "com.mailgun", "mailgun-java", "1.0.0");
                System.out.println("Updating files to set-up Mailgun configuration");
                Thread.sleep(3000);
                return mailGunConfig;

            case 2:
                MailServiceConfig resendConfig = new MailServiceConfig(2, "Resend");
                filesEditorService.createMailServiceDependencyInXml(projectName, "com.resend", "resend-java", "1.0.0");
                System.out.println("Updating files to set-up Resend configuration");
                Thread.sleep(3000);
                return resendConfig;

            case 3:
                MailServiceConfig sendGridConfig = new MailServiceConfig(3, "Sendgrid");
                filesEditorService.createMailServiceDependencyInXml(projectName, "com.sendgrid", "sendgrid-java", "1.0.0");
                System.out.println("Updating files to set-up Sendgrid configuration");
                Thread.sleep(3000);
                return sendGridConfig;

            default:
                MailServiceConfig defaultConfig = new MailServiceConfig(1, "Mailgun");
                filesEditorService.createMailServiceDependencyInXml(projectName, "com.mailgun", "mailgun-java", "1.0.0");
                System.out.println(defaultConfig);
                Thread.sleep(3000);
                return defaultConfig;
        }


    }

    /*
     * When user make a frontend Framework choice we need to do 2 things :
     *  - update framework properties (localhost port ..)
     *  - if not reactJs choice => delete reactapp (springreact-frontend) else change nothing
     *  - if angular choice => config a new angular app with multitenancy config
     *  - if vueJs choice => config a new vueJs app with multitenancy confg
     *
     */
    public FrontendFrameworkConfig frontendFrameworkConfig() throws InterruptedException, IOException {
        int choice = 0;
        boolean validInput = false;
        while (!validInput) {
            try {
                choice = Integer.parseInt(lineReader.readLine(FRONT_QUESTION));

                for (int i = 0; i< VALID_ANSWER.length; i++) {
                    if (VALID_ANSWER[i] == choice) {
                        validInput = true;
                        break;
                    }
                }
                if (!validInput) {
                    System.out.println("Error ! please enter valid format answer");
                }


            } catch (NumberFormatException e){
                System.out.println("Error ! please enter valid format answer");
            }
        }

        switch (choice) {
            case 1:
                FrontendFrameworkConfig reactConfig = new FrontendFrameworkConfig(1, "Reactjs");
                System.out.println("Updating files to set-up ReactJs configuration");

                Thread.sleep(3000);
                return reactConfig;
            case 2:
                FrontendFrameworkConfig vueJsConfig = new FrontendFrameworkConfig(2, "VueJs");
                Map<String , String> vueProperties = new HashMap<>();
                vueProperties.put("app.frontend.url", "http://localhost:8080");
                vueProperties.put("spring.web.cors.allowed-origins", "http://localhost:8080,https://accounts.google.com");
                vueProperties.put("app.oauth2.redirect-uri", "http://localhost:8080/oauth2/redirect");
                filesEditorService.updateApplicationProperties(projectName, vueProperties);
                System.out.println("Updating files to set-up VueJs configuration");
                Thread.sleep(3000);
                return vueJsConfig;
            case 3:
                FrontendFrameworkConfig angularConfig = new FrontendFrameworkConfig(3, "Angular");
                Map<String, String> angularProperties = new HashMap<>();
                angularProperties.put("app.frontend.url", "http://localhost:4200");
                angularProperties.put("spring.web.cors.allowed-origins", "http://localhost:4200,https://accounts.google.com");
                angularProperties.put("app.oauth2.redirect-uri", "http://localhost:4200/oauth2/redirect");
                filesEditorService.updateApplicationProperties(projectName, angularProperties);
                System.out.println("Updating files to set-up Angular configuration");
                Thread.sleep(3000);
                return angularConfig;
            default:
                FrontendFrameworkConfig defaultConfig = new FrontendFrameworkConfig(1, "Reactjs");
                System.out.println("Updating files to set-up ReactJs configuration");
                Thread.sleep(3000);
                return defaultConfig;
        }
    }


}
