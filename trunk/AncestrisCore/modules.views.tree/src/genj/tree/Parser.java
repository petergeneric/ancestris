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
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
  protected Path shapeMarrs, shapeDivs, shapePlus, shapeMinus; 
  protected Path shapeIndis, shapeIndisSquared, shapeIndisRounded, shapeFams, shapeFamsSquared, shapeFamsRounded; 
  
  /** padding (n, e, s, w) */
  protected int[] padIndis, padMinusPlus, padMarrs; 
  
  /** shapes and padding for multiple mariages */
  protected Path shapeEmpty, shapeNext1, shapeNext2; 
  protected int[] padEmpty, padNext1, padNext2; 
  
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
    initDivsShapes();
    initNextFamShapes();
    
    shapeIndis = model.isRoundedRectangle() ? shapeIndisRounded : shapeIndisSquared;  
    shapeFams = model.isRoundedRectangle() ? shapeFamsRounded : shapeFamsSquared;  
    
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
   * Init shapes/padding for entities
   */
  private void initEntityShapes() {
    
    // .. padding (n,w,e,s)
    padIndis  = new int[] { 
      (metrics.pad + 1)/2, // add 1 to fix rounding issue with marr rings display
      (metrics.pad + 1)/2, 
      (metrics.pad + 1)/2, 
      (metrics.pad + 1)/2
    };
    
    // indis
    shapeIndisSquared = new Path().append(new Rectangle2D.Double(
      -metrics.wIndis/2,
      -metrics.hIndis/2,
       metrics.wIndis,
       metrics.hIndis
    ));
     
    shapeIndisRounded = new Path().append(new RoundRectangle2D.Double(
      -metrics.wIndis/2,
      -metrics.hIndis/2,
       metrics.wIndis,
       metrics.hIndis, 5, 5
    ));
     
    // fams
    shapeFamsSquared = new Path().append(new Rectangle2D.Double(
      -metrics.wFams/2,
      -metrics.hFams/2,
       metrics.wFams,
       metrics.hFams
    ));
     
    shapeFamsRounded = new Path().append(new RoundRectangle2D.Double(
      -metrics.wFams/2,
      -metrics.hFams/2,
       metrics.wFams,
       metrics.hFams, 5, 5
    ));
     
    // done
  }
  
  /**
   * Init shapes/padding for signs
   */
  private void initFoldUnfoldShapes() {
    
    // how we pad signs (n,w,e,s)
    int pi = (model.getMetrics().indisThick + 1)/4;
    padMinusPlus  = new int[]{  
       -padIndis[0]+pi,
       1, 
       1, 
       padIndis[3]-pi
    };

    // size of signs 
    double d = 8;
    
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
    
    // done
  }
    
    /**
     * Calculates marriage rings
     */
    private void initMarrShapes() {

        shapeMarrs = new Path();

        // calculate maximum extension   
        int d = Math.min(metrics.wIndis / 4, metrics.hIndis / 4);
        
        // check model
        if (!model.isMarrSymbols()) {
            d /= 4;
            shapeMarrs.append(new Rectangle2D.Double(-d * 0.3F, -d * 0.3F, d * 0.6F, d * 0.6F));
        } else {
            // create result      
            Ellipse2D e = new Ellipse2D.Float(-d * 0.3F, -d * 0.3F, d * 0.6F, d * 0.6F);

            float dx = model.isVertical() ? d * 0.2F : d * 0.0F,
                  dy = model.isVertical() ? d * 0.0F : d * 0.2F;

            AffineTransform at1 = AffineTransform.getTranslateInstance(-dx, -dy),
                    at2 = AffineTransform.getTranslateInstance(dx, dy);

            shapeMarrs.append(e.getPathIterator(at1));
            shapeMarrs.append(e.getPathIterator(at2));
        }

        // how we pad marrs rings (n,w,e,s)
        int t = (model.getMetrics().indisThick + 1) / 2;
        int l = model.isVertical() ? metrics.hIndis / 2 : metrics.wIndis / 2;
        padMarrs = new int[]{
            padIndis[0] + l - (int) (d * 0.3F),
            t,
            t,
            0
        };
        
        
        // done
    }

    /**
     * Calculates divorce rings. Should be exact same size as shapeMarr n order to attach the NextFam shapes
     */
    private void initDivsShapes() {

        shapeDivs = new Path();

        // calculate maximum extension   
        int d = Math.min(metrics.wIndis / 4, metrics.hIndis / 4);
        
        // check model
        if (!model.isMarrSymbols()) {
            d /= 4;
            shapeDivs.append(new Rectangle2D.Double(-d * 0.3F, -d * 0.3F, d * 0.6F, d * 0.6F));
        } else {
            // create result      
            Ellipse2D e = new Ellipse2D.Float(-d * 0.2F, -d * 0.2F, d * 0.4F, d * 0.4F);

            float dx = model.isVertical() ? d * 0.3F : d * 0.0F,
                  dy = model.isVertical() ? d * 0.0F : d * 0.3F;

            AffineTransform at1 = AffineTransform.getTranslateInstance(-dx, -dy),
                            at2 = AffineTransform.getTranslateInstance(dx, dy);

            shapeDivs.append(e.getPathIterator(at1));
            shapeDivs.append(e.getPathIterator(at2));
            shapeDivs.moveTo(new Point2D.Double(model.isVertical() ? 0 : -d * 0.2F, model.isVertical() ? -d * 0.2F : 0));
            shapeDivs.lineTo(new Point2D.Double(model.isVertical() ? 0 : d * 0.2F, model.isVertical() ? d * 0.2F : 0));
        }
        // done
    }
  /**
   * Calculates next fam shape
   */
    private void initNextFamShapes() {

        shapeEmpty = new Path();
        shapeEmpty.moveTo(new Point2D.Double(0, 0));
        if (model.isVertical()) {
            shapeEmpty.lineTo(new Point2D.Double(0, 1)); // there must be a non zero size on the vertical direction, but not on the other one to remain "invisible"
        } else {
            shapeEmpty.lineTo(new Point2D.Double(1, 0)); // there must be a non zero size on the horizontal direction, but not on the other one to remain "invisible"
        }
        shapeEmpty.lineTo(new Point2D.Double(0, 0));
        padEmpty = new int[]{ 0,0,0,0 };
        
        shapeNext1 = new Path();
        shapeNext2 = new Path();

        double x, d, w, h;
        
        if (model.isVertical()) {
            d = Math.min((double) (metrics.hIndis) / 12, 10F);
            w = Math.min((double) (metrics.wIndis) / 12, 8F);
            h = 2.5 * d;
            x = shapeMarrs.getBounds2D().getWidth()/2 + metrics.wIndis + metrics.indisThick; // width
            AffineTransform tx = new AffineTransform();
            tx.rotate(3.141592654F); //  = Pi
            padNext1 = new int[]{ (int) -shapeMarrs.getBounds2D().getHeight()*3/4 - metrics.hIndis/2 + (int) h/2, 0, 0, 0 }; // descendant indi height and ascendant spouse height
            padNext2 = new int[]{ -metrics.hIndis/2, 0, 0, 0 };  // descendant spouse height and ascendant indi height 
            shapeNext1.append(tx.createTransformedShape(getTmpShapeNext(x, 0, d, w, h)));
            shapeNext2.append(getTmpShapeNext(x, 0, d, w, h));
        } else {
            d = Math.min((double) (metrics.wIndis) / 12, 8F);
            w = Math.min((double) (metrics.hIndis) / 6, 10F);
            h = 2.5 * d;
            x = shapeMarrs.getBounds2D().getHeight()/2 + metrics.hIndis + metrics.indisThick; // height
            AffineTransform tx1 = new AffineTransform();
            tx1.rotate(4.712388979F); //  = 3 x Pi / 2
            AffineTransform tx2 = new AffineTransform();
            tx2.rotate(1,570796327F); //  = Pi / 2
            padNext1 = new int[]{ (int) -shapeMarrs.getBounds2D().getWidth()*3/4 - metrics.wIndis/2 + (int) h/2, 0, 0, 0 }; // width
            padNext2 = new int[]{ -metrics.wIndis/2, 0, 0, 0 };
            shapeNext1.append(tx1.createTransformedShape(getTmpShapeNext(x, 0, d, w, h)));
            shapeNext2.append(tx2.createTransformedShape(getTmpShapeNext(x, 0, d, w, h)));
        }
    }

    private Path getTmpShapeNext(double x0, double y0, double d, double w, double h) {
        Path tmpShape = new Path();
        // Border
        tmpShape.moveTo(new Point2D.Double(x0, y0));
        tmpShape.lineTo(new Point2D.Double(x0+w/2, y0));
        tmpShape.curveTo(new Point2D.Double(x0+w, y0), new Point2D.Double(x0+w, y0), new Point2D.Double(x0+w, y0+d));
        tmpShape.lineTo(new Point2D.Double(x0+w, y0+h));
        tmpShape.curveTo(new Point2D.Double(x0+w, y0+h+d), new Point2D.Double(x0+w, y0+h+d), new Point2D.Double(x0+w/2, y0+h+d));
        tmpShape.lineTo(new Point2D.Double(x0, y0+h+d));
        // + Sign
        int l = (int) Math.max(Math.min(w/2, h/2), 2);
        tmpShape.moveTo(new Point2D.Double(x0+(w-l)/2-1, y0 + (h+d)/2));
        tmpShape.lineTo(new Point2D.Double(x0+(w+l)/2-1, y0 + (h+d)/2));
        tmpShape.moveTo(new Point2D.Double(x0+w/2-1, y0 + (h+d-l)/2));
        tmpShape.lineTo(new Point2D.Double(x0+w/2-1, y0 + (h+d+l)/2));
        
        return tmpShape;
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
   * Helper to create a plus/minus
   * Inserts a nextFamity component where entity is the current family or the current spouse of the indi
   */
  protected TreeNode insertNextFamily(Indi indi, TreeNode parent, Fam[] fams, Entity entity, boolean isDescendant, boolean isFirst) {

      // check if relevant
    TreeNode node= parent;
    if (isDescendant) {
        if (fams == null || fams.length < 2) {
            return parent;
        }
        node = model.add(new TreeNode(model.new NextFamily(indi, fams, entity), isFirst?shapeNext1:shapeNext2, isFirst?padNext1:padNext2));
        model.add(new TreeArc(parent, node, false));
    } else {
        node = model.add(new TreeNode(model.new NextFamily(indi, fams, entity), (isFirst && model.isVertical()) || (!isFirst && !model.isVertical()) ? shapeNext1:shapeNext2, isFirst?padNext2:padNext1));
        model.add(new TreeArc(parent, node, false));
    }
    
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
        if (generation>=model.getMaxGenerations()||model.isHideAncestors(indi)) {
          insertPlusMinus(indi, node, true, true);
        } else {
          // show minus
          TreeNode minus = insertPlusMinus(indi, node, true, false);
          // grab the family's husband/wife and their ancestors
          Indi wife = famc.getWife();
          Indi husb = famc.getHusband();
          //swap indi and spouse if horizontal
          if (!model.isVertical()){
              Indi i = wife;
              wife=husb;
              husb=i;
          }
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
      
    /** how we pad husband, spouse */
    private int[] padHusband,

      /**
       * how we pad husband, spouse
       */
      padWife;
    
    /** offset of spouses */
    private int offsetSpouse;
    
    /**
     * Constructor
     */
    protected AncestorsWithFams(Model model, TreeMetrics metrics) {
      super(model, metrics);
      
      // how we pad signs (n,w,e,s)
      int pi = (model.getMetrics().indisThick + 1)/4;
      padMinusPlus  = new int[]{  
         -padIndis[0]+pi,
         1, 
         1, 
         padIndis[3]-pi
      };

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

        // grab spouse&indi
        Indi spouse = fam.getWife();
        Indi indi = fam.getHusband();
        //swap indi and spouse if horizontal
        if (!model.isVertical()) {
            Indi i = spouse;
            spouse = indi;
            indi = i;
        }

        // Prepare multiple spouse data
        // FL: to be symetrical, if either of indi or spouse is multiple, we need to display both. In which case, if one is not multiple and the other one is, one is displayed with no color
        Fam[] famsspouse = spouse != null ? spouse.getFamiliesWhereSpouse() : null;
        Fam[] famsindi = indi != null ? indi.getFamiliesWhereSpouse() : null;
        
        // Draws boxes from right to left:

        // node for spouse & arc fam-spouse 
        TreeNode nSpouse = model.add(new TreeNode(spouse, shapeIndis, padHusband));
        model.add(new TreeArc(node, parse(spouse, nSpouse, hasParents(indi) ? -offsetSpouse : 0, generation + 1), false));
        
        // node for marr & arc fam-marr 
        TreeNode nMarr = model.add(new TreeNode(null, fam.areDivorced() ? shapeDivs : shapeMarrs, padMarrs));
        model.add(new TreeArc(node, nMarr, false));

        // node for husband & arc fam-indi 
        TreeNode nIndi = model.add(new TreeNode(indi, shapeIndis, padWife));
        model.add(new TreeArc(node, parse(indi, nIndi, hasParents(spouse) ? +offsetSpouse : 0, generation + 1), false));

        if (famsspouse != null && famsspouse.length > 1) {
            TreeNode nEmpty2 = model.add(new TreeNode(null, shapeEmpty, padEmpty));
            model.add(new TreeArc(nMarr, nEmpty2, false));
            insertNextFamily(spouse, nEmpty2, famsspouse, indi, false, false);
        }
            
        if (famsindi != null && famsindi.length > 1) {
            TreeNode nEmpty1 = model.add(new TreeNode(null, shapeEmpty, padEmpty));
            model.add(new TreeArc(nMarr, nEmpty1, false));
            insertNextFamily(indi, nEmpty1, famsindi, spouse, false, true);
        }
            
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
        if (generation>=model.getMaxGenerations()||model.isHideAncestors(indi)) {
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

      List<Indi> l = new ArrayList<Indi>(fams.length);

      for (int f=0; f<fams.length; f++) {
        // loop through children
        Indi[] children = fams[f].getChildren();
        if (!model.isVertical()){
            List<Indi> childrenList = Arrays.asList(children);
            Collections.reverse(childrenList);
            children = childrenList.toArray(new Indi[]{});
        }
        for (int c=0; c<children.length; c++) {
          if (!l.contains(children[c])) {
              l.add(children[c]);
          // on first arc
          if (node.getArcs().isEmpty()) {
            // stop when hiding descendants
            if (generation>=model.getMaxGenerations()||model.isHideDescendants(indi)) {
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
    
    /** how we pad husband, spouse */
    private int[] padHusband,

      /**
       * how we pad husband, spouse
       */
      padWife, 

      /**
       * how we pad husband, spouse
       */
      padNext;
    
    /** how we offset an indi above its marr */
    private int offsetHusband;
      
    /**
     * Constructor
     */
    protected DescendantsWithFams(Model model, TreeMetrics metrics) {
        super(model, metrics);

        // how we pad (n,w,e,s)
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

        padFams = new int[]{
            -(int) (metrics.pad * 0.4),
            padIndis[1],
            padIndis[2],
            padIndis[3]
        };
        
        int pf = (model.getMetrics().famsThick + 1) / 2;
        padMinusPlus = new int[]{
            -padIndis[0] + pf,
            1,
            1,
            padIndis[3]
        };

        if (model.isVertical()) {
            offsetHusband = -(metrics.wIndis + shapeMarrs.getBounds().width + model.getMetrics().indisThick / 2) / 2;
            
        } else {
            offsetHusband = -(metrics.hIndis + shapeMarrs.getBounds().height + model.getMetrics().indisThick / 2) / 2; 
        }


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
        if (!model.isVertical()){
            List<Indi> childrenList = Arrays.asList(children);
            Collections.reverse(childrenList);
            children = childrenList.toArray(new Indi[]{});
        }
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
            Fam[] famsindi = indi != null ? indi.getFamiliesWhereSpouse() : null;

            // no families is simple
            if (famsindi.length == 0) {
                TreeNode nIndi = model.add(new TreeNode(indi, shapeIndis, padIndis));
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                return nIndi;
            }

            Fam fam = model.getFamily(indi, famsindi);
            Indi spouse = fam.getOtherSpouse(indi);
            Fam[] famsspouse = spouse != null ? spouse.getFamiliesWhereSpouse() : null;

            // show first descendant on top of other spouse (horizontal trees go naturally bottom to top).
            TreeNode nIndi = null;
            TreeNode nSpouse = null;
            TreeNode nMarr = null;
            TreeNode nFam = null;

        if (model.isVertical()) {
                nIndi = model.add(new TreeNode(indi, shapeIndis, padHusband) {
                    public int getLongitude(Node node, Branch[] children, Orientation o) {
                        return super.getLongitude(node, children, o) + offsetHusband;
                    }
                });
                nSpouse = model.add(new TreeNode(spouse, shapeIndis, padWife));
            } else {
                nIndi = model.add(new TreeNode(indi, shapeIndis, padWife));
                nSpouse = model.add(new TreeNode(spouse, shapeIndis, padHusband) {
                    public int getLongitude(Node node, Branch[] children, Orientation o) {
                        return super.getLongitude(node, children, o) + offsetHusband;
                    }
                });
            }

            nMarr = model.add(new TreeNode(null, fam.areDivorced() ? shapeDivs : shapeMarrs, padMarrs));
            nFam = model.add(new TreeNode(fam, shapeFams, padFams));

            // Add arcs in a specific order (indi first for vertical, spouse first for horizontal)
            if (model.isVertical()) { // left to right
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                model.add(new TreeArc(pivot, nMarr, false));
                model.add(new TreeArc(pivot, nSpouse, false));
                model.add(new TreeArc(nIndi, nFam, false));
            } else { // bottom to top 
                model.add(new TreeArc(pivot, nSpouse, false));
                model.add(new TreeArc(pivot, nMarr, false));
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                model.add(new TreeArc(nSpouse, nFam, false));
            }

            if (famsspouse != null && famsspouse.length > 1) {
                TreeNode nEmpty2 = model.add(new TreeNode(null, shapeEmpty, padEmpty));
                model.add(new TreeArc(nMarr, nEmpty2, false));
                insertNextFamily(spouse, nEmpty2, famsspouse, fam, true, false);
            }
            if (famsindi != null && famsindi.length > 1) {
                TreeNode nEmpty1 = model.add(new TreeNode(null, shapeEmpty, padEmpty));
                model.add(new TreeArc(nMarr, nEmpty1, false));
                insertNextFamily(indi, nEmpty1, famsindi, fam, true, true);
            }
        
            // grab the children
            Indi[] children = fam.getChildren();
            if (!model.isVertical()) {
                List<Indi> childrenList = Arrays.asList(children);
                Collections.reverse(childrenList);
                children = childrenList.toArray(new Indi[]{});
            }
            for (int c = 0; c < children.length; c++) {

                // on first : no descendants for indi?
                if (c == 0) {
                    if (generation >= model.getMaxGenerations() || model.isHideDescendants(indi)) {
                        insertPlusMinus(indi, nFam, false, true);
                        break;
                    }
                    nFam = insertPlusMinus(indi, nFam, false, false);
                }

                // recurse into child        
                parse(children[c], nFam, generation + 1);

                // next child       
            }

            // done
            return nIndi;
        }

    } //DescendantsWithFams
  

} //Parser

