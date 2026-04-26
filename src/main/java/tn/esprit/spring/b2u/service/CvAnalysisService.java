package tn.esprit.spring.b2u.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class CvAnalysisService {

    public List<String> extractSkillsFromBytes(byte[] pdfBytes) {
        System.out.println("========== CV ANALYSIS START ==========");
        System.out.println("📦 PDF size: " + pdfBytes.length + " bytes");

        try {
            String extractedText = extractTextFromBytes(pdfBytes);

            System.out.println("📄 Texte complet du CV:");
            System.out.println("----------------------------------------");
            System.out.println(extractedText);
            System.out.println("----------------------------------------");

            if (extractedText == null || extractedText.trim().isEmpty()) {
                System.out.println("⚠️ TEXT IS EMPTY AFTER PDF EXTRACTION");
                return getDefaultSkills();
            }

            List<String> skills = extractSkillsFromText(extractedText);

            System.out.println("🎯 Final skills extracted: " + skills);
            System.out.println("========== CV ANALYSIS END ==========");

            return skills;

        } catch (Exception e) {
            System.out.println("❌ CV API ERROR OCCURRED");
            System.out.println("📍 Message: " + e.getMessage());
            e.printStackTrace();
            return getDefaultSkills();
        }
    }

    private String extractTextFromBytes(byte[] pdfBytes) throws IOException {
        System.out.println("📥 Extracting text from PDF bytes...");

        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = PDDocument.load(inputStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text != null && !text.trim().isEmpty()) {
                System.out.println("✅ PDF extracted successfully");
                System.out.println("📝 Text length: " + text.length());
                System.out.println("📝 First 200 chars: " + text.substring(0, Math.min(200, text.length())));
            } else {
                System.out.println("⚠️ PDF extracted but text is empty");
            }

            return text;
        } catch (Exception e) {
            System.out.println("❌ PDF extraction failed: " + e.getMessage());
            throw new IOException("Failed to extract PDF text", e);
        }
    }

    private List<String> extractSkillsFromText(String text) {
        Set<String> foundSkills = new HashSet<>();
        String lowerText = text.toLowerCase();

        Map<String, List<String>> skillCategories = new HashMap<>();

        // ===== SOFTWARE ENGINEERING SKILLS =====

        // Langages de programmation
        skillCategories.put("programming_languages", Arrays.asList(
                "java", "python", "javascript", "typescript", "c++", "c#", "php", "ruby",
                "go", "swift", "kotlin", "rust", "scala", "groovy", "perl", "r", "matlab"
        ));

        // Frameworks Backend
        skillCategories.put("backend_frameworks", Arrays.asList(
                "spring", "spring boot", "spring mvc", "spring security", "hibernate", "jpa",
                "jakarta ee", "java ee", "node.js", "express", "django", "flask", "fastapi",
                "laravel", "symfony", "ruby on rails", "asp.net", ".net core", "gin", "echo"
        ));

        // Frameworks Frontend
        skillCategories.put("frontend_frameworks", Arrays.asList(
                "react", "react js", "next.js", "angular", "angular js", "vue", "vue.js",
                "nuxt.js", "svelte", "jquery", "bootstrap", "tailwind css", "material ui",
                "ant design", "semantic ui", "bulma", "foundation"
        ));

        // Bases de données
        skillCategories.put("databases", Arrays.asList(
                "mysql", "postgresql", "mongodb", "oracle", "sql server", "sqlite",
                "mariadb", "cassandra", "redis", "elasticsearch", "dynamodb", "firebase",
                "realm", "couchdb", "neo4j", "influxdb", "timescaledb"
        ));

        // Cloud & DevOps
        skillCategories.put("cloud_devops", Arrays.asList(
                "aws", "amazon web services", "azure", "microsoft azure", "gcp", "google cloud",
                "docker", "kubernetes", "jenkins", "gitlab ci", "github actions", "circleci",
                "travis ci", "terraform", "ansible", "puppet", "chef", "prometheus", "grafana",
                "elk", "splunk", "cloudformation", "helm"
        ));

        // Architecture & Design Patterns
        skillCategories.put("architecture", Arrays.asList(
                "microservices", "monolithic", "serverless", "event driven", "cqrs", "event sourcing",
                "hexagonal architecture", "clean architecture", "onion architecture", "ddd",
                "domain driven design", "tdd", "test driven development", "bdd", "behaviour driven",
                "solid principles", "design patterns", "oop", "object oriented", "functional programming"
        ));

        // Tests & Qualité
        skillCategories.put("testing", Arrays.asList(
                "junit", "testng", "mockito", "powermock", "assertj", "selenium", "cypress",
                "playwright", "puppeteer", "jest", "mocha", "chai", "karma", "protractor",
                "postman", "newman", "soapui", "jmeter", "gatling", "sonarqube", "jacoco"
        ));

        // APIs & Intégration
        skillCategories.put("apis", Arrays.asList(
                "rest", "restful", "rest api", "graphql", "soap", "grpc", "websocket",
                "socket.io", "openapi", "swagger", "postman", "apache camel", "spring integration"
        ));

        // Version Control & Collaboration
        skillCategories.put("version_control", Arrays.asList(
                "git", "github", "gitlab", "bitbucket", "svn", "mercurial", "perforce",
                "jira", "confluence", "trello", "slack", "teams", "discord"
        ));

        // Méthodologies Agiles
        skillCategories.put("agile", Arrays.asList(
                "agile", "scrum", "kanban", "sprint", "jira", "trello", "agile methodology",
                "scrum master", "product owner", "extreme programming", "xp", "lean"
        ));

        // IDE & Outils
        skillCategories.put("ide_tools", Arrays.asList(
                "intellij idea", "eclipse", "visual studio", "vs code", "netbeans", "android studio",
                "pycharm", "webstorm", "phpstorm", "sublime text", "atom", "vim", "emacs"
        ));

        // Build Tools
        skillCategories.put("build_tools", Arrays.asList(
                "maven", "gradle", "ant", "npm", "yarn", "webpack", "vite", "babel",
                "gulp", "grunt", "rollup", "parcel", "make", "cmake"
        ));

        // ===== EMBEDDED & IoT SKILLS =====

        // IoT Frameworks
        skillCategories.put("iot_frameworks", Arrays.asList(
                "arduino", "raspberry pi", "esp32", "esp8266", "nodemcu", "mbed",
                "zephyr", "freertos", "threadx", "embos", "riot", "contiki"
        ));

        // IoT Platforms
        skillCategories.put("iot_platforms", Arrays.asList(
                "aws iot", "azure iot", "google cloud iot", "thingsboard", "blynk",
                "particle", "tuya", "ibm watson iot", "oracle iot"
        ));

        // Communication Protocols
        skillCategories.put("communication_protocols", Arrays.asList(
                "mqtt", "coap", "http", "https", "websocket", "amqp", "zigbee",
                "z-wave", "ble", "bluetooth low energy", "lora", "lorawan",
                "nb-iot", "sigfox", "wifi", "ethernet", "can bus", "modbus",
                "i2c", "spi", "uart", "usb", "rs232", "rs485"
        ));

        // Microcontrollers
        skillCategories.put("microcontrollers", Arrays.asList(
                "arduino uno", "arduino mega", "esp32", "esp8266", "stm32",
                "nrf52", "nrf52840", "atmega328", "atmega2560", "arm cortex-m",
                "raspberry pi pico", "teensy", "beaglebone", "nucleo"
        ));

        // Embedded OS
        skillCategories.put("embedded_os", Arrays.asList(
                "freertos", "rtos", "zephyr", "embos", "threadx", "mbed os",
                "linux embedded", "yocto", "buildroot", "ubuntu core"
        ));

        // Electronics & Hardware
        skillCategories.put("electronics", Arrays.asList(
                "circuit design", "pcb design", "electronics", "soldering", "oscilloscope",
                "multimeter", "logic analyzer", "signal processing", "fpga", "verilog", "vhdl"
        ));

        // ===== GENERAL SKILLS =====
        skillCategories.put("general", Arrays.asList(
                "iot", "internet of things", "embedded systems", "mobile development",
                "system design", "full stack", "backend developer", "frontend developer",
                "devops engineer", "software architect", "tech lead", "scrum master"
        ));

        // Scanner toutes les catégories
        for (Map.Entry<String, List<String>> category : skillCategories.entrySet()) {
            for (String skill : category.getValue()) {
                String lowerSkill = skill.toLowerCase();
                if (lowerText.contains(lowerSkill)) {
                    foundSkills.add(capitalizeSkill(skill));
                    System.out.println("🔍 Found skill: " + skill);
                }
            }
        }

        // Si aucune compétence trouvée, chercher dans les sections spécifiques
        if (foundSkills.isEmpty()) {
            System.out.println("⚠️ No skills found in standard lists, looking for COMPETENCES section...");

            String[] sectionMarkers = {
                    "compétences", "competences", "skills", "technical skills",
                    "technologies", "outils", "tools", "langages", "frameworks",
                    "soft skills", "competencies"
            };

            for (String marker : sectionMarkers) {
                int index = lowerText.indexOf(marker);
                if (index != -1) {
                    int start = index + marker.length();
                    int end = Math.min(start + 1000, lowerText.length());
                    String section = lowerText.substring(start, end);

                    int nextMarker = findNextMarker(section, sectionMarkers);
                    if (nextMarker != -1) {
                        section = section.substring(0, nextMarker);
                    }

                    String[] possibleSkills = section.split("[,;•\n\r•●○■▪✓✅]");
                    for (String possible : possibleSkills) {
                        possible = possible.trim();
                        if (possible.length() > 2 && possible.length() < 50 &&
                                !possible.contains("http") && !possible.contains("www")) {
                            foundSkills.add(capitalizeSkill(possible));
                            System.out.println("🔍 Found from section: " + possible);
                        }
                    }
                    break;
                }
            }
        }

        // Convertir Set en List et limiter à 15 compétences max (augmenté pour software engineering)
        List<String> result = new ArrayList<>(foundSkills);
        if (result.size() > 15) {
            result = result.subList(0, 15);
        }

        // Trier par ordre alphabétique pour meilleure lisibilité
        Collections.sort(result);

        // Si aucune compétence trouvée, retourner les compétences par défaut
        if (result.isEmpty()) {
            System.out.println("⚠️ No skills extracted, using default software engineering skills");
            return getDefaultSoftwareEngineeringSkills();
        }

        System.out.println("✅ Extracted " + result.size() + " unique skills");
        return result;
    }

    private int findNextMarker(String text, String[] markers) {
        int minIndex = Integer.MAX_VALUE;
        for (String marker : markers) {
            int index = text.indexOf(marker);
            if (index != -1 && index < minIndex) {
                minIndex = index;
            }
        }
        return minIndex != Integer.MAX_VALUE ? minIndex : -1;
    }

    private String capitalizeSkill(String skill) {
        if (skill == null || skill.isEmpty()) return skill;

        String lowerSkill = skill.toLowerCase();

        // Cas spéciaux pour Software Engineering
        Map<String, String> specialCases = new HashMap<>();
        specialCases.put("c++", "C++");
        specialCases.put("c#", "C#");
        specialCases.put("freertos", "FreeRTOS");
        specialCases.put("mqtt", "MQTT");
        specialCases.put("coap", "CoAP");
        specialCases.put("esp32", "ESP32");
        specialCases.put("esp8266", "ESP8266");
        specialCases.put("stm32", "STM32");
        specialCases.put("aws iot", "AWS IoT");
        specialCases.put("azure iot", "Azure IoT");
        specialCases.put("raspberry pi", "Raspberry Pi");
        specialCases.put("arduino", "Arduino");
        specialCases.put("iot", "IoT");
        specialCases.put("rtos", "RTOS");
        specialCases.put("node.js", "Node.js");
        specialCases.put(".net", ".NET");
        specialCases.put("react js", "React.js");
        specialCases.put("vue.js", "Vue.js");
        specialCases.put("spring boot", "Spring Boot");
        specialCases.put("spring security", "Spring Security");

        if (specialCases.containsKey(lowerSkill)) {
            return specialCases.get(lowerSkill);
        }

        // Capitaliser normalement
        String[] words = skill.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                // Garder certains mots en majuscules
                if (word.equalsIgnoreCase("api") || word.equalsIgnoreCase("iot") ||
                        word.equalsIgnoreCase("ide") || word.equalsIgnoreCase("os")) {
                    result.append(word.toUpperCase()).append(" ");
                } else {
                    result.append(Character.toUpperCase(word.charAt(0)))
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }
        }
        return result.toString().trim();
    }

    private List<String> getDefaultSkills() {
        return Arrays.asList("Java", "Spring Boot", "Communication", "Problem Solving");
    }

    private List<String> getDefaultSoftwareEngineeringSkills() {
        return Arrays.asList(
                "Java", "Spring Boot", "Microservices", "REST API", "Git",
                "MongoDB", "Docker", "Agile", "JUnit", "Design Patterns"
        );
    }

    private List<String> getEmbeddedDefaultSkills() {
        return Arrays.asList(
                "C", "C++", "Python", "IoT", "Embedded Systems",
                "Arduino", "ESP32", "MQTT", "RTOS", "Microcontrollers"
        );
    }
}