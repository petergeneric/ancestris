/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.views.graph;

import genj.util.Registry;
import java.util.Formatter;

/**
 * Parameters class for Graph Module.
 *
 * @author Zurga
 */
public class GraphParameter {

    private static final String CSS = "node.sosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "node.mariage {"
            + "    fill-color:%s;"
            + "}"
            + "node.mariagesosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "edge.sosa {"
            + "    fill-color:%s;"
            + "    size: %s;"
            + "}"
            + "edge.mariage {"
            + "    fill-color:%s;"
            + "}"
            + "edge.asso {"
            + "    fill-color:%s; "
            + "}"
            + "node.cujus {"
            + "    fill-color:%s;"
            + "	   size: %s;"
            + "}"
            + "edge {"
            + "    size: %s;"
            + "    fill-color:%s;"
            + "}"
            + "node {"
            + "	   size: %s;"
            + "    fill-color:%s;"
            + "}"
            + "node.sticked {"
            + "	fill-color:%s;"
            + "}"
            + "edge.sticked {"
            + "	fill-color:%s;"
            + "}"
            + "graph {"
            + "	fill-color:%s;"
            + "}";
    
     private static final String GEN_SCHEME = "node.sosa {"
            + "    fill-color:lightgray, %s, black;"
            + "    fill-mode: dyn-plain;"
            + "}"
            + "node {"
            + "	   fill-mode: dyn-plain;"
            + "    fill-color:lightgray, %s, black;"
            + "}"
            + "node.sticked {"
           + "	   fill-mode: dyn-plain;"
            + "    fill-color:lightgray, %s, black;"
            + "}";

    // Display Labels ?
    private boolean showLabel = false;
    // Auto Layoyt ?
    private boolean autoDisplay = true;
    // Center on click ?
    private boolean centerGraph = false;
    // Cacher sur click
    private boolean hideNodes = false;
    // Display associaiton link ?
    private boolean showAsso = false;
    // Search for shortest Path ?
    private boolean doPath = false;

    // Colors and sizes
    private String colorDef = "#000000";
    private String colorSosa = "#006400";
    private String colorMariage = "#FF4500";
    private String colorAsso = "#708090";
    private String colorCujus = "#FF00FF";
    private String colorSticked = "#0000FF";
    private String colorBack = "#FFFFFF";
    private String colorMariageSosa = "#FFCC33";
    private String sizeEdge = "2";
    private String sizeNode = "8";
    private String sizeCujus = "20";
    private String sizeNodeSosa = "8";
    private String sizeEdgeSosa = "2";
    private double indiNodeWeight = 10.0;
    private double mariageNodeWeight = 5.0;
    private double edgeWeight = 1.0;
    private boolean useGenerationScheme = false;

    private LabelFamEnum labelFam = LabelFamEnum.FAM_DATE;
    private LabelIndiEnum labelIndi = LabelIndiEnum.INDI_NAME;

    public String getCss() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format(CSS, colorSosa, sizeNodeSosa, colorMariage, colorMariageSosa, sizeNodeSosa, colorSosa,
                sizeEdgeSosa, colorMariage, colorAsso, colorCujus, sizeCujus,
                sizeEdge, colorDef, sizeNode, colorDef, colorSticked, colorSticked, colorBack);
        return sb.toString();
    }
    
    public String getGenerationScheme() {
        StringBuilder sb = new StringBuilder();
        Formatter fmt = new Formatter(sb);
        fmt.format(GEN_SCHEME, colorSticked, colorSticked, colorSticked);
        return sb.toString();
    }

    public void loadSettings(Registry registry) {
        colorDef = registry.get("GRAPH.color.default", "#000000");
        colorSosa = registry.get("GRAPH.color.sosa", "#006400");
        colorMariage = registry.get("GRAPH.color.marriage", "#FF4500");
        colorAsso = registry.get("GRAPH.color.asso", "#708090");
        colorCujus = registry.get("GRAPH.color.cujus", "#FF00FF");
        colorSticked = registry.get("GRAPH.color.sticked", "#0000FF");
        colorBack = registry.get("GRAPH.color.back", "#FFFFFF");
        colorMariageSosa = registry.get("GRAPH.color.marriage.sosa", "#FFCC33");
        sizeEdge = registry.get("GRAPH.size.edge", "2");
        sizeNode = registry.get("GRAPH.size.node", "8");
        sizeCujus = registry.get("GRAPH.size.cujus", "20");
        sizeNodeSosa = registry.get("GRAPH.size.node.sosa", "8");
        sizeEdgeSosa = registry.get("GRAPH.size.edge.sosa", "2");
        indiNodeWeight = Double.valueOf(registry.get("GRAPH.weight.node.indi", "10.0"));
        mariageNodeWeight = Double.valueOf(registry.get("GRAPH.weight.node.fam", "5.0"));
        edgeWeight = Double.valueOf(registry.get("GRAPH.weight.edge", "1.0"));
        labelFam = LabelFamEnum.valueOf(registry.get("GRAPH.fam.labels", "FAM_DATE"));
        labelIndi = LabelIndiEnum.valueOf(registry.get("GRAPH.indi.labels", "INDI_NAME"));
        useGenerationScheme = Boolean.valueOf(registry.get("GRAPH.color.scheme", "false"));
    }

    public void saveSettings(Registry registry) {
        registry.put("GRAPH.color.default", colorDef);
        registry.put("GRAPH.color.sosa", colorSosa);
        registry.put("GRAPH.color.marriage", colorMariage);
        registry.put("GRAPH.color.child", colorAsso);
        registry.put("GRAPH.color.cujus", colorCujus);
        registry.put("GRAPH.color.sticked", colorSticked);
        registry.put("GRAPH.color.back", colorBack);
        registry.put("GRAPH.color.marriage.sosa", colorMariageSosa);
        registry.put("GRAPH.size.edge", sizeEdge);
        registry.put("GRAPH.size.node", sizeNode);
        registry.put("GRAPH.size.cujus", sizeCujus);
        registry.put("GRAPH.size.node.sosa", sizeNodeSosa);
        registry.put("GRAPH.size.edge.sosa", sizeEdgeSosa);
        registry.put("GRAPH.weight.node.indi", String.valueOf(indiNodeWeight));
        registry.put("GRAPH.weight.node.fam", String.valueOf(mariageNodeWeight));
        registry.put("GRAPH.weight.edge", String.valueOf(edgeWeight));
        registry.put("GRAPH.fam.labels", labelFam.name());
        registry.put("GRAPH.indi.labels", labelIndi.name());
        registry.put("GRAPH.color.scheme", useGenerationScheme);
    }

    // Getters / Setters
    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
    }

