package model;

import java.util.ArrayList;
import java.util.List;

public class YangModule {
    private String name;
    private String namespace;
    private String prefix;
    private List<YangNode> nodes;
    private List<String> imports;

    public YangModule(String name) {
        this.name = name;
        this.nodes = new ArrayList<>();
        this.imports = new ArrayList<>();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public List<YangNode> getNodes() { return nodes; }
    public void addNode(YangNode node) { this.nodes.add(node); }

    public List<String> getImports() { return imports; }
    public void addImport(String importModule) { this.imports.add(importModule); }

    @Override
    public String toString() {
        return "YangModule{name='" + name + "', namespace='" + namespace + "', nodes=" + nodes.size() + "}";
    }
}