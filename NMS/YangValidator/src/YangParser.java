import model.YangModule;
import model.YangNode;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.List;

public class YangParser {
    private static final Pattern MODULE_PATTERN = Pattern.compile("^\\s*module\\s+(\\S+)\\s*\\{");
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("^\\s*namespace\\s+\"([^\"]+)\"\\s*;");
    private static final Pattern PREFIX_PATTERN = Pattern.compile("^\\s*prefix\\s+(\\S+)\\s*;");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+(\\S+)\\s*\\{");
    private static final Pattern CONTAINER_PATTERN = Pattern.compile("^\\s*container\\s+(\\S+)\\s*\\{");
    private static final Pattern LEAF_PATTERN = Pattern.compile("^\\s*leaf\\s+(\\S+)\\s*\\{");
    private static final Pattern LEAF_LIST_PATTERN = Pattern.compile("^\\s*leaf-list\\s+(\\S+)\\s*\\{");
    private static final Pattern LIST_PATTERN = Pattern.compile("^\\s*list\\s+(\\S+)\\s*\\{");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*type\\s+(\\S+)\\s*;");
    private static final Pattern MANDATORY_PATTERN = Pattern.compile("^\\s*mandatory\\s+(true|false)\\s*;");
    private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^\\s*description\\s+\"([^\"]+)\"\\s*;");

