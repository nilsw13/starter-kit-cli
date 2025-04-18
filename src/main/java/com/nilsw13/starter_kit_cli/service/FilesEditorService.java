package com.nilsw13.starter_kit_cli.service;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class FilesEditorService {

    String oldPackage = "com.nilsw13";
    String oldProjectName = "springreact";

    // public methods

            //application.properties !! error , need to update sql dialect  for each choices
            public void updateApplicationProperties(String projectName, Map<String , String> newProperties) throws IOException {
                        Path propsPath = Paths.get(projectName, "src", "main", "resources", "application.properties");

                        if (!Files.exists(propsPath)) {
                            System.out.println("application.properties file not found at : " + propsPath.toString());
                            return;
                        }

                        List<String> lines = Files.readAllLines(propsPath);
                        List<String> newLines = new ArrayList<>();

                        //Set<String> processedKeys = new HashSet<>();

                        for (String line: lines) {

                            String trimmedLine = line.trim();

                            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")){
                                newLines.add(line);
                                continue;
                            }

                            int equalsIndex = trimmedLine.indexOf("=");
                            if(equalsIndex > 0) {
                                String key = trimmedLine.substring(0, equalsIndex).trim();

                                if (newProperties.containsKey(key)) {
                                    newLines.add(line.substring(0, line.indexOf('=') + 1) + newProperties.get(key));
                                    //processedKeys.add(key);
                                } else {
                                    newLines.add(line);
                                }
                            } else {
                                newLines.add(line);
                            }


                        }

                        Files.write(propsPath, newLines);

                    }

            //Pom.xml
            public void setUpProjectRefs(String projectName, String packageName) throws Exception {
                        Path pomPath = Paths.get(projectName, "pom.xml");
                        if (!Files.exists(pomPath)) {
                            System.out.println("error ! pom.xml not found at: " + pomPath);
                            return;
                        }

                        // load document
                        Document document = loadXmlDocument(pomPath);
                        // update xml file
                        updateXmlFile(projectName, packageName, document);
                        updateProjectPackage(projectName, packageName);








                        saveXmlDocument(document, pomPath);
                        System.out.println("Project name updated to : " + projectName);
                        System.out.println("Artifact Id updated to :" + projectName);
                        System.out.println("Description updated to :" + projectName + "Saas project");

                    }
            public void updateDatabaseDependencyInXml(String projectName,  String newgroup, String newArtifactId) throws Exception {
                String oldGroupId = "org.postgresql";
                String oldArtifactId = "postgresql";

                Path pomPath = Paths.get(projectName, "pom.xml");


                if (!Files.exists(pomPath)) {
                    System.out.println("error ! pom.xml not found at: " + pomPath);
                    return;
                }

                Document document = loadXmlDocument(pomPath);

                NodeList dependencyTag =  document.getElementsByTagName("dependency");
                for (int i =0; i < dependencyTag.getLength(); i++) {
                    Element childTag = (Element) dependencyTag.item(i);


                    NodeList groupeTag = childTag.getElementsByTagName("groupId");
                    NodeList artifactTag = childTag.getElementsByTagName("artifactId");

                    if (groupeTag.getLength() > 0 && artifactTag.getLength() > 0) {
                        String groupeIdValue =groupeTag.item(0).getTextContent();
                        String artifactIdValue = artifactTag.item(0).getTextContent();

                        if(groupeIdValue.startsWith("org.postgresql") && artifactIdValue.startsWith("postgresql")) {
                            groupeTag.item(0).setTextContent(newgroup);
                            artifactTag.item(0).setTextContent(newArtifactId);
                        }


                    }
                }

                saveXmlDocument(document, pomPath);


            }
            public void createMailServiceDependencyInXml(String projectName, String groupId, String artifactId, String version) throws Exception {
                    Path pomPath = Paths.get(projectName, "pom.xml");

                    if (!Files.exists(pomPath)){
                        System.out.println("Error pom.xml not found at : " + pomPath);
                        return;
                    }

                    Document document = loadXmlDocument(pomPath);

                    NodeList dependenciesNode = document.getElementsByTagName("dependencies");
                    if (dependenciesNode.getLength() == 0) {
                        System.out.println("Error dependencies tag not found");
                        return;
                    }


                   Node dependencyNode = dependenciesNode.item(0).appendChild(document.createElement("dependency"));

                    Node groupElement = dependencyNode.appendChild(document.createElement("groupId"));
                    Node artifactIdElement = dependencyNode.appendChild(document.createElement("artifactId"));
                    Node versionElement = dependencyNode.appendChild(document.createElement("version"));

                    groupElement.setTextContent(groupId);
                    artifactIdElement.setTextContent(artifactId);
                    versionElement.setTextContent(version);

                    saveXmlDocument(document, pomPath);


            }



            // need to implement a way to update all old  package reference for the new one
            public void updateProjectPackage(String projectName, String packageName) throws Exception {
                updatePackageNameInPackageAndImport(projectName, packageName, oldPackage);  // done
                updateProjectNameInPackageAndImport(projectName, oldProjectName, packageName, oldPackage); // done
                // NEED TO BE IMPLEMENTED // change structure directory name
                updatePackageDirectoryStructure(projectName, oldPackage, packageName); // to do
            }









    // private methods
    private void updateXmlFile(String projectName, String packageName,  Document document) {
        updateArtifactIdInPom(projectName, document);

        NodeList nameNodes = document.getElementsByTagName("name");
        NodeList descriptionNodes = document.getElementsByTagName("description");
        NodeList groupNodes = document.getElementsByTagName("groupId");


        // update name tag
        if (nameNodes.getLength() > 0 && descriptionNodes.getLength() > 0  && groupNodes.getLength() > 0 ) {
            nameNodes.item(0).setTextContent(projectName);
            descriptionNodes.item(0).setTextContent(projectName + "SaaS project");
            groupNodes.item(1).setTextContent("com."+packageName);

        } else {
            System.out.println("Error name , artifactId or description tag not found");
            throw new RuntimeException();
        }
    }
    private void updatePackageDirectoryStructure(String projectName, String oldPackage, String packageName) {
                // change package name in folder structure




    }
    private void updatePackageNameInPackageAndImport(String projectName, String newPackage, String oldPackage) {
        // Base path to search Java files
        Path javaPath = Paths.get(projectName, "src", "main", "java")
                .resolve(oldPackage.replace(".", File.separator));

        if (!Files.exists(javaPath)) {
            System.out.println("error: path not found at: " + javaPath);
            return;
        }

        try {
            Files.walk(javaPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        try {
                            List<String> lines = Files.readAllLines(javaFile);
                            List<String> updatedLines = new ArrayList<>();

                            for (String line : lines) {
                                // Remplace toute ancienne mention du package par le nouveau
                                if (line.trim().startsWith("package ") && line.contains(oldPackage)) {
                                    updatedLines.add(line.replace(oldPackage, "com." +newPackage));
                                } else if (line.trim().startsWith("import ") &&line.contains(oldPackage)) {
                                    // remplace aussi dans les imports si nécessaire
                                    updatedLines.add(line.replace(oldPackage, "com."+ newPackage));
                                } else  {
                                    updatedLines.add(line);
                                }
                            }

                            Files.write(javaFile, updatedLines);
                            System.out.println("✔ Package updated in: " + javaFile);

                        } catch (IOException e) {
                            System.err.println("❌ Error updating file: " + javaFile);
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            System.err.println("❌ Error walking through files at path: " + javaPath);
            e.printStackTrace();
        }
    }
    private void updateProjectNameInPackageAndImport(String projectName, String oldProjectName, String packageName, String oldPackageName ) {


        Path javaPath = Paths.get(projectName, "src", "main", "java", "com", "nilsw13", "springreact");

        if (!Files.exists(javaPath)){
            System.out.println("error: path not found at: " +  javaPath);
        }

        try {
            Files.walk(javaPath)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(javafile-> {
                        try {
                            List<String> lines = Files.readAllLines(javafile);
                            List<String> updatedLines = new ArrayList<>();

                            for(String line : lines) {
                                if (line.trim().startsWith("import ") && line.contains(oldPackageName)) {
                                    String updatedLine = line.replace(oldPackageName, packageName);
                                    updatedLines.add(updatedLine);

                                } else if (line.trim().startsWith("package ") && line.contains(oldProjectName)) {

                                    String updatedLine = line.replace(oldProjectName, projectName);
                                    updatedLines.add(updatedLine);

                                } else {
                                    updatedLines.add(line);
                                }
                            }

                            Files.write(javafile, updatedLines);
                            System.out.println("updated package in : " + javafile);



                        } catch (IOException e) {
                            System.err.println("Error reading or writing file: " + javafile);
                            e.printStackTrace();
                        }



                        try {
                            List<String> lines = Files.readAllLines(javafile);
                            List<String> updatedLines = new ArrayList<>();

                            for(String line : lines) {
                                if (line.trim().startsWith("import ") && line.contains(oldProjectName)) {
                                    String updatedLine = line.replace(oldProjectName, projectName);
                                    updatedLines.add(updatedLine);

                                } else {
                                    updatedLines.add(line);
                                }
                            }

                            Files.write(javafile, updatedLines);
                            System.out.println("updated package in : " + javafile);



                        } catch (IOException e) {
                            System.err.println("Error reading or writing file: " + javafile);
                            e.printStackTrace();
                        }





                    });
        } catch (IOException e) {
            System.err.println("Error walking through files in path: " + javaPath);
            e.printStackTrace();  }


    }
    private void updateArtifactIdInPom(String projectName, Document document) {
        NodeList artifactNodes = document.getElementsByTagName("artifactId");
        if (artifactNodes.getLength() > 0) {
            artifactNodes.item(1).setTextContent(projectName);
        }

    }
    private void saveXmlDocument(Document document, Path pomPath) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(pomPath.toFile());
        transformer.transform(source, result);
    }
    private Document loadXmlDocument(Path filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(filePath.toFile());
        document.getDocumentElement().normalize();
        return document;
    }


}


