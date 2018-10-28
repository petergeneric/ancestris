/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2018 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.renderer;

import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Grammar;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertySimpleReadOnly;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import java.util.HashMap;
import java.util.Map;

/**
 * Subclass to add capacity to dispay samples used in blueprint editor.
 *
 * @author zurga
 */
public class BlueprintRendererSample extends BlueprintRenderer {
    
      /** faked values */
  private final static Map<String,String> SAMPLES = new HashMap<String, String>();
  
  static {
    SAMPLES.put("NAME", "John /Doe/");
    SAMPLES.put("SEX" , "M");
    SAMPLES.put("DATE", "01 JAN 1900");
    SAMPLES.put("PLAC", "Nice Place");
    SAMPLES.put("ADDR", "Long Address");
    SAMPLES.put("CITY", "Big City");
    SAMPLES.put("POST", "12345");
  }

    /**
     * the grammar we're looking at
     */
    private final Grammar grammar;

    /**
     * Constructor.
     * @param grammar the grammar to use
     * @param bp 
     */
    public BlueprintRendererSample(Grammar grammar, Blueprint bp) {
        super(bp);
        this.grammar = grammar;
    }
    
    

    @Override
    public Property getProperty(Property entity, TagPath path) {

        // try to lookup from entity
        Property result = super.getProperty(entity, path);
        if (result != null) {
            return result;
        }

        // generate a sample value
        String sample = SAMPLES.get(path.getLast());
        if (sample == null) {
            sample = Gedcom.getName(path.getLast());
        }

        MetaProperty meta = grammar.getMeta(path, false);
        if (PropertyXRef.class.isAssignableFrom(meta.getType())) {
            sample = "@...@";
        }
        try {
            return meta.create(sample);
        } catch (GedcomException e) {
            return new PropertySimpleReadOnly(path.getLast(), sample);
        }
    }

}
