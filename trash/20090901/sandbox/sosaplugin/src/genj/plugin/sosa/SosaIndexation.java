/**
 * This GenJ SosaIndexation Source is Freeware Code
 *
 * This source is distributed in the hope that it will be useful for creating custom GenJ plugins, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package genj.plugin.sosa;

//import genj.option.OptionProvider;
//import genj.option.PropertyOption;

//import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
//import genj.gedcom.GedcomLifecycleEvent;
//import genj.gedcom.GedcomLifecycleListener;
//import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
//import genj.gedcom.PropertyChild;
//import genj.gedcom.PropertyFamilyChild;
//import genj.gedcom.PropertyFamilySpouse;
//import genj.gedcom.PropertyHusband;
//import genj.gedcom.PropertyWife;
//import genj.plugin.ExtensionPoint;
//import genj.plugin.Plugin;
//import genj.util.Registry;
//import genj.util.Resources;
//import genj.util.swing.Action2;
//import genj.util.swing.ChoiceWidget;
//import genj.util.swing.ImageIcon;
//import genj.view.ExtendContextMenu;
//import genj.window.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * A sample plugin that manages Sosa Indexation of individuals
 */
	/**
	 * Sosa
	 */
	
	public class SosaIndexation {
	
		static final String SOSA_LABEL="_SOSA";
		static final String SOSA_INDEX_SEPARATOR=";";
		private final String emptySosaMarker="";
		static final int SOSA_INDEX_MARKER_LENGTH=5;
		//private final String biologicalBrotherAndSisterSosaMarker="+";
		//private final String biologicalBrotherAndSisterSpouseSosaMarker="++";
		//private final String otherBrotherAndSisterSosaMarker="~+";
		//private final String otherBrotherAndSisterSpousesSosaMarker="~++";
		private enum myExtendedSosaMarkerEnum {
			/* we initialise enum constants */
			BIOLOGICAL_BROTHER_AND_SISTER("(  +)"),
			BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE("( ++)"),
			OTHER_BROTHER_AND_SISTER("( ~+)"),
			OTHER_BROTHER_AND_SISTER_SPOUSE("(~++)");

			private String marker;

			/* constructor */
			myExtendedSosaMarkerEnum(String marker) {
				this.marker = marker;
			}

			/* method to retrieve constant value */
			public String getMarker() {
				return marker;
			}
		}

		private Map<Integer,Indi>myMap=new HashMap<Integer,Indi>();
		private	ArrayList<String>myList=new ArrayList<String>();
		private Logger LOG = Logger.getLogger("genj.plugin.sosa");
		private enum interactionType{_NULL,_SOSACutFromINDI,_SOSAAddedToINDI,_SOSAModifiedInINDI,_SOSADeletedFromINDI,_SOSASetValueToINDI,_CHILCutFromFAM,_CHILAddedToFAM,_newINDIInFAM,_newFAM};
		private interactionType action=interactionType._NULL;

		private Indi mySosaRoot;
		private Gedcom gedcom;
		
		private String sosaIndexArray[];
		private Map<Integer,Indi> sosaIndexIndiMap=new TreeMap<Integer,Indi>();
		
		/**
		* Sets Sosa indexation
		* <p>
		* This constructor sets sosa indexation starting from root individual
		*/
		public SosaIndexation(Indi mySosaRoot,Gedcom gedcom) {
			this.mySosaRoot=mySosaRoot;
			this.gedcom=gedcom;
			if (mySosaRoot != null) {
			setSosaIndexation(mySosaRoot);
			}
			LOG.fine("Sosa indexation mise dans les données = "+mySosaRoot);
			LOG.fine("=========Gedcom= "+gedcom);
		}
		
		/**
		* Sets Sosa indexation from root individual
		* <p>
		* This method sets sosa indexation starting from root individual
		*/
		
		public void setSosaIndexation(Indi indi) {
			/* we start with an empty map when necessary */
			if (myMap.size() !=0 ) myMap.clear();
			/* we set Sosa root value */
			int sosaIndex=1;
			/* (1) we remove all set _SOSA tags to start afresh */
			removeSosaIndexationFromAllIndis();
			//removeSosaIndexationFromIndi(indi,sosaIndex);
			/* (2) we build Sosa Index value */
			buildSosaIndexation(mySosaRoot,sosaIndex);
			/* we sort the map by alphabetical order of key */
			sosaIndexIndiMap=new TreeMap<Integer,Indi>(myMap);
			/* we start with an empty list when necessary */
			if (myList.size() !=0 ) myList.removeAll(myList);
			/* we build list of index values */
			for (Map.Entry <Integer,Indi> entry :sosaIndexIndiMap.entrySet()) {
				myList.add(Integer.toString(entry.getKey()));
			}
			/* we build string array of index values */
			sosaIndexArray=myList.toArray(new String[myList.size()]);
		}
		
		/**
		* Removes all _SOSA properties from all individuals
		* <p>
		* This method deletes all _SOSA properties in the Gedcom
		*/
		public void removeSosaIndexationFromAllIndis() {
			/* we need to search for all existing _SOSA properties to delete them */
			Property SosaProperties[];
			Indi indi;
			Collection indisCollection=gedcom.getEntities(Gedcom.INDI);
			for (Iterator it=indisCollection.iterator();it.hasNext();) {
				indi=(Indi)it.next();
				/* we delete all _SOSA properties of INDI */
				/* there might be more than one due to data base incoherencies */
				SosaProperties=indi.getProperties(SOSA_LABEL);
				for (int i=0;i<SosaProperties.length;i++) {
					/* we delete SosaProperties[i] of indi1 */
					indi.delProperty(SosaProperties[i]);
				}
			}
		}

		/**
		* Removes Sosa indexation from one individual
		* <p>
		* This method deletes all Sosa properties from one individual
		*
		* @param indi individual
		* @param sosaIndex Sosa index value
		*/
		public void removeSosaIndexationFromIndi(Indi indi,int sosaIndex) {
			Indi indis[],spouses[],children[];
			Fam famc,fams[];
			/* we delete the sosaIndex entry in the map */
			myMap.remove(sosaIndex);
			/* we delete Sosa property */
			deleteSosaIndexFromIndi(indi);
			/* we check for Sosa extension option */
			if (isExtendSosaIndexation() == true) {
				/* we process biological brothers ans sisters */
				indis=indi.getSiblings(false);
				for (int i=0;i<indis.length;i++) {
					/* we delete Sosa property for indis[i] */
					deleteSosaIndexFromIndi(indis[i]);
					/* we process biological brother and sister spouses */
					/* we get all spouses of indi */
					spouses=indis[i].getPartners();
					for (int j=0;j<spouses.length;j++) {
						/* we delete Sosa property for spouses[j] */
						deleteSosaIndexFromIndi(spouses[j]);
					}
				}
			}
			/* we get father of indi */
			Indi father=indi.getBiologicalFather();
			/* we get mother of indi */
			Indi mother=indi.getBiologicalMother();
			/* we get biological family of indi */
			famc=indi.getFamilyWhereBiologicalChild();
			/* we check for Sosa extension option */
			if (isExtendSosaIndexation() == true) {
				/* we process other brothers and sisters and their spouses */
				if (father != null) {
					/* we process father case */
					/* we get all families in which father is spouse */
					fams=father.getFamiliesWhereSpouse();
					for (int i=0;i<fams.length;i++) {
						/* we need to skip famc family already processed */
						if (fams[i] != famc) {
							/* we get all children of fams[i] */
							children=fams[i].getChildren();
							for (int j=0;j<children.length;j++) {
								/* we set Sosa property value for children[j] */
								deleteSosaIndexFromIndi(children[j]);
								/* we get all spouses of children[j] */
								spouses=children[j].getPartners();
								for (int k=0;k<spouses.length;k++) {
									/* we set Sosa property value for spouses[k] */
									deleteSosaIndexFromIndi(spouses[k]);
								}
							}
						}
					}
				}
				/* we process mother case */
				if (mother != null) {
					/* we get all families in which mother is spouse */
					fams=mother.getFamiliesWhereSpouse();
					for (int i=0;i<fams.length;i++) {
						/* we need to skip famc family already processed */
						if (fams[i] != famc) {
							/* we get all children of fams[i] */
							children=fams[i].getChildren();
							for (int j=0;j<children.length;j++) {
								/* we set Sosa property value for children[j] */
								deleteSosaIndexFromIndi(children[j]);
								/* we get all spouses of children[j] */
								spouses=children[j].getPartners();
								for (int k=0;k<spouses.length;k++) {
									/* we set Sosa property value for spouses[k] */
									deleteSosaIndexFromIndi(spouses[k]);
								}
							}
						}
					}
				}
			}
			/* we set Sosa index for one level up */
			sosaIndex=2*sosaIndex;
			/* we process biological father */
			if (father != null) {
				/* we delete Sosa index from father */
				removeSosaIndexationFromIndi(father,sosaIndex);
			}
			/* we set Sosa index of spouse */
			sosaIndex++;
			/* we process biological mother */
			if (mother != null) {
				/* we set Sosa index for mother */
				removeSosaIndexationFromIndi(mother,sosaIndex);
			}
		}

		/**
		* Sets _SOSA property value of individual
		* <p>
		* This method sets _SOSA property value ; if _SOSA property value contains a value the
		* provide value will be concatenated using a sosaIndexSeparator string as separator ;
		* the string marker is used in front of the index value to indicate Sosa extension feature
		* (see information provided in Report Option tab window)
		*
		* @param indi individual as selected by user
		* @param sosaIndex Sosa index value to be set in _SOSA property
		*/ 
		private void deleteSosaIndexFromIndi(Indi indi) {
			/* we check for _SOSA property */
			/* as we have first removed all _SOSA tags there are 2 cases : */
			/* no _SOSA property : we have to add one, */
			/* or one single _SOSA property already set by this method  : we have to update it */
			/* consequently we assume only one _SOSA property */
			Property sosaProperty=indi.getProperty(SOSA_LABEL);
			if (sosaProperty == null) {
				/* no Sosa property : we do nothing */
			}
			else {
				/* there is a Sosa property : we delete it */
				indi.delProperty(sosaProperty);
			}
		}
		
		public void deleteExistingSosaIndexFromIndi(Indi indi,Property sosaProperty) {
			/* we check for _SOSA property */
			/* as we have first removed all _SOSA tags there are 2 cases : */
			/* no _SOSA property : we have to add one, */
			/* or one single _SOSA property already set by this method  : we have to update it */
			/* consequently we assume only one _SOSA property */
			//Property sosaProperty=indi.getProperty(SOSA_LABEL);
			//if (sosaProperty == null) {
			//	/* no Sosa property : we do nothing */
			//}
			//else {
				/* there is a Sosa property : we delete it */
				indi.delProperty(sosaProperty);
			//}
		}
		/**
		* Removes Sosa property from an individual
		* <p>
		* This method removes the Sosa property from an individual
		*
		* @param indi individual
		* @param sosaProperty Sosa property
		*/
		
		public void removeSosaTagFromIndi(Indi indi,Property sosaProperty) {
			/* we delete Sosa property of indi */
			LOG.fine("juste avant effacement de _SOSA");
			indi.delProperty(sosaProperty);
			LOG.fine("juste après effacement de _SOSA");
		}

		/**
		* Sets Sosa indexation as a property of an individual
		* <p>
		* This method install a Sosa indexation from a root INDI which has Sosa index value = 1
		*
		* @param indi individual
		* @param sosaIndex Sosa index value
		*/

		private void buildSosaIndexation(Indi indi,int sosaIndex) {
			Indi indis[],spouses[],children[];
			Fam famc,fams[];
			/* we set map entry */
			//setHashMapEntry(indi,sosaIndex);
			/* we set an entry in HashMap */
			myMap.put(sosaIndex,indi);
			/* we set Sosa property value */
			//setSosaIndexToIndi(indi,sosaIndex,emptySosaMarker);
			setSosaIndexToIndi(indi,sosaIndex,emptySosaMarker);
			/* we check for Sosa extension option */
			if (isExtendSosaIndexation() == true) {
				/* we process biological brothers ans sisters */
				indis=indi.getSiblings(false);
				for (int i=0;i<indis.length;i++) {
					/* we set Sosa property value for indis[i] */
					//setSosaIndexToIndi(indis[i],sosaIndex,biologicalBrotherAndSisterSosaMarker);
					setSosaIndexToIndi(indis[i],sosaIndex,myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER.getMarker());
					/* we process biological brother and sister spouses */
					/* we get all spouses of indi */
					spouses=indis[i].getPartners();
					for (int j=0;j<spouses.length;j++) {
						/* we set Sosa property value for spouses[j] */
						//setSosaIndexToIndi(spouses[j],sosaIndex,biologicalBrotherAndSisterSpouseSosaMarker);
						setSosaIndexToIndi(spouses[j],sosaIndex,myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE.getMarker());
					}
				}
			}
			/* we get father of indi */
			Indi father=indi.getBiologicalFather();
			/* we get mother of indi */
			Indi mother=indi.getBiologicalMother();
			/* we get biological family of indi */
			famc=indi.getFamilyWhereBiologicalChild();
			/* we check for Sosa extension option */
			if (isExtendSosaIndexation() == true) {
				/* we process other brothers and sisters and their spouses */
				if (father != null) {
					/* we process father case */
					/* we get all families in which father is spouse */
					fams=father.getFamiliesWhereSpouse();
					for (int i=0;i<fams.length;i++) {
						/* we need to skip famc family already processed */
						if (fams[i] != famc) {
							/* we get all children of fams[i] */
							children=fams[i].getChildren();
							for (int j=0;j<children.length;j++) {
								/* we set Sosa property value for children[j] */
								//setSosaIndexToIndi(children[j],sosaIndex,otherBrotherAndSisterSosaMarker);
								setSosaIndexToIndi(children[j],sosaIndex,myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER.getMarker());
								/* we get all spouses of children[j] */
								spouses=children[j].getPartners();
								for (int k=0;k<spouses.length;k++) {
									/* we set Sosa property value for spouses[k] */
									//setSosaIndexToIndi(spouses[k],sosaIndex,otherBrotherAndSisterSpousesSosaMarker);
									setSosaIndexToIndi(spouses[k],sosaIndex,myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER_SPOUSE.getMarker());
									//myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER.getMarker()

								}
							}
						}
					}
				}
				/* we process mother case */
				if (mother != null) {
					/* we get all families in which mother is spouse */
					fams=mother.getFamiliesWhereSpouse();
					for (int i=0;i<fams.length;i++) {
						/* we need to skip famc family already processed */
						if (fams[i] != famc) {
							/* we get all children of fams[i] */
							children=fams[i].getChildren();
							for (int j=0;j<children.length;j++) {
								/* we set Sosa property value for children[j] */
								//setSosaIndexToIndi(children[j],sosaIndex,otherBrotherAndSisterSosaMarker);
								setSosaIndexToIndi(children[j],sosaIndex,myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER.getMarker());
								/* we get all spouses of children[j] */
								spouses=children[j].getPartners();
								for (int k=0;k<spouses.length;k++) {
									/* we set Sosa property value for spouses[k] */
									//setSosaIndexToIndi(spouses[k],sosaIndex,otherBrotherAndSisterSpousesSosaMarker);
									setSosaIndexToIndi(spouses[k],sosaIndex,myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER_SPOUSE.getMarker());
								}
							}
						}
					}
				}
			}
			/* we set Sosa index for one level up */
			sosaIndex=2*sosaIndex;
			/* we process biological father */
			if (father != null) {
				/* we set Sosa index for father */
				buildSosaIndexation(father,sosaIndex);
			}
			/* we set Sosa index of spouse */
			sosaIndex++;
			/* we process biological mother */
			if (mother != null) {
				/* we set Sosa index for mother */
				buildSosaIndexation(mother,sosaIndex);
			}
		}


		/**
		* Sets _SOSA property value of individual
		* <p>
		* This method sets _SOSA property value ; if _SOSA property value contains a value the
		* provide value will be concatenated using a sosaIndexSeparator string as separator ;
		* the string marker is used in front of the index value to indicate Sosa extension feature
		* (see information provided in Report Option tab window)
		*
		* @param indi individual as selected by user
		* @param sosaIndex Sosa index value to be set in _SOSA property
		* @param sosaMarker string marker used in front of index value
		*/ 
		private void setSosaIndexToIndi(Indi indi,int sosaIndex,String sosaMarker) {
			String sosaIndex1=sosaMarker+String.valueOf(sosaIndex);
			/* we check for _SOSA property */
			/* as we have first removed all _SOSA tags there are 2 cases : */
			/* no _SOSA property : we have to add one, */
			/* or one single _SOSA property already set by this method  : we have to update it */
			/* consequently we assume only one _SOSA property */
			Property SosaProperty=indi.getProperty(SOSA_LABEL);
			if (SosaProperty == null) {
				/* no _SOSA property : we need to create one with the proper value */
				SosaProperty=indi.addProperty(SOSA_LABEL,sosaIndex1);
			}
			else {
				/* there is a _SOSA property : we need to set the proper value */
				/* if a value is present we concatenate the new value */
				//SosaProperty.setValue(SosaProperty.getValue()+sosaIndexSeparator+sosaIndex1);
				SosaProperty.setValue(SosaProperty.getValue()+SOSA_INDEX_SEPARATOR+sosaIndex1);
			}
		}

		/**
		* Restores _SOSA property value of individual
		* <p>
		* This method restores _SOSA property value of an individual
		*
		* @param indi individual as selected by user
		* @param sosaValue Sosa index value to be set in _SOSA property
		*/ 
		public void restoreSosaValueToIndi(Indi indi,String sosaValue) {
			indi.addProperty(SOSA_LABEL,sosaValue);
		}
		
		/**
		* Restores Sosa indexation in individual cut from family
		* <p>
		* This method restores all _SOSA property value resulting from a cut action of an individual
		* from a family
		*
		* @param indiIndex string index of individual cut
		* @param famIndex string index of family the individual belong to
		*/ 

		public void restoreSosaInChildCutFromFam(Indi indi,Fam fam) {
			LOG.fine("CHILD : "+indi+" cut from FAM : "+fam);
			Property sosaProperty=indi.getProperty(SOSA_LABEL);
			/* we initialise new Sosa index */
			String newIndex="";
			/* we check for indi Sosa property */
			if (sosaProperty != null) {
				/* this individual has a Sosa property -> we process it */
				String sosaIndex=sosaProperty.getValue();
				//LOG.fine("_SOSA = "+sosaIndex);
				int indexBeginning=0;
				//int indexEnd=sosaIndex.indexOf(sosaIndexSeparator);
				int indexEnd=sosaIndex.indexOf(SOSA_INDEX_SEPARATOR);
				while (indexEnd != -1) {
					newIndex=buildNewSosaIndex("cutCHILFromFAM",indi,fam,sosaIndex.substring(indexBeginning,indexEnd),newIndex);
					indexBeginning=indexEnd+1;
					//indexEnd=sosaIndex.indexOf(sosaIndexSeparator,indexBeginning);
					indexEnd=sosaIndex.indexOf(SOSA_INDEX_SEPARATOR,indexBeginning);
				}
				newIndex=buildNewSosaIndex("cutCHILFromFAM",indi,fam,sosaIndex.substring(indexBeginning),newIndex);
				//LOG.fine("NEW _SOSA= "+newIndex);
				/* we set the correct Sosa index */
				if (newIndex.length() == 0) {
					/* we delete the Sosa property */
					action=interactionType._SOSADeletedFromINDI;
					indi.delProperty(sosaProperty);
					//LOG.fine("NEW _SOSA= supprimé");
				} else {
					/* we set the new Sosa property */
					action=interactionType._SOSAModifiedInINDI;
					sosaProperty.setValue(newIndex);
					//LOG.fine("NEW _SOSA= installé");
				}
			} else {
				/* no Sosa property -> we do nothing */
				LOG.fine("we do nothing");
			}
			/* we check for the impact of spouses of indi on family=fam of indi */
			Indi[] spouses=indi.getPartners();
			for (int i = 0; i < spouses.length; i++) {
				LOG.fine("épouse :" + spouses[i]);
				Property spouseSosaProperty = spouses[i].getProperty(SOSA_LABEL);
				if (spouseSosaProperty != null) {
					/* spouse has a Sosa property -> we process it */
					String spouseSosaIndex=spouseSosaProperty.getValue();
					String spouseSosaIndexToBeProcessed;
					int indexBeginning=0;
					//int indexEnd=spouseSosaIndex.indexOf(sosaIndexSeparator);
					int indexEnd=spouseSosaIndex.indexOf(SOSA_INDEX_SEPARATOR);
					while (indexEnd != -1) {
						spouseSosaIndexToBeProcessed=processImpactOfSpouseOnSosaIndexOfBrotherAndSisterOfIndi("cutCHILFromFAM",spouses[i],fam,spouseSosaIndex.substring(indexBeginning,indexEnd));
						if (spouseSosaIndexToBeProcessed.length() != 0) {
							LOG.fine("we have to process impact of index :"+spouseSosaIndexToBeProcessed);
						}
						indexBeginning=indexEnd+1;
						//indexEnd=spouseSosaIndex.indexOf(sosaIndexSeparator,indexBeginning);
						indexEnd=spouseSosaIndex.indexOf(SOSA_INDEX_SEPARATOR,indexBeginning);
					}
					spouseSosaIndexToBeProcessed=processImpactOfSpouseOnSosaIndexOfBrotherAndSisterOfIndi("cutCHILFromFAM",spouses[i],fam,spouseSosaIndex.substring(indexBeginning));
					if (spouseSosaIndexToBeProcessed.length() != 0) {
						LOG.fine("we have to process impact of index :"+spouseSosaIndexToBeProcessed);
					}
				}
			}
	}
		
		public String buildNewSosaIndex(String actionType, Indi indi,Fam fam,String indiSosaSubIndex,String indiNewSosaIndex) {
			LOG.fine("_SOSA partial= "+indiSosaSubIndex);
			String indiLinkIndex;
			String biologicalBrotherAndSisterSpouseSosaMarker=myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE.getMarker();
			String biologicalBrotherAndSisterSosaMarker=myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER.getMarker();
			String otherBrotherAndSisterSpouseSosaMarker=myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER_SPOUSE.getMarker();
			String otherBrotherAndSisterSosaMarker=myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER.getMarker();
			if (actionType.equals("cutCHILFromFAM")) {
				if (isExtendSosaIndexation()) {
					String beginning=indiSosaSubIndex.substring(0,SOSA_INDEX_MARKER_LENGTH);
					/* we have to process extended Sosa value */
					if (indiSosaSubIndex.startsWith(biologicalBrotherAndSisterSpouseSosaMarker)) {
						LOG.fine(indiSosaSubIndex+" commence par :"+biologicalBrotherAndSisterSpouseSosaMarker);
						//indiLinkIndex=indiSosaSubIndex.substring(biologicalBrotherAndSisterSpouseSosaMarker.length());
						indiLinkIndex=indiSosaSubIndex.substring(biologicalBrotherAndSisterSpouseSosaMarker.length());
						/* this indi was spouse of brother or sister of linked Sosa indi = indiLinkIndex /*
						/* he still is */
						LOG.fine("this indi is spouse of brother or sister of _SOSA= "+indiLinkIndex);
						LOG.fine("this indi remains as such");
					} else {
						//if (indiSosaSubIndex.startsWith(biologicalBrotherAndSisterSosaMarker)) {
						if (indiSosaSubIndex.startsWith(biologicalBrotherAndSisterSosaMarker)) {
							LOG.fine(indiSosaSubIndex+" commence par :"+biologicalBrotherAndSisterSosaMarker);
							//indiLinkIndex=indiSosaSubIndex.substring(biologicalBrotherAndSisterSosaMarker.length());
							indiLinkIndex=indiSosaSubIndex.substring(biologicalBrotherAndSisterSosaMarker.length());
							/* this indi was brother or sister of linked Sosa indi = indiLinkIndex /*
							/* he is no more */
							LOG.fine("this indi is brother or sister of _SOSA= "+indiLinkIndex);
							LOG.fine("this indi is no more");
							indiSosaSubIndex="";
							/* his spouses have no more extended sosa related to indi = indiLinkIndex /*
							/* we process extended sosa values of his spouses */
							Indi[] spouses=indi.getPartners();
							for (int k=0;k<spouses.length;k++) {
								LOG.fine("épouse :"+spouses[k]);
								/* we strip biologicalBrotherAndSisterSosaSpouseSosaMarker from spouse sosa */
								Property spouseSosaProperty=spouses[k].getProperty(SOSA_LABEL);
								/* we check for spouseSosaProperty */
								if (spouseSosaProperty != null) {
									/* spouse has a sosa property -> we process it */
									String spouseSosaIndex=spouseSosaProperty.getValue();
									LOG.fine("spouseSosaIndex avant= "+spouseSosaIndex);
									//int i0=spouseSosaIndex.indexOf(biologicalBrotherAndSisterSpouseSosaMarker+indiLinkIndex);
									//int i1=i0+(biologicalBrotherAndSisterSpouseSosaMarker+indiLinkIndex).length();
									int i0=spouseSosaIndex.indexOf(biologicalBrotherAndSisterSpouseSosaMarker+indiLinkIndex);
									int i1=i0+(biologicalBrotherAndSisterSpouseSosaMarker+indiLinkIndex).length();
									//myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE.getMarker()
									//LOG.fine("i0 avant= "+i0);
									//LOG.fine("i1 avant= "+i1);
									//LOG.fine("index length= "+spouseSosaIndex.length());
									if (i1 != (spouseSosaIndex.length())) {
										i1+=1;
										//LOG.fine("pas dernier i1= "+i1);
									} else {
										//LOG.fine("dernier i1= "+i1);
										if (i0 != 0) {
											i0+=-1;
											//LOG.fine("pas premier i0= "+i0);
										} //else {
											//LOG.fine("premier i0= "+i0);
										//}
									}
									String spouseStrippedIndex=spouseSosaIndex.substring(i0,i1);
									//LOG.fine("spouseStrippedIndex= "+spouseStrippedIndex);
									spouseSosaIndex=spouseSosaIndex.replaceFirst("\\Q"+spouseStrippedIndex+"\\E","");
									//LOG.fine("spouseSosaIndex après= "+spouseSosaIndex);
									if (spouseSosaIndex.length() == 0) {
										/* there is no more a Sosa property for this spouse */
										action=interactionType._SOSADeletedFromINDI;
										spouses[k].delProperty(spouseSosaProperty);
										//LOG.fine("Remove _SOSA from INDI :"+spouses[k]);
									} else {
										/* we strip separator in first position */
										//spouseSosaIndex=spouseSosaIndex.substring(1);
										/* we update the spouse Sosa index */
										action=interactionType._SOSAModifiedInINDI;
										spouseSosaProperty.setValue(spouseSosaIndex);
										//LOG.fine("Change _SOSA to :"+spouseSosaIndex+" in INDI :"+spouses[k]);
									}
								} else {
									/* we have no Sosa property -> error */
									LOG.fine("ERREUR !");
								}
							}
						} else {
							//if (indiSosaSubIndex.startsWith(otherBrotherAndSisterSpousesSosaMarker)) {
							if (indiSosaSubIndex.startsWith(otherBrotherAndSisterSpouseSosaMarker)) {
								LOG.fine(indiSosaSubIndex+" commence par :"+otherBrotherAndSisterSpouseSosaMarker);
								//indiLinkIndex=indiSosaSubIndex.substring(otherBrotherAndSisterSpousesSosaMarker.length());
								indiLinkIndex=indiSosaSubIndex.substring(otherBrotherAndSisterSpouseSosaMarker.length());
								/* this indi is spouse of indi that has same father or mother of linked Sosa indi = indiLinkIndex /*
								/* he still is */
								LOG.fine("this indi is spouse of indi that same father or mother of _SOSA= "+indiLinkIndex);
								LOG.fine("this indi remains as such");
							} else {
								//if (indiSosaSubIndex.startsWith(otherBrotherAndSisterSosaMarker)) {
								if (indiSosaSubIndex.startsWith(otherBrotherAndSisterSosaMarker)) {
									LOG.fine(indiSosaSubIndex+" commence par :"+otherBrotherAndSisterSosaMarker);
									//indiLinkIndex=indiSosaSubIndex.substring(otherBrotherAndSisterSosaMarker.length());
									indiLinkIndex=indiSosaSubIndex.substring(otherBrotherAndSisterSosaMarker.length());
									/* this indi has same father or mother of linked Sosa indi = indiLinkIndex /*
									/* he still is */
									LOG.fine("this indi has same father or mother of _SOSA= "+indiLinkIndex);
									LOG.fine("this indi remains as such");
								}
							}
						}
					}
				} else {
					/* we have to process regular Sosa value */
					LOG.fine("Regular index");
				}
				LOG.fine("BILAN "+indiSosaSubIndex);
				/* we update the indiNewSosaIndex with indiSosaSubIndex */
				if (indiSosaSubIndex.length() != 0) {
					if (indiNewSosaIndex.length() != 0) {
						LOG.fine("New index 1="+indiNewSosaIndex+SOSA_INDEX_SEPARATOR+indiSosaSubIndex);
						//return indiNewSosaIndex=indiNewSosaIndex+sosaIndexSeparator+indiSosaSubIndex;
						return indiNewSosaIndex=indiNewSosaIndex+SOSA_INDEX_SEPARATOR+indiSosaSubIndex;
					} else {
						LOG.fine("New index 2="+indiSosaSubIndex);
						return indiNewSosaIndex=indiSosaSubIndex;
					}
				} else {
					LOG.fine("New index 3="+indiNewSosaIndex);
					return indiNewSosaIndex;
				}
			} else {
				/* we return indiNewSosaIndex as is */
				LOG.fine("New index 4="+indiNewSosaIndex);
				return indiNewSosaIndex;
			}
		}

		public String processImpactOfSpouseOnSosaIndexOfBrotherAndSisterOfIndi(String actionType,Indi indi,Fam fam,String spouseSosaSubIndex) {
			LOG.fine("_SOSA partial= "+spouseSosaSubIndex);
			if (actionType.equals("cutCHILFromFAM")) {
				if (isExtendSosaIndexation()) {
					/* we have to process extended Sosa value */
					//if (spouseSosaSubIndex.startsWith(biologicalBrotherAndSisterSpouseSosaMarker)) {
					if (spouseSosaSubIndex.startsWith(myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE.getMarker())) {
						LOG.fine(spouseSosaSubIndex+" commence par :"+myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER_SPOUSE.getMarker());
						LOG.fine("we do nothing with= "+spouseSosaSubIndex);
						return "";
					} else {
						//if (spouseSosaSubIndex.startsWith(biologicalBrotherAndSisterSosaMarker)) {
						if (spouseSosaSubIndex.startsWith(myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER.getMarker())) {
							LOG.fine(spouseSosaSubIndex+" commence par :"+myExtendedSosaMarkerEnum.BIOLOGICAL_BROTHER_AND_SISTER.getMarker());
							LOG.fine("we do nothing with= "+spouseSosaSubIndex);
							return "";
						} else {
							//if (spouseSosaSubIndex.startsWith(otherBrotherAndSisterSpousesSosaMarker)) {
							if (spouseSosaSubIndex.startsWith(myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER_SPOUSE.getMarker())) {
								LOG.fine(spouseSosaSubIndex+" commence par :"+myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER_SPOUSE.getMarker());
								LOG.fine("we do nothing with= "+spouseSosaSubIndex);
								return "";
							} else {
								//if (spouseSosaSubIndex.startsWith(otherBrotherAndSisterSosaMarker)) {
								if (spouseSosaSubIndex.startsWith(myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER.getMarker())) {
									LOG.fine(spouseSosaSubIndex+" commence par :"+myExtendedSosaMarkerEnum.OTHER_BROTHER_AND_SISTER.getMarker());
									LOG.fine("we do nothing with= "+spouseSosaSubIndex);
									return "";
								} else {
									LOG.fine("we have to process impact of : "+spouseSosaSubIndex);
									return spouseSosaSubIndex;
								}
							}
						}
					}
				} else {
					/* we have to process regular Sosa value of spouse */
					// String spouseLinkIndex=indiSosaSubIndex;
					LOG.fine("we have to process impact of : "+spouseSosaSubIndex);
					return spouseSosaSubIndex;
				}
			} else {
				LOG.fine("other cases to be processed");
				return "";
			}
		}

		public void restoreSosaInHusbCutFromFam(Indi indi,Fam fam) {
			LOG.fine("HUSB : "+indi+" cut from FAM : "+fam);
		}

		public void restoreSosaInWifeCutFromFam(Indi indi,Fam fam) {
			LOG.fine("WIFE : "+indi+" cut from FAM : "+fam);
		}

		public void restoreSosaInFamsCutFromMaleIndi(Fam fam,Indi indi) {
			LOG.fine("FAMS : "+fam+" cut from INDI : "+indi+" (M)");
		}

		public void restoreSosaInFamsCutFromFemaleIndi(Fam fam,Indi indi) {
			LOG.fine("FAMS : "+fam+"indiNewSosaIndexfrom INDI : "+indi+" (F)");
		}

		public void restoreSosaInFamcCutFromIndi(Fam fam,Indi indi) {
			// same as INDI cut from FAM
			LOG.fine("FAMC : "+fam+" cut  from INDI : "+indi);
		}

		public void restoreSosaInChildAddedToFam(Indi indi,Fam fam) {
			LOG.fine("INDI : "+indi+" created in FAM : "+fam);
		}
		
		public void addNewIndiToFam(Indi indi, Fam fam) {
			LOG.fine("INDI : "+indi+" created in FAM : "+fam);
		}
		
		/**
		* Returns the array of Sosa indexes
		* <p>
		* This method returns a string array of all Sosa indexes as built from Sosa root
		* This array is sorted by alphabetical order
		* 
		* @return sosaIndexArray string array of all Sosa indexes
		*/
		public String[] getSosaIndexArray() {
			return sosaIndexArray;
		}

		/**
		* Returns the map of Sosa index <> individual
		* <p>
		* This method returns the map all Sosa indexes versus individuals as built from Sosa root
		* This map is sorted by aceding order of Sosa indexes
		* 
		* @return sosaIndexIndiMap map of all Sosa indexes versus individuals
		*/
		public Map<Integer,Indi> getSosaMap() {
			return sosaIndexIndiMap;
		}
	
		/**
		* Sets the Sosa gedcom
		* <p>
		* This method sets the gedcom
		* 
		* @param gedcom Sosa gedcom
		*/
		public void setSosaGedcom(Gedcom gedcom) {
			this.gedcom=gedcom;
		}

		/**
		* Sets the Sosa root individual
		* <p>
		* This method set the Sosa root individual used to build Sosa indexation
		* 
		* @param mySosaRoot Sosa root individual
		*/
		public void setSosaRoot(Indi indi) {
			LOG.fine("Sosa indexation mise dans les données = "+indi);
			this.mySosaRoot=indi;
		}

		/**
		* Returns the Sosa root individual
		* <p>
		* This method returns the Sosa root individual used to build Sosa indexation
		* 
		* @param mySosaRoot Sosa root individual
		*/
		public Indi getSosaRoot() {
			return mySosaRoot;
		}
		
		/**
		 * Check whether sosa indexation is actually turned on by user
		 */
		private boolean isExtendSosaIndexation() {
		  return SosaOptions.getInstance().isExtendSosaIndexation;
		}

	}
