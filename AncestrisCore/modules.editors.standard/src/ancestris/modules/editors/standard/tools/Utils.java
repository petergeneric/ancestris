/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package ancestris.modules.editors.standard.tools;

import ancestris.core.TextOptions;
import static ancestris.util.swing.FileChooserBuilder.getExtension;
import static ancestris.util.swing.FileChooserBuilder.imgExtensions;
import static ancestris.util.swing.FileChooserBuilder.sndExtensions;
import static ancestris.util.swing.FileChooserBuilder.vidExtensions;
import genj.gedcom.Fam;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertySex;
import genj.gedcom.PropertyXRef;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.util.Exceptions;




/**
 *
 * @author frederic
 */
public class Utils {
    
    private static Image IMG_INVALID_PHOTO = null;
    private static Image IMG_VIDEO = null;
    private static Image IMG_SOUND = null;
    private static Image IMG_NO_SOURCE_MEDIA = null;
    private static Image IMG_JUST_TEXT_MEDIA = null;
    
    public static boolean parentTagsContains(Property prop, String tag) {
        if (prop == null) {
            return false;
        }
        Property parent;
        if (prop instanceof PropertyXRef) {
            parent = ((PropertyXRef) prop).getTargetParent();
        } else {
            parent = prop.getParent();
        }
        if (parent == null) {
            return false;
        }
        if (parent.getTag().equals(tag)) {
            return true;
        }
        return parentTagsContains(parent, tag);
    }
    
    public static Image getImageFromFile(File file, Class clazz) {
        return getImageFromFile(file, clazz, false);
    }
    
