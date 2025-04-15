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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class FilesEditorService {

    // public methods


            // update application.properties & pom.xml
            public  void updateApplicationProperties(String projectName, Map<String , String> newProperties) throws IOException {
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


            public void updateProjectNameArtifactAndDescriptionInXml(String projectName) throws Exception {
                Path pomPath = Paths.get(projectName, "pom.xml");

                if (!Files.exists(pomPath)) {
                    System.out.println("error ! pom.xml not found at: " + pomPath);
                    return;
                }

                // load document
                Document document = loadXmlDocument(pomPath);

                // update artifactID for project
                updateArtifactIdInPom(projectName, document);
                NodeList nameNodes = document.getElementsByTagName("name");
                NodeList descriptionNodes = document.getElementsByTagName("description");
                // update name tag
                if (nameNodes.getLength() > 0 ) {
                    nameNodes.item(0).setTextContent(projectName);
                    descriptionNodes.item(0).setTextContent(projectName + "SaaS project");

                } else {
                    System.out.println("Error name , artifactId or description tag not found");
                    return;
                }






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

            String oldPackage = "nilsw13";
            String oldProjectName = "springreact";

            // change groupId declaration in Pom.xml
            updateGroupIdInPom(projectName, packageName);

            // change package declaration in java files
            updateJavaPackageDeclarations(projectName, packageName, oldPackage);

            // change structure directory name
            updatePackageDirectoryStructure(projectName, oldPackage, packageName);


            System.out.println("Package successfully updated from " + oldPackage + " to " + packageName);

        }






    // private methods



    private void updatePackageDirectoryStructure(String projectName, String oldPackage, String packageName) {
                // change package name in folder structure
    }

    private void updateJavaPackageDeclarations(String projectName, String packageName, String oldPackage) {
                // change package declaration in .java files
    }

    private void updateArtifactIdInPom(String projectName, Document document) {
        NodeList artifactNodes = document.getElementsByTagName("artifactId");
        if (artifactNodes.getLength() > 0) {
            artifactNodes.item(1).setTextContent(projectName);
        }

    }
    private void updateGroupIdInPom(String projectName, String packageName) {
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


