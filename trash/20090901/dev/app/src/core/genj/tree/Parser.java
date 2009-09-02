/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.tree;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import gj.awt.geom.Path;
import gj.layout.tree.Branch;
import gj.layout.tree.Orientation;
import gj.model.Node;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser
 */
/*package*/ abstract class Parser {
  
  /** the model we're working on*/
  protected Model model;

  /** metrics */
  protected TreeMetrics metrics;
  
  /** shapes */
  protected Path shapeMarrs, shapeIndis, shapeFams, shapePlus, shapeMinus, shapeNext; 

  /** padding (n, e, s, w) */
  protected int[] padIndis, padMinusPlus; 
  
  private final static int MAX_GENERATION = 20;
  
  /** 
   * gets an instance of a parser
   */
  public static Parser getInstance(boolean ancestors, boolean families, Model model, TreeMetrics metrics) {
    if (ancestors) {
      if (families) return new AncestorsWithFams(model, metrics);
      return new AncestorsNoFams(model, metrics);
    } else {
      if (families) return new DescendantsWithFams(model, metrics);
      return new DescendantsNoFams(model, metrics);
    }
  }
  
  /**
   * Constructor
   */
  protected Parser(Model mOdel, TreeMetrics mEtrics) {
    
    // keep the model&metrics
    model = mOdel;
    metrics = mEtrics;
    
    // init values
    initEntityShapes();
    initFoldUnfoldShapes();
    initMarrShapes();
    
    // done    
  }
   
  /**
   * parses a tree starting from entity
   * @param entity either fam or indi
   */
  public final TreeNode parse(Entity root) {
    return (root instanceof Indi) ? parse((Indi)root) : parse((Fam )root);
  }
  
  /**
   * Place another node at the origin
   */
  public TreeNode align(TreeNode other) {
    return other;
  }
  
  /**
   * parses a tree starting from an indi
   */
  protected abstract TreeNode parse(Indi indi);
  
  /**
   * parses a tree starting from a family
   */
  protected abstract TreeNode parse(Fam fam);
  
  /**
   * Calculates marriage rings
   */
  private void initMarrShapes() {

    shapeMarrs = new Path();
    
    // check model
    if (!model.isMarrSymbols()) { 
      shapeMarrs.append(new Rectangle2D.Double());
      return; 
    }

    // calculate maximum extension    
    int d = Math.min(metrics.wIndis/4, metrics.hIndis/4);
    
    // create result      
    Ellipse2D e = new Ellipse2D.Float(-d*0.3F,-d*0.3F,d*0.6F,d*0.6F);

    float 
      dx = model.isVertical() ? d*0.2F : d*0.0F,
      dy = model.isVertical() ? d*0.0F : d*0.2F;

    AffineTransform 
      at1 = AffineTransform.getTranslateInstance(-dx,-dy),
      at2 = AffineTransform.getTranslateInstance( dx, dy);

    shapeMarrs.append(e.getPathIterator(at1));
    shapeMarrs.append(e.getPathIterator(at2));
    
    // patch bounds - I wish I could just make the marr symbol
    // laterally align with the boxes of husband/wife instead
    Rectangle2D r = shapeMarrs.getBounds2D();
    if (model.isVertical()) {
      r.setRect(r.getMinX(), -metrics.hIndis/2-metrics.pad/2, r.getWidth(), metrics.hIndis+metrics.pad);
    } else {
      r.setRect(-metrics.wIndis/2-metrics.pad/2, r.getMinY(), metrics.wIndis+metrics.pad, r.getHeight());
    }
    shapeMarrs.setBounds2D(r);
   
    // done
  }

  /**
   * Init shapes/padding for entities
   */
  private void initEntityShapes() {
    
    // .. padding (n,w,e,s)
    padIndis  = new int[] { 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2, 
      metrics.pad/2
    };
    
    // indis
    shapeIndis = new Path().append(new Rectangle2D.Double(
      -metrics.wIndis/2,
      -metrics.hIndis/2,
       metrics.wIndis,
       metrics.hIndis
    ));
     
    // fams
    shapeFams = new Path().append(new Rectangle2D.Double(
      -metrics.wFams/2,
      -metrics.hFams/2,
       metrics.wFams,
       metrics.hFams
    ));
     
    // done
  }
  
  /**
   * Init shapes/padding for signs
   */
  private void initFoldUnfoldShapes() {
    
    // how we pad signs (n,w,e,s)
    padMinusPlus  = new int[]{  
      -padIndis[3], 
       padIndis[1], 
       padIndis[2], 
       padIndis[3]     
    };

    // size of signs (3mm)
    double d = 3;
    
    // plus
    shapePlus = new Path();
    shapePlus.moveTo(new Point2D.Double( 0,-d*0.3));
    shapePlus.lineTo(new Point2D.Double( 0, d*0.3));
    shapePlus.moveTo(new Point2D.Double(-d*0.3, 0));
    shapePlus.lineTo(new Point2D.Double( d*0.3, 0));
    shapePlus.append(new Rectangle2D.Double(-d/2,-d/2,d,d));

    // minus    
    shapeMinus = new Path();
    shapeMinus.moveTo(new Point2D.Double(-d*0.3, 0));
    shapeMinus.lineTo(new Point2D.Double(+d*0.3, 0));
    shapeMinus.append(new Rectangle2D.Double(-d/2,-d/2,d,d));
    
    // more
    shapeNext = new Path();
    shapeNext .moveTo(new Point2D.Double(-d*0.3,-d*0.3));
    shapeNext .lineTo(new Point2D.Double(+d*0.3,     0));
    shapeNext .lineTo(new Point2D.Double(-d*0.3,+d*0.3));
    shapeNext .append(new Rectangle2D.Double(-d/2,-d/2,d,d));
    
    // done
  }
    
  /**
   * Helper to create a plus/minus
   */
  protected TreeNode insertPlusMinus(Indi indi, TreeNode parent, boolean ancestors, boolean plus) {
    // check if we're doing fold/unfolds
    if (!model.isFoldSymbols()) return parent;
    // do it
    TreeNode node = model.add(new TreeNode(model.new FoldUnfold(indi,ancestors), plus?shapePlus:shapeMinus, padMinusPlus));
    model.add(new TreeArc(parent, node, false));
    // done
    return node;
  }
  
  /**
   * Parser - Ancestors without Families
   */
  private static class AncestorsNoFams extends Parser {
    /**
     * Constructor
     */
    protected AncestorsNoFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam, java.awt.geom.Point2D)
     */
    protected TreeNode parse(Fam fam) {
      throw new IllegalArgumentException();
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi, java.awt.geom.Point2D)
     */
    protected TreeNode parse(Indi indi) {
      return parse(indi, 0);
    }
    
    private TreeNode parse(Indi indi, int generation) {
      // node for indi      
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis));
      // do we have a family we're child in?
      Fam famc = indi.getFamilyWhereBiologicalChild();
      if (famc!=null) {
        // stop when hiding ancestors
        if (generation>MAX_GENERATION||model.isHideAncestors(indi)) {
          insertPlusMinus(indi, node, true, true);
        } else {
          // show minus
          TreeNode minus = insertPlusMinus(indi, node, true, false);
          // grab the family's husband/wife and their ancestors
          Indi wife = famc.getWife();
          Indi husb = famc.getHusband();
          if (wife!=null) model.add(new TreeArc(minus, parse(wife, generation+1), true));
          if (husb!=null) model.add(new TreeArc(minus, parse(husb, generation+1), true));
          // done
        }
      } 
      // done
      return node;
    }
  } //AncestorsNoFams 
   
  /**
   * Parser - Ancestors with Families
   */
  private static class AncestorsWithFams extends Parser {
    
    /** how we pad families */
    private int[] padFams;
      
    /** how we pad husband, wife */
    private int[] padHusband, padWife;
    
    /** offset of spouses */
    private int offsetSpouse;
    
    /**
     * Constructor
     */
    protected AncestorsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
      
      // .. fams ancestors (n,w,e,s)
      padFams  = new int[]{  
         padIndis[0], 
         padIndis[1], 
         padIndis[2], 
        -(int)(metrics.pad*0.40),
      };

      padHusband = new int[]{
        padIndis[0],
        padIndis[1],
        0,
        padIndis[3]
      };

      padWife = new int[]{
        padIndis[0],
        0,
        padIndis[2],
        padIndis[3]
      };
      
      offsetSpouse = (model.isVertical() ? metrics.wIndis : metrics.hIndis) / 2;
      
      // done      
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      return parse(fam, 0);
    }
    
    private TreeNode parse(Fam fam, int generation) {

      // node for the fam
      TreeNode node = model.add(new TreeNode(fam, shapeFams, padFams));
      
      // grab wife&husb
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();

      // node for wife & arc fam-wife 
      TreeNode nWife = model.add(new TreeNode(wife, shapeIndis, padHusband));
      model.add(new TreeArc(node, parse(wife, nWife, hasParents(husb)?-offsetSpouse:0, generation+1), false)); 
      
      // node for marr & arc fam-marr 
      TreeNode nMarr = model.add(new TreeNode(null, shapeMarrs, null));
      model.add(new TreeArc(node, nMarr, false));
      
      // node for husband & arc fam-husb 
      TreeNode nHusb = model.add(new TreeNode(husb, shapeIndis, padWife));
      model.add(new TreeArc(node, parse(husb, nHusb, hasParents(wife)?+offsetSpouse:0, generation+1), false));
      
      // done
      return node;
    }
    
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi) {
      return parse(indi, model.add(new TreeNode(indi, shapeIndis, padIndis)), 0, 0);
    }
    
    /**
     * parse an individual's ancestors
     */
    private TreeNode parse(Indi indi, TreeNode nIndi, int align, int generation) {
      // might be a placeholder call
      if (indi==null) return nIndi;
      // do we have a family we're child in? 
      Fam famc = indi.getFamilyWhereBiologicalChild();
      if (famc!=null) {
        // no more ancestors?
        if (generation>MAX_GENERATION||model.isHideAncestors(indi)) {
          insertPlusMinus(indi, nIndi, true, true);
        } else {
          
          // patch with minus
          TreeNode nMinus = insertPlusMinus(indi, nIndi, true, false);
          model.add(new TreeArc(nMinus, parse(famc, generation), true));
          
          // patch alignment
          nMinus.align = align;
        }
      }
      // done
      return nIndi;
    }
    
    /**
     * helper that checks if an individual is child in a family
     */
    private boolean hasParents(Indi indi) {
      if (indi==null) return false;
      if (model.isHideAncestors(indi)) return false;
      return indi.getFamiliesWhereChild()!=null;
    }
    
  } //AncestorsWithFams
  
  /**
   * Parser - Descendants no Families
   */
  private static class DescendantsNoFams extends Parser {
    /**
     * Constructor
     */
    protected DescendantsNoFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi) {
      return parse(indi, 0);
    }
    
    private TreeNode parse(Indi indi, int generation) {
      // create node for indi
      TreeNode node = model.add(new TreeNode(indi, shapeIndis, padIndis)); 
      // grab fams
      Fam[] fams = indi.getFamiliesWhereSpouse();
      TreeNode pivot = node;
      // loop through fams

      List l = new ArrayList(fams.length);

      for (int f=0; f<fams.length; f++) {
        // loop through children
        Indi[] children = fams[f].getChildren();
        for (int c=0; c<children.length; c++) {
          if (!l.contains(children[c])) {
              l.add(children[c]);
          // on first arc
          if (node.getArcs().isEmpty()) {
            // stop when hiding descendants
            if (generation>MAX_GENERATION||model.isHideDescendants(indi)) {
              // insert plus
              insertPlusMinus(indi, node, false, true);
              // break
              break;
            }
            // insert minus
            pivot = insertPlusMinus(indi, node, false, false);
          }
          
          // parse child and arc from pivot to child
          model.add(new TreeArc(pivot, parse(children[c], generation+1), true));       

          // next child          
          } 
        }
      }
      // done
      return node;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      throw new IllegalArgumentException();
    }
    
  } //DescendantsNoFams
  
  /**
   * Parser - Descendants with Families 
   */
  private static class DescendantsWithFams extends Parser {
    
    /** real origin */
    private TreeNode origin;
  
    /** how we pad families */
    private int[] padFams;
    
    /** how we pad husband, wife */
    private int[] padHusband, padWife, padNext;
    
    /** how we offset an indi above its marr */
    private int offsetHusband;
      
    /**
     * Constructor
     */
    protected DescendantsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);

      // how we pad fams (n,w,e,s)
      padFams  = new int[]{  
        -(int)(metrics.pad*0.4), 
         padIndis[1], 
         padIndis[2], 
         padIndis[3]     
      };
      
      padHusband = new int[]{
        padIndis[0],
        padIndis[1],
        0,
        padIndis[3]
      };

      padWife = new int[]{
        padIndis[0],
        0,
        padIndis[2],
        0
      };
      
      padNext = new int[] {
        padIndis[0],
        -padIndis[1],
        0,
        0
      };
      
      offsetHusband = model.isVertical() ? 
        - (metrics.wIndis + shapeMarrs.getBounds().width )/2 :
        - (metrics.hIndis + shapeMarrs.getBounds().height)/2;
        
      // done
    }
    
    /**
     * Place another node at the origin
     */
    public TreeNode align(TreeNode other) {
      other.getPosition().setLocation(origin.getPosition());    
      return other;
    }
  
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Indi)
     */
    protected TreeNode parse(Indi indi) {
      // parse under artificial pivot
      TreeNode nPivot = model.add(new TreeNode(null, null, null));
      // the origin is not nPivot!!!
      origin = parse(indi, nPivot, 0);
      // done
      return nPivot;
    }
    /**
     * @see genj.tree.Model.Parser#parse(genj.gedcom.Fam)
     */
    protected TreeNode parse(Fam fam) {
      
      // node for fam (note patched padding)
      TreeNode nFam = model.add(new TreeNode(fam, shapeFams, padIndis));
      
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        // create an arc from node to node for indi
        parse(children[c], nFam, 0);       
         // next child
      }

      // the origin is the fam      
      origin = nFam;
      
      // done
      return nFam;
    }
        
    /**
     * recurse into indi
     * @param indi the indi to parse
     * @param pivot all nodes of descendant are added to pivot
     * @return MyNode
     */
    private TreeNode parse(Indi indi, TreeNode pivot, int generation) {

      // lookup its families      
      Fam[] fams = indi.getFamiliesWhereSpouse();
      
      // no families is simply
      if (fams.length==0) {
        TreeNode nIndi = model.add(new TreeNode(indi,shapeIndis,padIndis));
        model.add(new TreeArc(pivot, nIndi, pivot.getShape()!=null));
        return nIndi;        
      }

      // choose one of the families
      Fam fam = model.getFamily(indi, fams, false);
      
      // otherwise indi as husband first of family and arc pivot-indi
      TreeNode nIndi = model.add(new TreeNode(indi,shapeIndis,padHusband) {
        /**
         * @see genj.tree.TreeNode#getLongitude(gj.model.Node, gj.layout.tree.Branch[], gj.layout.tree.Orientation)
         */
        public int getLongitude(Node node, Branch[] children, Orientation o) {
          return super.getLongitude(node, children, o) + offsetHusband;
        }
      });
      model.add(new TreeArc(pivot, nIndi, pivot.getShape()!=null));
      
      // add marr and arc pivot-marr
      TreeNode nMarr = model.add(new TreeNode(null, shapeMarrs, null));
      model.add(new TreeArc(pivot, nMarr, false));
      
      // add spouse and arc pivot-spouse
      TreeNode nSpouse = model.add(new TreeNode(fam.getOtherSpouse(indi), shapeIndis, padWife));
      model.add(new TreeArc(pivot, nSpouse, false));
      
      // add 'next' spouse and arc spouse-next
      if (fams.length>1&&model.isFoldSymbols()) {
        TreeNode nNext = model.add(new TreeNode(model.new NextFamily(indi,fams), shapeNext, padNext));
        model.add(new TreeArc(pivot, nNext, false));
      }
            
      // add fam and arc indi-fam
      TreeNode nFam = model.add(new TreeNode(fam, shapeFams, padFams));
      model.add(new TreeArc(nIndi, nFam, false));
      
      // grab the children
      Indi[] children = fam.getChildren();
      for (int c=0; c<children.length; c++) {
        
        // on first : no descendants for indi?
        if (c==0) {
          if (generation>MAX_GENERATION||model.isHideDescendants(indi)) {
            insertPlusMinus(indi, nFam, false, true);
            break;
          }
          nFam = insertPlusMinus(indi, nFam, false, false);
        }

        // recurse into child        
        parse(children[c], nFam, generation+1);
        
        // next child       
      }
      
      // done
      return nIndi;
    }
    
  } //DescendantsWithFams
  

} //Parser