    public static Image getImageFromFile(File file, Class clazz, boolean noText) {
        
        if (clazz != null) {
            try {
                IMG_INVALID_PHOTO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/invalid_photo.png"));
                IMG_VIDEO = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/video.png"));
                IMG_SOUND = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/sound.png"));
                IMG_NO_SOURCE_MEDIA = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/source_dummy_small.png"));
                IMG_JUST_TEXT_MEDIA = ImageIO.read(clazz.getResourceAsStream("/ancestris/modules/editors/standard/images/source_text_only_small.png"));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Image image = null;

        if (file == null) {
            return clazz == SourceChooser.SourceThumb.class && !noText ? IMG_JUST_TEXT_MEDIA : IMG_NO_SOURCE_MEDIA;
        }
        if (Arrays.asList(imgExtensions).contains(getExtension(file))) {
            try {
                image = ImageIO.read(new FileInputStream(file));
            } catch (Exception ex) {
                //Exceptions.printStackTrace(ex);
                image = IMG_INVALID_PHOTO;
            }
        } else if (Arrays.asList(vidExtensions).contains(getExtension(file))) {
            image = IMG_VIDEO;
        } else if (Arrays.asList(sndExtensions).contains(getExtension(file))) {
            image = IMG_SOUND;
        } else {
            image = IMG_INVALID_PHOTO;
        }
        if ((image.getWidth(null) <= 0) || (image.getHeight(null) <= 0)) {
            return IMG_INVALID_PHOTO;
        }
    
        return image;
    }



    
    public static void getPropertiesRecursively(Property parent, String tag, List props) {
        Property[] children = parent.getProperties();
        for (Property child : children) {
            if (child.getTag().equals(tag)) {
                props.add(child);
            }
            getPropertiesRecursively(child, tag, props);
        }
    }
    
    
    public static Image scaleImage(File f, Class clazz, int width, int height) {
        return scaleImage(f, clazz, width, height, false);
    }
    
    public static Image scaleImage(File f, Class clazz, int width, int height, boolean noText) {
        Image image = getImageFromFile(f, clazz, noText);
        return scaleImage(image, width, height);
    }
    
    public static ImageIcon getResizedIcon(ImageIcon imageIcon, int targetWidth, int targetHeight) {
        return new ImageIcon(scaleImage(imageIcon.getImage(), targetWidth, targetHeight));
    }
    
    private static BufferedImage scaleImage(Image image, int targetWidth, int targetHeight) {

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if ((imageWidth <= 0) || (imageHeight <= 0)) {
            image.flush();
            return null;
        }
        
        double imageRatio = (double) imageWidth / (double) imageHeight;
        double targetRatio = (double) targetWidth / (double) targetHeight;
        if (targetRatio < imageRatio) {
            targetHeight = (int) (targetWidth / imageRatio);
        } else {
            targetWidth = (int) (targetHeight * imageRatio);
        }

        return resizeImage(image, targetWidth, targetHeight);
    }

     
    private static BufferedImage resizeImage(Image img, int width, int height) {
        BufferedImage dimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return dimg;
    }

    
    
    
    

    
    public static Indi[] getPotentialFamilyMembers(Indi indi, int relation) {
        
        List<Indi> l = new ArrayList<Indi>();
        for (Indi i : indi.getGedcom().getIndis()) {
            // Exclude itself
            if (i == indi) {
                continue;
            }
            
            if (isLikely(i, relation, indi, l.size())) {
                l.add(i);
            }
            
            // Quit if full (limit list to 15 names)
            if (l.size() >= 15) {
                break;
            }
            
        }
        // Add manuel choice
        l.add(null);
        
        // Build returned array
        Indi[] result = new Indi[l.size()];
        l.toArray(result);
        return result;
        
        
    }
    
    public static boolean isLikely(Indi i, int relation, Indi indi, int n) {
        return likelyResult(i, relation, indi, n) == 0;
    }
    
    /**
     * Test how likely the two people are related based on relationship tested
     * 
     * @param i
     * @param memberTag
     * @param indi
     * @param n
     * @return 
     *      0 : is likely
     * 
     *      1 : Father should be a male
     *      2 : Father should have the same lastname as his kids
     *      3 : Father should be at least 15 years older than his kids
     *      4 : Father should be less than 64 years older than his kids
     *      5 : Father should still be alive at his kid's birth.
     * 
     *      6 : Mother should be a female
     *      7 : Mother's husband should have the same lastname as her kids
     *      8 : Mother should be at least 17 years older than her kids
     *      9 : Mother should be less than 47 years older than her kids
     *     10 : Mother should still be alive at her kid's birth.
     * 
     *     11 : Brother should be a male
     *     12 : Brother should have the same lastname as his siblings
     *     13 : Brother should be less than 21 years younger than his siblings
     *     14 : Brother should be less than 21 years older than his siblings
     *     15 : Selected individual is already a brother of the current individual
     *     16 : Brother should not be an ancestor of his siblings
     * 
     *     21 : Sister should be a female
     *     22 : Sister should have the same lastname as his siblings
     *     23 : Sister should be less than 21 years younger than her siblings
     *     24 : Sister should be less than 21 years older than her siblings
     *     25 : Selected individual is already a brother of the current individual
     *     26 : Sister should not be an ancestor of her siblings
     * 
     *     31 : Spouses should be of opposite sex
     *     32 : Spouse should have a different lastname
     *     33 : Spouse should be less than 20 years younger than his/her spouse
     *     34 : Spouse should be less than 20 years older than his/her spouse
     *     35 : Selected spouse has already got a family
     *     36 : Spouse should not be an ancestor of his/her spouse
     * 
     *     41 : Child should be born before current individual's death.
     *     42 : Child should have the same lastname as his/her father 
     *     43 : Child should have the same lastname as his/her mother's husband
     *     44 : Child should be more than 14 years younger than his/her parent
     *     45 : Child should be less than 50 years younger than his/her parent
     *     46 : Selected individual is already current individual's child
     *     47 : Child should not be an ancestor of his/her parent
     * 
     *     98 : Selected individual should be different from the current one
     *     99 : other reasons
     */
    public static int likelyResult(Indi i, int relation, Indi indi, int n) {
        
        if (i == indi) {
            return 98;
        }
        
        PropertyDate bd = null, ibd = null, dd = null, idd = null;
        long t = 0, it = 0, diffb = 0, diffbd = 0, diffdb = 0;
        
        // Calculate age difference
        t = 0;
        it = 0;
        diffb = 0;
        diffbd = 0;
        diffdb = 0;
        bd = indi.getBirthDate();
        ibd = i.getBirthDate();
        dd = indi.getDeathDate();
        idd = i.getDeathDate();
        if (ibd != null && bd != null && ibd.isComparable() && bd.isComparable()) {
            try {
                t = bd.getStart().getJulianDay();
                it = ibd.getStart().getJulianDay();
                diffb = (it - t) / (1461 / 4);    // 1461/4 = 365.25
            } catch (GedcomException ex) {
                diffb = 0; //Exceptions.printStackTrace(ex);
            }
        }
        
        // Calculate diff between indi birth and i death (if diffbd < 0, i cannot be father of indi)
        if (idd != null && bd != null && idd.isComparable() && bd.isComparable()) {
            try {
                t = bd.getStart().getJulianDay();
                it = idd.getStart().getJulianDay();
                diffbd = (it - t) / (1461 / 4);    // 1461/4 = 365.25
            } catch (GedcomException ex) {
                diffbd = 0; //Exceptions.printStackTrace(ex);
            }
        }
        
        // Calculate diff between indi death and i birth in years (if diffdb > 0, i cannot be child of indi)
        if (ibd != null && dd != null && ibd.isComparable() && dd.isComparable()) {
            try {
                t = dd.getStart().getJulianDay();
                it = ibd.getStart().getJulianDay();
                diffdb = (it - t) / (1461 / 4);    // 1461/4 = 365.25
            } catch (GedcomException ex) {
                diffdb = 0; //Exceptions.printStackTrace(ex);
            }
        }

        if (relation == IndiCreator.REL_FATHER) {
            if (i.getSex() != PropertySex.MALE) {
                return 1;
            }
            if (!indi.getLastName().equals(i.getLastName())) {
                return 2;
            }
            boolean oldEnough = diffb < -15;
            if (!oldEnough) {
                return 3;
            }
            boolean youngEnough = diffb > -64;
            if (!youngEnough) {
                return 4;
            }
            boolean notDead = diffbd >= 0;
            if (!notDead) {
                return 5;
            }
            return 0;
        }
        else if (relation == IndiCreator.REL_MOTHER) {
            if (i.getSex() != PropertySex.FEMALE) {
                return 6;
            }
            boolean oldEnough = diffb < -17;
            if (!oldEnough) {
                return 8;
            }
            boolean youngEnough = diffb > -47;
            if (!youngEnough) {
                return 9;
            }
            boolean notDead = diffbd >= 0;
            if (!notDead) {
                return 10;
            }
            Fam[] fams = i.getFamiliesWhereSpouse();
            for (Fam fam : fams) {
                if (fam != null) {
                    Indi husb = fam.getHusband();
                    if (husb == null) {
                        continue;
                    }
                    if (!indi.getLastName().equals(husb.getLastName())) {
                        return 7;
                    }
                    return 0; // we have found a father with identical lastname
                }
            }
            if (fams.length == 0 && n >= 10) { // no more than 10 outsiders based on age only
                return 99;
            }
            return 0;
        }

        //          - With no known father nor mother
        else if (relation == IndiCreator.REL_BROTHER) {
            if (i.getSex() != PropertySex.MALE) {
                return 11;
            }
            if (!indi.getLastName().equals(i.getLastName())) {
                return 12;
            }
            boolean oldEnough = diffb < 21;
            if (!oldEnough) {
                return 13;
            }
            boolean youngEnough = diffb > -21;
            if (!youngEnough) {
                return 14;
            }
            Fam fam1 = i.getFamilyWhereBiologicalChild();
            Fam fam2 = indi.getFamilyWhereBiologicalChild();
            if (fam1 != null && fam1 == fam2) {
                return 15;
            }
            if (i.isAncestorOf(indi)) {
                return 16;
            }
            return 0;
        }
        else if (relation == IndiCreator.REL_SISTER) {
            if (i.getSex() != PropertySex.FEMALE) {
                return 21;
            }
            if (!indi.getLastName().equals(i.getLastName())) {
                return 22;
            }
            boolean oldEnough = diffb < 21;
            if (!oldEnough) {
                return 23;
            }
            boolean youngEnough = diffb > -21;
            if (!youngEnough) {
                return 24;
            }
            Fam fam1 = i.getFamilyWhereBiologicalChild();
            Fam fam2 = indi.getFamilyWhereBiologicalChild();
            if (fam1 != null && fam1 == fam2) {
                return 25;
            }
            if (i.isAncestorOf(indi)) {
                return 26;
            }
            return 0;
        }

        else if (relation == IndiCreator.REL_PARTNER) {
            if (i.getSex() == indi.getSex()) {
                return 31;
            }
            if (indi.getLastName().equals(i.getLastName())) {
                return 32;
            }
            boolean oldEnough = diffb < 20;
            if (!oldEnough) {
                return 33;
            }
            boolean youngEnough = diffb > -20;
            if (!youngEnough) {
                return 34;
            }
            Fam[] fams = i.getFamiliesWhereSpouse();
            if ((fams != null && fams.length != 0)) {
                return 35;
            }
            if (i.isAncestorOf(indi)) {
                return 36;
            }
            return 0;
        }

        // Potential children
        //          - Same lastname
        //          - Born between 14 and 50 years after
        //          - With no known parents
        //          - not an ancestor
        //
        else if (relation == IndiCreator.REL_CHILD) {
            if (diffdb > 0) {
                return 41;
            }
            if ((indi.getSex() == PropertySex.MALE)) {
                if (!indi.getLastName().equals(i.getLastName())) {
                    return 42;
                }
            }
            if ((indi.getSex() == PropertySex.FEMALE)) {
                boolean husbFound = false;
                boolean husbDiff = false;
                Fam[] fams = indi.getFamiliesWhereSpouse();
                for (Fam fam : fams) {
                    Indi husb = fam.getHusband();
                    if (husb == null) {
                        continue;
                    }
                    husbFound = true;
                    if (!husb.getLastName().equals(i.getLastName())) {
                        husbDiff = true;
                    }
                }
                if (husbFound && husbDiff) {
                    return 43;
                }
            }
            boolean oldEnough = diffb > 14;
            if (!oldEnough) {
                return 44;
            }
            boolean youngEnough = diffb < 50;
            if (!youngEnough) {
                return 45;
            }
            Fam fam1 = i.getFamilyWhereBiologicalChild();
            if (fam1 != null) {
                Fam[] fams = indi.getFamiliesWhereSpouse();
                for (Fam fam : fams) {
                    if (fam1 == fam) {
                        return 46;
                    }
                }
            }
            if (i.isAncestorOf(indi)) {
                return 47;
            }
            return 0;
        }
        
        // Else not likely
        return 99;
    }
    
    
    public static String getDetails(Indi indi) {
        String ret = "";
        
        if (indi == null) {
            return ret;
        }
        ret += indi.toString(true);
        
        PropertyDate birth = indi.getBirthDate();
        if (birth != null && birth.getStart() != null && birth.getStart().getYear() < 3000) {
            ret += " " + TextOptions.getInstance().getBirthSymbol() + birth.getStart().getYear();
        }
        
        PropertyDate death = indi.getDeathDate();
        if (death != null && death.getStart() != null && death.getStart().getYear() < 3000) {
            ret += " " + TextOptions.getInstance().getDeathSymbol() + death.getStart().getYear();
        }

        return ret;
    }

    /**
     * 
     * @param indi
     * @return null if none or only one spouse, current spouse otherwise
     */
    public static Indi getCurrentSpouse(Indi indi, JTree familyTree) {
        
        // Return is no spouse or only one
        Fam[] fams = indi.getFamiliesWhereSpouse();
        if (fams == null || fams.length == 1) {
            return null;
        }
        
        // If nothing selected, return first spouse
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) familyTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            return fams[0].getOtherSpouse(indi);
        }
        
        // If spouse cannot be determined from selection, return first spouse
        NodeWrapper node = (NodeWrapper) selectedNode.getUserObject();
        Indi spouse = node.getCurrentSpouse(indi);
        if (spouse == null) {
            return fams[0].getOtherSpouse(indi);
        }
        
        // Else return detected spouse
        return spouse;
    }
    
    
}
