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

/**
 * Store information useful to recreate edge after hide them.
 * @author Zurga
 */
public class HideEdge {
    
    private final String id;
    
   private String classOrigine;
   private String classe;
   private String nodeInitial;
   private String nodeFinal;
   private boolean directed;
    
    public HideEdge(String id) {
        this.id = id;
    }

    public String getClassOrigine() {
        return classOrigine;
    }

    public void setClassOrigine(String classOrigine) {
        this.classOrigine = classOrigine;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getNodeInitial() {
        return nodeInitial;
    }

    public void setNodeInitial(String nodeInitial) {
        this.nodeInitial = nodeInitial;
    }

    public String getNodeFinal() {
        return nodeFinal;
    }

    public void setNodeFinal(String nodFinal) {
        this.nodeFinal = nodFinal;
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    public String getId() {
        return id;
    }
    
}
