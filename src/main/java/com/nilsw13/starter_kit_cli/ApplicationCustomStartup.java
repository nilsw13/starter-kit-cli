package com.nilsw13.starter_kit_cli;

import com.nilsw13.starter_kit_cli.records.DatabaseConfig;
import com.nilsw13.starter_kit_cli.records.FrontendFrameworkConfig;
import com.nilsw13.starter_kit_cli.records.MailServiceConfig;
import com.nilsw13.starter_kit_cli.records.ProjectSetUp;
import com.nilsw13.starter_kit_cli.service.GithubService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationCustomStartup {

    ProjectSetUp projectConfig;

    final String DB_QUESTION = String.format(
            "Choose your Database : \n" +
            "[1] - PostgreSQL \n" +
            "[2] - MySql \n" +
            "[3] - MariaDB\n" + "\n"
    );

    final String MAIL_QUESTION = String.format(
            "Choose your Mail service : \n" +
                    "[1] - Mailgun \n" +
                    "[2] - Resend \n" +
                    "[3] - Sendgrid\n" + "\n"
    );

    final String FRONT_QUESTION = String.format(
            "Choose your Frontend framework : \n" +
                    "[1] - ReactJs \n" +
                    "[2] - Angular \n" +
                    "[3] - VueJs\n" + "\n"
    );



    private final LineReader lineReader;
    private final Terminal terminal;
    private final GithubService githubService;


    public ApplicationCustomStartup(GithubService githubService, LineReader lineReader, Terminal terminal) {
        this.githubService = githubService;
        this.lineReader = lineReader;
        this.terminal = terminal;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onStartUp() throws InterruptedException, GitAPIException {
        terminal.writer().println("==============================");
        terminal.writer().println("     Spring Boot Starter      ");
        terminal.writer().println("==============================");

        String packageName = lineReader.readLine("Package name ( No spaces ) :");
        String projectName = lineReader.readLine("Project name ( No spaces ) : ");

        System.out.println(packageName + "."+ projectName);
        DatabaseConfig db =  dbConfig();
        MailServiceConfig mail = mailServiceConfig();
        FrontendFrameworkConfig frontend = frontendFrameworkConfig();

        projectConfig = new ProjectSetUp(packageName, projectName, db.desc(), mail.desc(), frontend.desc());
        getSummary();

        Git git = githubService.cloneDefaultRepo(projectName);
        System.out.println(git.toString());

        getProjectSetupLoading();

        terminal.writer().flush();


    }

    public void getSummary() {
        System.out.printf("Project summary : \n" +
                "========================================================================================\n\n" +
                "- PROJECT PACKAGE : " + projectConfig.packageName() + "\n" +
                "- PROJECT NAME : " + projectConfig.projectName() + "\n" +
                "- PROJECT DATABASE : " + projectConfig.databaseName() + "\n" +
                "- PROJECT MAIL SERVICE : " + projectConfig.mailServiceName() + "\n" +
                "- PROJECT FRONT-END FRAMEWORK : " + projectConfig.frontEndFrameworkName() + "\n\n" +
                "========================================================================================\n\n"
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
        try {
            choice = Integer.parseInt(lineReader.readLine(DB_QUESTION));

        } catch (NumberFormatException e) {
            System.out.println("Error ! please enter valid format answer");
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

        try {
            choice = Integer.parseInt(lineReader.readLine(MAIL_QUESTION));

        } catch (NumberFormatException e) {
            System.out.println("Error ! please enter valid format answer");
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
        try {
            choice = Integer.parseInt(lineReader.readLine(FRONT_QUESTION));
        } catch (NumberFormatException e){
            System.out.println("Error ! please enter valid format answer");
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
