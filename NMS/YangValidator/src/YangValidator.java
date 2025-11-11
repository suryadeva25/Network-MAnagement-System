import model.YangModule;
import model.YangNode;
import org.json.JSONObject;
import java.io.File;
import java.util.Scanner;
import java.util.List;

public class YangValidator {
    private YangParser parser;
    private JsonConverter converter;
    private Scanner scanner;
    
    public YangValidator() {
        this.parser = new YangParser();
        this.converter = new JsonConverter();
        this.scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("=== YANG Model Validator ===");
        System.out.println("Developed for local YANG file processing");
        
        while (true) {
            printMenu();
            String choice = scanner.nextLine();
            
            switch (choice) {
                case "1":
                    validateYangFile();
                    break;
                case "2":
                    displayYangStructure();
                    break;
                case "3":
                    convertToJson();
                    break;
                case "4":
                    System.out.println("Exiting YANG Validator. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void printMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Validate YANG file for syntax errors");
        System.out.println("2. Extract and display key nodes and relationships");
        System.out.println("3. Convert YANG to JSON");
        System.out.println("4. Exit");
        System.out.print("Enter your choice (1-4): ");
    }
    
    private void validateYangFile() {
        System.out.print("Enter YANG file path (or filename if in input/ folder): ");
        String filePath = scanner.nextLine();
        
        // If no path provided, use input folder
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = "input/" + filePath;
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: File not found - " + filePath);
                return;
            }
            
            System.out.println("\nValidating YANG file: " + filePath);
            parser.validateSyntax(filePath);
            System.out.println("✓ YANG file syntax is valid");
            
        } catch (Exception e) {
            System.out.println("✗ Validation failed: " + e.getMessage());
        }
    }
    
    private void displayYangStructure() {
        System.out.print("Enter YANG file path (or filename if in input/ folder): ");
        String filePath = scanner.nextLine();
        
        // If no path provided, use input folder
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = "input/" + filePath;
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: File not found - " + filePath);
                return;
            }
            
            System.out.println("\nParsing YANG file: " + filePath);
            YangModule module = parser.parseYangFile(filePath);
            
            displayModuleInfo(module);
            displayNodes(module.getNodes(), 0);
            
        } catch (Exception e) {
            System.out.println("✗ Error parsing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void displayModuleInfo(YangModule module) {
        System.out.println("\n=== Module Information ===");
        System.out.println("Module Name: " + module.getName());
        System.out.println("Namespace: " + (module.getNamespace() != null ? module.getNamespace() : "Not specified"));
        System.out.println("Prefix: " + (module.getPrefix() != null ? module.getPrefix() : "Not specified"));
        
        if (!module.getImports().isEmpty()) {
            System.out.println("Imports: " + String.join(", ", module.getImports()));
        }
        System.out.println();
    }
    
    private void displayNodes(List<YangNode> nodes, int indent) {
        String indentStr = "  ".repeat(indent);
        
        for (YangNode node : nodes) {
            System.out.printf("%s%s (%s)", indentStr, node.getName(), node.getType());
            
            if (node.getDataType() != null) {
                System.out.printf(" - type: %s", node.getDataType());
            }
            
            if (node.isMandatory()) {
                System.out.print(" [MANDATORY]");
            }
            
            System.out.println();
            
            if (node.getDescription() != null && !node.getDescription().isEmpty()) {
                System.out.printf("%s  Description: %s\n", indentStr, node.getDescription());
            }
            
            displayNodes(node.getChildren(), indent + 1);
        }
    }
    
    private void convertToJson() {
        System.out.print("Enter YANG file path (or filename if in input/ folder): ");
        String filePath = scanner.nextLine();
        
        // If no path provided, use input folder
        if (!filePath.contains("/") && !filePath.contains("\\")) {
            filePath = "input/" + filePath;
        }
        
        System.out.print("Enter output JSON filename (without path): ");
        String outputFile = scanner.nextLine();
        String outputPath = "output/" + outputFile;
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: File not found - " + filePath);
                return;
            }
            
            System.out.println("\nConverting YANG to JSON...");
            
            // Parse YANG file
            YangModule module = parser.parseYangFile(filePath);
            
            // Convert to JSON
            JSONObject json = converter.convertToJson(module);
            
            // Save to file
            converter.saveJsonToFile(json, outputPath);
            
            System.out.println("✓ Successfully converted to JSON");
            System.out.println("✓ Output saved to: " + outputPath);
            
            // Display a preview
            System.out.println("\n=== JSON Preview (first 500 chars) ===");
            String jsonString = json.toString(2);
            System.out.println(jsonString.substring(0, Math.min(jsonString.length(), 500)) + "...");
            
        } catch (Exception e) {
            System.out.println("✗ Conversion failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}