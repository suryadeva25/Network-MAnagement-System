package model;

import java.util.ArrayList;
import java.util.List;

public class YangNode {
    private String name;
    private String type;
    private String description;
    private List<YangNode> children;
    private boolean isMandatory;
    private String dataType;

    public YangNode(String name, String type) {
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<YangNode> getChildren() { return children; }
    public void addChild(YangNode child) { this.children.add(child); }

    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { isMandatory = mandatory; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }

    @Override
    public String toString() {
        return "YangNode{name='" + name + "', type='" + type + "', children=" + children.size() + "}";
    }
}