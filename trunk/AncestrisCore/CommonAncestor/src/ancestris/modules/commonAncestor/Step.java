package ancestris.modules.commonAncestor;

import genj.gedcom.Fam;
import genj.gedcom.Indi;


/**
 * a step in the link chain between one of the two Indis chosen by the user and the researched ancestor
 * @author ylh
 *
 */
/**
 *
 * @author michel
 */


public class Step {
  Fam famWhereSpouse;
  int linkSex;
  Indi link;

  protected static final int MALE = 1;
  protected static final int FEMALE = 2;


  public Step(Fam famWhereChild, Indi link, int linkSex) {
    this.famWhereSpouse = famWhereChild;
    this.link = link;
    this.linkSex = linkSex;
  }

  public Indi getLink() {
    //			if(linkSex==MALE){
    //				return fam.getHusband();
    //			}else if(linkSex==FEMALE){
    //				return fam.getWife();
    //			}
    //			else{
    //				Log.info("the link has no determined sex...");
    //				Log.info("the link has no determined sex...");
    //				return null;
    //			}
    return link;
  }

  /* ----------------- */
  /**
   * get the wife of this step : it may be the link or the link's wife, or null if link had no wife
   * @return
   */
  public Indi getWife() {
    if (linkSex == Step.FEMALE) {
      return link;
    }
    if (famWhereSpouse != null) {
      return famWhereSpouse.getWife();
    }
    return null;
  }

  /* ----------------- */
  /**
   * get the husband of this step : it may be the link or the link's husband, or null if link had no husband
   * @return
   */
  public Indi getHusband() {
    if (linkSex == Step.MALE) {
      return link;
    }
    if (famWhereSpouse != null) {
      return famWhereSpouse.getHusband();
    }
    return null;
  }

}
