package com.nilsw13.starter_kit_cli;

import com.nilsw13.starter_kit_cli.records.DatabaseConfig;
import com.nilsw13.starter_kit_cli.records.FrontendFrameworkConfig;
import com.nilsw13.starter_kit_cli.records.MailServiceConfig;
import com.nilsw13.starter_kit_cli.records.ProjectSetUp;
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



    private final LineReader lineReader;
    private final Terminal terminal;
    private final GithubService githubService;


    public ApplicationCustomStartup(GithubService githubService, LineReader lineReader, Terminal terminal) {
        this.githubService = githubService;
        this.lineReader = lineReader;
        this.terminal = terminal;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onStartUp() throws InterruptedException, GitAPIException, IOException {

// Create a bordered box with properly aligned text
        int boxWidth = 40;
        String title = "Spring Boot SaaS Starter";
        String author = "by @nilsw13";

// Calculate centering padding for each line
        int titlePadding = (boxWidth - title.length()) / 2;
        int authorPadding = (boxWidth - author.length()) / 2;

        terminal.writer().println("\n" + GREEN + "┌" + "─".repeat(boxWidth) + "┐" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(titlePadding) + title + " ".repeat(boxWidth - titlePadding - title.length()) + "│" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(boxWidth) + "│" + RESET);
        terminal.writer().println(GREEN + "│" + " ".repeat(authorPadding) + author + " ".repeat(boxWidth - authorPadding - author.length()) + "│" + RESET);
        terminal.writer().println(GREEN + "└" + "─".repeat(boxWidth) + "┘" + RESET);
        terminal.writer().println();
        boolean isFolderCreated = false;

        String packageName = lineReader.readLine(
                PURPLE + BOLD + "Package name" + RESET + " (no spaces): " + CYAN
        );
        terminal.writer().print(RESET);


        String projectName = lineReader.readLine(
                PURPLE + BOLD + "Project name" + RESET + " (no spaces): " + CYAN
        );
        terminal.writer().print(RESET);

        System.out.println(packageName + "."+ projectName);

        try {

            DatabaseConfig db =  dbConfig();
            MailServiceConfig mail = mailServiceConfig();
            FrontendFrameworkConfig frontend = frontendFrameworkConfig();

            projectConfig = new ProjectSetUp(packageName, projectName, db.desc(), mail.desc(), frontend.desc());
            getSummary();
            Git git = githubService.cloneDefaultRepo(projectName);
            isFolderCreated = true;
            System.out.println(git.toString());

            getProjectSetupLoading();
            SpringApplication.exit(applicationContext, () -> 0);


        } catch (Exception e) {
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

    public DatabaseConfig dbConfig() throws InterruptedException {
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
            case 1:
                DatabaseConfig psqlConfig = new DatabaseConfig(1, "PostgreSQL");
                System.out.println("Updating files to set-up postgreSQL configuration");
                Thread.sleep(3000);
                return psqlConfig;
            case 2:
                DatabaseConfig msqlConfig = new DatabaseConfig(1, "MySql");
                System.out.println("Updating files to set-up MySql configuration");
                Thread.sleep(3000);
                return msqlConfig;
            case 3:
                DatabaseConfig mariaConfig = new DatabaseConfig(1, "MariaDB");
                System.out.println("Updating files to set-up MariaDB configuration");
                Thread.sleep(3000);
                return mariaConfig;
            default:
                DatabaseConfig defaultConfig = new DatabaseConfig(1, "PostgreSQL");
                System.out.println("Updating files to set-up postgreSQL configuration");
                Thread.sleep(3000);
                return defaultConfig;
        }
    }

    public MailServiceConfig mailServiceConfig() throws InterruptedException {
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
                System.out.println("Updating files to set-up Mailgun configuration");
                Thread.sleep(3000);
                return mailGunConfig;

            case 2:
                MailServiceConfig resendConfig = new MailServiceConfig(2, "Resend");
                System.out.println("Updating files to set-up Resend configuration");
                Thread.sleep(3000);
                return resendConfig;

            case 3:
                MailServiceConfig sendGridConfig = new MailServiceConfig(3, "Sendgrid");
                System.out.println("Updating files to set-up Sendgrid configuration");
                Thread.sleep(3000);
                return sendGridConfig;

            default:
                MailServiceConfig defaultConfig = new MailServiceConfig(1, "Mailgun");
                System.out.println(defaultConfig);
                Thread.sleep(3000);
                return defaultConfig;
        }


    }

    public FrontendFrameworkConfig frontendFrameworkConfig() throws InterruptedException {
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
                System.out.println("Updating files to set-up VueJs configuration");
                Thread.sleep(3000);
                return vueJsConfig;
            case 3:
                FrontendFrameworkConfig angularConfig = new FrontendFrameworkConfig(3, "Angular");
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
