import model.YangModule;
import model.YangNode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class JsonConverter {
    
    public JSONObject convertToJson(YangModule module) {
        try {
            JSONObject json = new JSONObject();
            
            // Module metadata
            json.put("module", module.getName());
            if (module.getNamespace() != null) {
                json.put("namespace", module.getNamespace());
            }
            if (module.getPrefix() != null) {
                json.put("prefix", module.getPrefix());
            }
            
            // Imports
            if (!module.getImports().isEmpty()) {
                JSONArray importsArray = new JSONArray();
                for (String imp : module.getImports()) {
                    importsArray.put(imp);
                }
                json.put("imports", importsArray);
            }
            
            // Nodes
            JSONArray nodesArray = new JSONArray();
            for (YangNode node : module.getNodes()) {
                nodesArray.put(convertNodeToJson(node));
            }
            json.put("nodes", nodesArray);
            
            return json;
            
        } catch (Exception e) {
            throw new RuntimeException("Error converting to JSON: " + e.getMessage(), e);
        }
    }
    
    private JSONObject convertNodeToJson(YangNode node) {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put("name", node.getName());
        nodeJson.put("type", node.getType());
        
        if (node.getDescription() != null && !node.getDescription().isEmpty()) {
            nodeJson.put("description", node.getDescription());
        }
        
        if (node.getDataType() != null && !node.getDataType().isEmpty()) {
            nodeJson.put("data-type", node.getDataType());
        }
        
        nodeJson.put("mandatory", node.isMandatory());
        
        // Children
        if (!node.getChildren().isEmpty()) {
            JSONArray childrenArray = new JSONArray();
            for (YangNode child : node.getChildren()) {
                childrenArray.put(convertNodeToJson(child));
            }
            nodeJson.put("children", childrenArray);
        }
        
        return nodeJson;
    }
    
    public void saveJsonToFile(JSONObject json, String outputPath) throws IOException {
        // Ensure output directory exists
        File outputDir = new File(outputPath).getParentFile();
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(json.toString(4)); // 4 spaces for indentation
            file.flush();
        }
    }
    
    public String convertToJsonString(YangModule module) {
        return convertToJson(module).toString(4);
    }
}