    public boolean isAutoDisplay() {
        return autoDisplay;
    }

    public void setAutoDisplay(boolean autoDisplay) {
        this.autoDisplay = autoDisplay;
    }

    public boolean isCenterGraph() {
        return centerGraph;
    }

    public void setCenterGraph(boolean centerGraph) {
        this.centerGraph = centerGraph;
    }

    public String getColorDef() {
        return colorDef;
    }

    public void setColorDef(String colorDef) {
        this.colorDef = colorDef;
    }

    public String getColorSosa() {
        return colorSosa;
    }

    public void setColorSosa(String colorSosa) {
        this.colorSosa = colorSosa;
    }

    public String getColorMariage() {
        return colorMariage;
    }

    public void setColorMariage(String colorMariage) {
        this.colorMariage = colorMariage;
    }

    public String getColorAsso() {
        return colorAsso;
    }

    public void setColorAsso(String colorAsso) {
        this.colorAsso = colorAsso;
    }

    public String getColorCujus() {
        return colorCujus;
    }

    public void setColorCujus(String colorCujus) {
        this.colorCujus = colorCujus;
    }

    public String getColorSticked() {
        return colorSticked;
    }

    public void setColorSticked(String colorSticked) {
        this.colorSticked = colorSticked;
    }

    public String getColorBack() {
        return colorBack;
    }

    public void setColorBack(String colorBack) {
        this.colorBack = colorBack;
    }

    public String getColorMariageSosa() {
        return colorMariageSosa;
    }

    public void setColorMariageSosa(String colorMariageSosa) {
        this.colorMariageSosa = colorMariageSosa;
    }

    public String getSizeEdge() {
        return sizeEdge;
    }

    public void setSizeEdge(String sizeEdge) {
        this.sizeEdge = sizeEdge;
    }

    public String getSizeNode() {
        return sizeNode;
    }

    public void setSizeNode(String sizeNode) {
        this.sizeNode = sizeNode;
    }

    public String getSizeCujus() {
        return sizeCujus;
    }

    public void setSizeCujus(String sizeCujus) {
        this.sizeCujus = sizeCujus;
    }

    public String getSizeNodeSosa() {
        return sizeNodeSosa;
    }

    public void setSizeNodeSosa(String sizeNodeSosa) {
        this.sizeNodeSosa = sizeNodeSosa;
    }

    public String getSizeEdgeSosa() {
        return sizeEdgeSosa;
    }

    public void setSizeEdgeSosa(String sizeEdgeSosa) {
        this.sizeEdgeSosa = sizeEdgeSosa;
    }

    public double getIndiNodeWeight() {
        return indiNodeWeight;
    }

    public void setIndiNodeWeight(double indiNodeWeight) {
        this.indiNodeWeight = indiNodeWeight;
    }

    public double getMariageNodeWeight() {
        return mariageNodeWeight;
    }

    public void setMariageNodeWeight(double mariageNodeWeight) {
        this.mariageNodeWeight = mariageNodeWeight;
    }

    public double getEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(double edgeWeight) {
        this.edgeWeight = edgeWeight;
    }

    public LabelFamEnum getLabelFam() {
        return labelFam;
    }

    public void setLabelFam(LabelFamEnum labelFam) {
        this.labelFam = labelFam;
    }

    public LabelIndiEnum getLabelIndi() {
        return labelIndi;
    }

    public void setLabelIndi(LabelIndiEnum labelIndi) {
        this.labelIndi = labelIndi;
    }

    public boolean isHideNodes() {
        return hideNodes;
    }

    public void setHideNodes(boolean hideNodes) {
        this.hideNodes = hideNodes;
    }

    public boolean isShowAsso() {
        return showAsso;
    }

    public void setShowAsso(boolean showAsso) {
        this.showAsso = showAsso;
    }

    public boolean isDoPath() {
        return doPath;
    }

    public void setDoPath(boolean doPath) {
        this.doPath = doPath;
    }

    public boolean isUseGenerationScheme() {
        return useGenerationScheme;
    }

    public void setUseGenerationScheme(boolean useGenerationScheme) {
        this.useGenerationScheme = useGenerationScheme;
    }

}
