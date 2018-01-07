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
  protected Path shapeMarrs, shapePlus, shapeMinus, shapeNext; 
  protected Path shapeIndis, shapeIndisSquared, shapeIndisRounded, shapeFams, shapeFamsSquared, shapeFamsRounded; 

  /** padding (n, e, s, w) */
  protected int[] padIndis, padMinusPlus, padMarrs; 
  
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
        int d = Math.min(metrics.wIndis / 4, metrics.hIndis / 4);;
        
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
   * Calculates next fam shape
   */
    private void initNextFamShapes() {

        shapeNext = new Path();
        Path tmpShape = new Path();

        double d;
        double w;
        double h;
        
        if (model.isVertical()) {
            d = Math.max((double) (metrics.hIndis) / 12, 0.2F);
            w = Math.min(2 * d, (double) (metrics.wIndis) / 2);
            h = 4 * d;
        } else {
            d = Math.max((double) (metrics.wIndis) / 12, 0.2F);
            w = Math.min(2 * d, (double) (metrics.hIndis) / 4);
            h = 4 * d;
        }
        
        tmpShape = new Path();
        // Border
        tmpShape.moveTo(new Point2D.Double(0, 0));
        tmpShape.lineTo(new Point2D.Double(w/2, 0));
        tmpShape.curveTo(new Point2D.Double(w, 0), new Point2D.Double(w, 0), new Point2D.Double(w, d));
        tmpShape.lineTo(new Point2D.Double(w, h));
        tmpShape.curveTo(new Point2D.Double(w, h+d), new Point2D.Double(w, h+d), new Point2D.Double(w/2, h+d));
        tmpShape.lineTo(new Point2D.Double(0, h+d));
        // + Sign
        tmpShape.moveTo(new Point2D.Double(w/4, (h+d)/2));
        tmpShape.lineTo(new Point2D.Double(3*w/4, (h+d)/2));
        tmpShape.moveTo(new Point2D.Double(w/2, h/2));
        tmpShape.lineTo(new Point2D.Double(w/2, h/2 + d));
        
        if (model.isVertical()) {
            shapeNext.append(tmpShape);
        } else {
            AffineTransform tx = new AffineTransform();
            tx.rotate(4.712388979F); //  = 3 x Pi / 2
            shapeNext.append(tx.createTransformedShape(tmpShape));
        }
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
        if (generation>=model.getMaxGenerations()||model.isHideAncestors(indi)) {
          insertPlusMinus(indi, node, true, true);
        } else {
          // show minus
          TreeNode minus = insertPlusMinus(indi, node, true, false);
          // grab the family's husband/wife and their ancestors
          Indi wife = famc.getWife();
          Indi husb = famc.getHusband();
          //swap husb and wife if horizontal
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
      
    /** how we pad husband, wife */
    private int[] padHusband, padWife;
    
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
      
      // grab wife&husb
      Indi wife = fam.getWife();
      Indi husb = fam.getHusband();
      //swap husb and wife if horizontal
      if (!model.isVertical()){
          Indi i = wife;
          wife=husb;
          husb=i;
      }

      // node for wife & arc fam-wife 
      TreeNode nWife = model.add(new TreeNode(wife, shapeIndis, padHusband));
      model.add(new TreeArc(node, parse(wife, nWife, hasParents(husb)?-offsetSpouse:0, generation+1), false));
      
      // node for marr & arc fam-marr 
      TreeNode nMarr = model.add(new TreeNode(null, shapeMarrs, padMarrs));
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
    
    /** how we pad husband, wife */
    private int[] padHusband, padWife, padNext;
    
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
            padNext = new int[]{
                padIndis[0] + metrics.hIndis / 2 - (int) (shapeNext.getBounds2D().getHeight() / 2),
                -padIndis[1],
                0,
                0
            };
            offsetHusband = -(metrics.wIndis + shapeMarrs.getBounds().width + model.getMetrics().indisThick / 2) / 2;
            
        } else {
            
            padNext = new int[]{
                padIndis[0] + metrics.wIndis / 2 - (int) (shapeNext.getBounds2D().getWidth() / 2),
                -padIndis[1],
                0,
                0
            };
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
            Fam[] fams = indi.getFamiliesWhereSpouse();

            // no families is simple
            if (fams.length == 0) {
                TreeNode nIndi = model.add(new TreeNode(indi, shapeIndis, padIndis));
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                return nIndi;
            }

            // choose one of the families
            Fam fam = model.getFamily(indi, fams, false);

            // show first descendant on top of other spouse (horizontal trees go naturally bottom to top).
            TreeNode nIndi = null;
            TreeNode nSpouse = null;
            TreeNode nNext = null;
            TreeNode nMarr = null;
            TreeNode nFam = null;

            if (model.isVertical()) {
                nIndi = model.add(new TreeNode(indi, shapeIndis, padHusband) {
                    public int getLongitude(Node node, Branch[] children, Orientation o) {
                        return super.getLongitude(node, children, o) + offsetHusband;
                    }
                });
                nSpouse = model.add(new TreeNode(fam.getOtherSpouse(indi), shapeIndis, padWife));
            } else {
                nIndi = model.add(new TreeNode(indi, shapeIndis, padWife));
                nSpouse = model.add(new TreeNode(fam.getOtherSpouse(indi), shapeIndis, padHusband) {
                    public int getLongitude(Node node, Branch[] children, Orientation o) {
                        return super.getLongitude(node, children, o) + offsetHusband;
                    }
                });
            }

            if (fams.length > 1 && model.isFoldSymbols()) {
                nNext = model.add(new TreeNode(model.new NextFamily(indi, fams), shapeNext, padNext));
            }
            nMarr = model.add(new TreeNode(null, shapeMarrs, padMarrs));
            nFam = model.add(new TreeNode(fam, shapeFams, padFams));

            // Add arcs in a specific order
            if (model.isVertical()) { // left to right
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                model.add(new TreeArc(pivot, nMarr, false));
                model.add(new TreeArc(pivot, nSpouse, false));
                if (nNext != null) {
                    model.add(new TreeArc(pivot, nNext, false));
                }
                model.add(new TreeArc(nIndi, nFam, false));
            } else { // bottom to top 
                model.add(new TreeArc(pivot, nSpouse, false));
                model.add(new TreeArc(pivot, nMarr, false));
                model.add(new TreeArc(pivot, nIndi, pivot.getShape() != null));
                model.add(new TreeArc(nSpouse, nFam, false));
                if (nNext != null) {
                    model.add(new TreeArc(pivot, nNext, false));
                }
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

