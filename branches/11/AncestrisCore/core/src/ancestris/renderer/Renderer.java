/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2013 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.renderer;

import java.io.IOException;
import java.io.Writer;
import org.openide.util.Exceptions;

/**
 *
 * @author daniel
 */
public interface Renderer {
    
    public void put (String key, Object object);
    public void render(String template, Writer output);
    
    public static class Lookup {

        public static Renderer lookup(String template) {
            //FIXME: we should maintain a template cache and find appropriate provider given a template
            // From AncestrisEditor:
            //public static AncestrisEditor findEditor(Property property) {
            //    AncestrisEditor editor = NoOpEditor.instance;
            //    if (property == null) {
            //        return editor;
            //    }
            //    for (AncestrisEditor edt : Lookup.getDefault().lookupAll(AncestrisEditor.class)) {
            //        if (edt.canEdit(property)) {
            //            if (edt.isActive()) {
            //                return edt;
            //            }
            //            editor = edt;
            //        }
            //    }
            //    return editor;
            //}
            
            Renderer renderer = NoOpRenderer.instance;
            for (Renderer r: org.openide.util.Lookup.getDefault().lookupAll(Renderer.class)){
                return r;
            }
            return renderer;
        }
    }
    static class NoOpRenderer implements Renderer{
        public static final Renderer instance = new NoOpRenderer();

        public void put(String key, Object object) {
        }

        public void render(String template, Writer output) {
            try {
                output.write("<html><body><center><b>Not template renderer found!</b><br/>"
                        + "Please install <b>'Apache Velocity Renderer'!</b></center></body></html>");
            } catch (IOException ex) {
            }
        }
        
    }
}
