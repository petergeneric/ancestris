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
package ancestris.renderer.velocity;

import ancestris.renderer.Renderer;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.io.File;
import java.io.Writer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.SortTool;
import org.openide.modules.Places;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Renderer.class)
public class VelocityRenderer implements Renderer {

    private VelocityContext context;
    RuntimeInstance engine = new RuntimeInstance();
    private static final File TEMPLATE_DIR = Places.getUserDirectory();

    public VelocityRenderer() {
        try {
            engine.setProperty("resource.loader", "file,class");
            engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            engine.setProperty("class.resource.loader.cache", "true");
            engine.setProperty("file.resource.loader.path", TEMPLATE_DIR.getPath());
            engine.setProperty("file.resource.loader.cache", "false");
            engine.setProperty("directive.set.null.allowed", "true");
            engine.setProperty("runtime.interpolate.string.literals", "true");
            engine.init();

        } catch (Exception e) {
            System.out.println("Problem initializing Velocity : " + e);
        }
        restart();
    }

    private void restart() {
        context = new VelocityContext();
        context.put("gedcom", new Gedcom());
        context.put("sorter", new SortTool());
        context.put("date", new DateTool());
        context.put("null", null);
    }

    @Override
    public void put(String key, Object o) {
        if (o instanceof Entity) {
            put(key, (Entity) o);
        } else if (o instanceof Gedcom) {
            put(key, (Gedcom) o);
        } else if (o instanceof Entity[]) {
            put(key, (Entity[]) o);
        } else {
            context.put(key, o);
        }
    }

    private void put(String key, Gedcom e) {
        context.put(key, new GedcomWrapper(e));
    }

    private void put(String key, Fam f) {
        FamWrapper rf = null;
        if (f != null) {
            rf = new FamWrapper(f);
        }
        put(key, rf);
    }

    private void put(String key, Indi i) {
        context.put(key, new IndiWrapper(i));
    }

    private void put(String key, Entity e) {
        if (e instanceof Indi) {
            put(key, ((Indi) e));
        } else if (e instanceof Fam) {
            put(key, ((Fam) e));
        } else {
            put(key, new EntityWrapper(e));
        }
    }

    private void put(String key, Entity[] entities) {
        if (entities.length == 0) {
            put(key, new Object[0]);
        } else if (entities[0] instanceof Indi) {
            IndiWrapper[] reportIndis = new IndiWrapper[entities.length];
            for (int i = 0; i < entities.length; i++) {
                reportIndis[i] = new IndiWrapper((Indi) entities[i]);
            }
            put(key, reportIndis);
        } else if (entities[0] instanceof Fam) {
            FamWrapper[] reportFams = new FamWrapper[entities.length];
            for (int i = 0; i < entities.length; i++) {
                reportFams[i] = new FamWrapper((Fam) entities[i]);
            }
            put(key, reportFams);
        } else {
            put(key, (Object) null);
        }
    }

    @Override
    public void render(String template, Writer out) {
        try {
            Template t = engine.getTemplate(template+".vm","ISO-8859-1");
            if (t != null)
                t.merge(context, out);
        } catch (Exception ee) {
        }
        restart();
    }
}
