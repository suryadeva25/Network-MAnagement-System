import java.io.File;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== YANG Model Validator ===");
            System.out.println("Initializing application...");
            
            // Create necessary directories
            createDirectories();
            
            // Check for JSON library
            checkDependencies();
            
            // Start the validator
            YangValidator validator = new YangValidator();
            validator.start();
            
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            System.err.println("Please ensure all dependencies are available.");
            e.printStackTrace();
        }
    }
    
    private static void createDirectories() {
        File inputDir = new File("input");
        File outputDir = new File("output");
        
        if (!inputDir.exists()) {
            if (inputDir.mkdirs()) {
                System.out.println("✓ Created input directory for YANG files");
            }
        }
        
        if (!outputDir.exists()) {
            if (outputDir.mkdirs()) {
                System.out.println("✓ Created output directory for JSON files");
            }
        }
    }
    
    private static void checkDependencies() {
        try {
            // Try to load JSONObject to verify the library is available
            Class.forName("org.json.JSONObject");
            System.out.println("✓ JSON library loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ JSON library not found!");
            System.err.println("Please ensure json-20231013.jar is in the lib/ directory");
            System.err.println("Run download-dependencies.sh or download-dependencies.bat");
            System.exit(1);
        }
    }
}