    public YangModule parseYangFile(String filePath) throws IOException {
        YangModule module = null;
        Stack<YangNode> nodeStack = new Stack<>();
        Stack<String> braceStack = new Stack<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                
                // Parse module declaration
                if (module == null) {
                    Matcher moduleMatcher = MODULE_PATTERN.matcher(line);
                    if (moduleMatcher.find()) {
                        module = new YangModule(moduleMatcher.group(1));
                        braceStack.push("module");
                        continue;
                    }
                }
                
                if (module == null) continue;
                
                // Parse namespace
                Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(line);
                if (namespaceMatcher.find()) {
                    module.setNamespace(namespaceMatcher.group(1));
                    continue;
                }
                
                // Parse prefix
                Matcher prefixMatcher = PREFIX_PATTERN.matcher(line);
                if (prefixMatcher.find()) {
                    module.setPrefix(prefixMatcher.group(1));
                    continue;
                }
                
                // Parse imports
                Matcher importMatcher = IMPORT_PATTERN.matcher(line);
                if (importMatcher.find()) {
                    module.addImport(importMatcher.group(1));
                    braceStack.push("import");
                    continue;
                }
                
                // Parse container
                Matcher containerMatcher = CONTAINER_PATTERN.matcher(line);
                if (containerMatcher.find()) {
                    YangNode container = new YangNode(containerMatcher.group(1), "container");
                    if (nodeStack.isEmpty()) {
                        module.addNode(container);
                    } else {
                        nodeStack.peek().addChild(container);
                    }
                    nodeStack.push(container);
                    braceStack.push("container");
                    continue;
                }
                
                // Parse leaf
                Matcher leafMatcher = LEAF_PATTERN.matcher(line);
                if (leafMatcher.find()) {
                    YangNode leaf = new YangNode(leafMatcher.group(1), "leaf");
                    if (!nodeStack.isEmpty()) {
                        nodeStack.peek().addChild(leaf);
                    } else {
                        module.addNode(leaf);
                    }
                    nodeStack.push(leaf);
                    braceStack.push("leaf");
                    continue;
                }
                
                // Parse leaf-list
                Matcher leafListMatcher = LEAF_LIST_PATTERN.matcher(line);
                if (leafListMatcher.find()) {
                    YangNode leafList = new YangNode(leafListMatcher.group(1), "leaf-list");
                    if (!nodeStack.isEmpty()) {
                        nodeStack.peek().addChild(leafList);
                    } else {
                        module.addNode(leafList);
                    }
                    nodeStack.push(leafList);
                    braceStack.push("leaf-list");
                    continue;
                }
                
                // Parse list
                Matcher listMatcher = LIST_PATTERN.matcher(line);
                if (listMatcher.find()) {
                    YangNode list = new YangNode(listMatcher.group(1), "list");
                    if (!nodeStack.isEmpty()) {
                        nodeStack.peek().addChild(list);
                    } else {
                        module.addNode(list);
                    }
                    nodeStack.push(list);
                    braceStack.push("list");
                    continue;
                }
                
                // Parse type for current node
                if (!nodeStack.isEmpty()) {
                    Matcher typeMatcher = TYPE_PATTERN.matcher(line);
                    if (typeMatcher.find()) {
                        nodeStack.peek().setDataType(typeMatcher.group(1));
                        continue;
                    }
                    
                    // Parse mandatory
                    Matcher mandatoryMatcher = MANDATORY_PATTERN.matcher(line);
                    if (mandatoryMatcher.find()) {
                        nodeStack.peek().setMandatory("true".equals(mandatoryMatcher.group(1)));
                        continue;
                    }
                    
                    // Parse description
                    Matcher descMatcher = DESCRIPTION_PATTERN.matcher(line);
                    if (descMatcher.find()) {
                        nodeStack.peek().setDescription(descMatcher.group(1));
                        continue;
                    }
                }
                
                // Handle closing braces
                if (line.equals("}")) {
                    if (!braceStack.isEmpty()) {
                        String lastBrace = braceStack.pop();
                        if (!nodeStack.isEmpty() && 
                            ("container".equals(lastBrace) || "leaf".equals(lastBrace) || 
                             "leaf-list".equals(lastBrace) || "list".equals(lastBrace))) {
                            nodeStack.pop();
                        }
                    }
                }
            }
        }
        
        return module;
    }
    
    public void validateSyntax(String filePath) throws IOException {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int braceCount = 0;
            int lineNumber = 0;
            boolean inModule = false;
            boolean inQuotes = false;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String originalLine = line;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                
                // Check for unclosed quotes
                char[] chars = originalLine.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (chars[i] == '"' && (i == 0 || chars[i-1] != '\\')) {
                        inQuotes = !inQuotes;
                    }
                }
                
                // Count braces for basic syntax validation
                for (char c : line.toCharArray()) {
                    if (c == '{') braceCount++;
                    if (c == '}') braceCount--;
                }
                
                // Check for module declaration
                if (MODULE_PATTERN.matcher(line).find()) {
                    inModule = true;
                }
                
                // Enhanced syntax checks
                
                // Check for missing semicolons after statements (when not followed by brace)
                if (line.contains("namespace") && !line.endsWith(";") && !line.contains("{")) {
                    errors.add("Line " + lineNumber + ": Missing semicolon after namespace declaration");
                }
                
                if (line.contains("prefix") && !line.endsWith(";") && !line.contains("{")) {
                    errors.add("Line " + lineNumber + ": Missing semicolon after prefix declaration");
                }
                
                if (line.contains("description") && line.contains("\"") && !line.endsWith(";") && !line.contains("{")) {
                    errors.add("Line " + lineNumber + ": Missing semicolon after description");
                }
                
                if (line.contains("type") && !line.endsWith(";") && !line.contains("{")) {
                    errors.add("Line " + lineNumber + ": Missing semicolon after type declaration");
                }
                
                // Check for unquoted descriptions
                if (line.contains("description") && !line.contains("\"")) {
                    warnings.add("Line " + lineNumber + ": Description might be missing quotes");
                }
                
                // Check for double semicolons
                if (line.contains(";;")) {
                    warnings.add("Line " + lineNumber + ": Double semicolon detected");
                }
                
                // Basic syntax checks
                if (line.contains(";") && !line.endsWith(";") && !line.endsWith(";;") && !line.contains("{")) {
                    warnings.add("Line " + lineNumber + ": Semicolon might be misplaced");
                }
            }
            
            // Final validation checks
            if (!inModule) {
                errors.add("No module declaration found in the file");
            }
            
            if (braceCount != 0) {
                errors.add("Unbalanced braces in YANG file - " + 
                          (braceCount > 0 ? braceCount + " more opening brace(s)" : Math.abs(braceCount) + " more closing brace(s)"));
            }
            
            if (inQuotes) {
                errors.add("Unclosed quotes in the file");
            }
            
            // Print results
            if (warnings.size() > 0) {
                System.out.println("Warnings:");
                for (String warning : warnings) {
                    System.out.println("  ⚠ " + warning);
                }
            }
            
            if (errors.size() > 0) {
                System.out.println("Errors found:");
                for (String error : errors) {
                    System.out.println("  ✗ " + error);
                }
                throw new IOException("Validation failed with " + errors.size() + " error(s)");
            }
            
            System.out.println("✓ Basic syntax validation passed");
            System.out.println("✓ YANG file syntax is valid");
            
        } catch (Exception e) {
            if (!e.getMessage().contains("Validation failed")) {
                System.out.println("✗ Validation failed: " + e.getMessage());
            }
            throw e;
        }
    }
}