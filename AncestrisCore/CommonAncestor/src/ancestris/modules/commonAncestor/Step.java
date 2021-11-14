package ancestris.modules.commonAncestor;

import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.PropertySex;


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

  protected static final int ALL = PropertySex.UNKNOWN;
  protected static final int MALE = PropertySex.MALE;
  protected static final int FEMALE = PropertySex.FEMALE;


  public Step(Fam famWhereChild, Indi link, int linkSex) {
    this.famWhereSpouse = famWhereChild;
    this.link = link;
    this.linkSex = linkSex;
  }

  public Indi getLink() {
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
    if (linkSex == Step.MALE || linkSex == Step.ALL) {
      return link;
    }
    if (famWhereSpouse != null) {
      return famWhereSpouse.getHusband();
    }
    return null;
  }

}
