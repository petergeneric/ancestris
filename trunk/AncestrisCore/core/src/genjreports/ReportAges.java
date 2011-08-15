package genjreports;

/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even 
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.TagPath;
import genj.gedcom.time.Delta;
import genj.gedcom.time.PointInTime;
import genj.report.Report;

/**
 * GenJ - ReportAges (based on ReportDescendants and ReportGedcomStatistics)
 */
public class ReportAges extends Report {

    public boolean reportBaptismAge = true;
    public boolean reportConfirmationAge = true;
    public boolean reportMarriageAge = true;
    public boolean reportAgeAtDivorce = true;
    public boolean reportAgeAtChildBirth = true;
    public boolean reportAgeAtEmigration = true;
    public boolean reportAgeAtImmigration = true;
    public boolean reportAgeAtNaturalization = true;
    public boolean reportAgeAtDeath = true;
    public boolean reportAgeSinceBirth = true;

    /** localized strings */
    private final static String AGE = Gedcom.getName("AGE");

    /**
     * Main for argument Indi
     */
    public void start(Indi indi) {

        // Display the ages
        analyzeIndi(indi);

        // Done
    }

    /**
     * Analyze an event and report its information, date and age of indi
     */
    private boolean analyzeEvent(boolean header, Indi indi, String tag) {

        // check for date under tag
        PropertyDate prop = (PropertyDate) indi.getProperty(new TagPath("INDI:" + tag + ":DATE"));
        if (prop == null || !prop.isValid())
            return false;

        // do the header
        if (header)
            println(getIndent(2) + Gedcom.getName(tag) + ':');

        // format and ouput
        println(getIndent(3) + prop.getDisplayValue());
        Delta age = indi.getAge(prop.getStart());
        printAge(age, 4);
        println();

        // done
        return true;
    }

    /**
     * Analyze and report ages for given individual
     */
    private void analyzeIndi(Indi indi) {

        Delta age = null;

        println(indi);

        // print birth date (give up if none)
        PropertyDate birth = indi.getBirthDate();
        if (birth == null || !birth.isValid()) {
            println(OPTIONS.getBirthSymbol()+translate("noData"));
            return;
        }
        println(OPTIONS.getBirthSymbol()+ birth);
        println();

        if (reportBaptismAge) {
            analyzeEvent(true, indi, "BAPM");
            analyzeEvent(true, indi, "BAPL");
            analyzeEvent(true, indi, "CHR");
            analyzeEvent(true, indi, "CHRA");
        }

        if (reportConfirmationAge)
            analyzeEvent(true, indi, "CONF");

        if (reportMarriageAge) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                println(getIndent(2) + Gedcom.getName("MARR") + ":");
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    String text = getIndent(3) + OPTIONS.getMarriageSymbol() +
                        " " + fam + ": ";
                    if (fam.getMarriageDate() == null)
                        println(text + translate("noData"));
                    else {
                        println(text + fam.getMarriageDate());
                        age = indi.getAge(fam.getMarriageDate().getStart());
                        printAge(age, 4);
                    }
                }
                println();
            }
        }

        if (reportAgeAtDivorce) {
            Fam[] fams = indi.getFamiliesWhereSpouse();
            if (fams.length > 0) {
                boolean found = false;
                for (int i = 0; i < fams.length; i++) {
                    Fam fam = fams[i];
                    if (fam.getDivorceDate() != null) {
                        if (!found) {
                            println(getIndent(2) + Gedcom.getName("DIV") + ":");
                            found = true;
                        }
                        println(getIndent(3) + OPTIONS.getDivorceSymbol() +
                                " " + fam + ": " + fam.getDivorceDate());
                        age = indi.getAge(fam.getDivorceDate().getStart());
                        printAge(age,4);
                    }
                }
                if (found)
                    println();
            }
        }

        if (reportAgeAtChildBirth) {
            Indi[] children = indi.getChildren();
            if (children.length > 0) {
                println(getIndent(2) + translate("childBirths") + ":");
                for (int i = 0; i < children.length; i++) {
                    Indi child = children[i];
                    String text = getIndent(3) + OPTIONS.getBirthSymbol()+child+": ";
                    PropertyDate cbirth = child.getBirthDate();
                    if (cbirth == null)
                        println(text + translate("noData"));
                    else {
                        println(text + cbirth);
                        age = indi.getAge(cbirth.getStart());
                        printAge(age,4);
                    }
                }
                println();
            }
        }

        if (reportAgeAtEmigration)
            analyzeEvent(true, indi, "EMIG");

        if (reportAgeAtImmigration)
            analyzeEvent(true, indi, "IMMI");

        if (reportAgeAtNaturalization)
            analyzeEvent(true, indi, "NATU");

        if (reportAgeAtDeath) {
            PropertyDate death = indi.getDeathDate();
            if (death != null) {
                println(getIndent(2) + Gedcom.getName("DEAT") + ":");
                println(getIndent(3) + OPTIONS.getDeathSymbol() + death);
                age = indi.getAge(indi.getDeathDate().getStart());
                printAge(age,4);
                println();
            }
        }

        if (reportAgeSinceBirth) {
            PointInTime now = PointInTime.getNow();
            age = indi.getAge(now);
            if (age != null) {
                println(getIndent(2) + translate("sinceBirth", now) + ":");
                printAge(age, 4);
            }
        }
    }

    /**
     * Print a computed age with given indent
     */
    private void printAge(Delta age, int indent) {
        if (age == null)
            println(getIndent(indent) + translate("noData"));
        else
            println(getIndent(indent) + AGE + ": " + age);
    }

} //ReportAges


/*********************************************************************************************************************
author               = Daniel P. Kionka, Carsten Müssig <carsten.muessig@gmx.net>
version              = 1.4
category             = text
updated              = $Date: 2010-02-11 15:19:43 $

name                 = Age at Events
name.de              = Alter
name.fr              = Ages
name.es              = Edad
name.pl              = Wieki życia


info = <h1><center>Age at Events</center></h1><p>This report prints out the age of an individual at different events during their life. Use the options to turn these events on/off.</p>
info.de = <h1><center>Alter zu verschiedenen Zeitpunkten</center></h1><p>Alter einer Person zu verschiedenen Zeitpunkten ausgeben. Die Zeitpunkte können in den Optionen gewählt werden.</p>
info.fr = <h1><center>Les âges au cours de la vie</center></h1><p>
 Après avoir choisi la personne sur laquelle vous voulez lancer ce rapport, vous obtiendrez l'âge d'une personne à différentes époques de sa vie.</p>
 <p>Ainsi on trouvera :</p>
 <ul>
  <li>La date de sa naissance,</li>
  <li>L'âge à son baptême,</li>
  <li>L'âge à son mariage,</li>
  <li>L'âge à son divorce,</li>
  <li>L'âge à la naissance de chacun de ses enfants,</li>
  <li>L'âge lors de son émigration,</li>
  <li>L'âge lors de son immigration,</li>
  <li>L'âge lors de sa naturalisation,</li>
  <li>L'âge qu'elle avait à son décès,</li>
  <li>Le temps écoulé depuis sa naissance.</li>
 </ul>
 <p>Utilisez les options pour visualiser tel ou tel évènement (actif : oui/non).</p>
 <p>Bien sûr, si tel ou tel évènement ne s'est pas produit, ou si une date n'est pas renseignée, l'information correspondante ne sera pas affichée.</p>
 
info.es = <h1><center>Edades de la vida</center></h1><p>Este informe imprime la edad de un individuo en diferentes momentos. 
 Use las opciones para incluir o no esos momentos</p>
info.pl = <h1><center>Wieki życia</center></h1><p>Ten raport wyświetla wiek osoby podczas różnych wydarzeń. 
 Zdarzenia te można wybrać na zakładce ustawień.</p>

reportMarriageAge    = Report age at marriage
reportMarriageAge.de = Alter bei Heirat ausgeben
reportMarriageAge.fr = Indication de l'âge lors du mariage
reportMarriageAge.es = Informe de la edad del matrimonio
reportMarriageAge.pl = Wyświetl wiek podczas małżeństwa

reportAgeAtDeath    = Report age at death
reportAgeAtDeath.de = Alter bei Tod ausgeben
reportAgeAtDeath.fr = Indication de l'âge lors du décès
reportAgeAtDeath.es = Informe de la edad de la muerte
reportAgeAtDeath.pl = Wyświetl wiek podczas śmierci

reportBaptismAge    = Report age at baptism
reportBaptismAge.de = Alter bei Taufe ausgeben
reportBaptismAge.fr = Indication de l'âge lors du baptême
reportBaptismAge.es = Informe de la edad del bautismo
reportBaptismAge.pl = Wyświetl wiek podczas chrztu

reportConfirmationAge    = Report age at confirmation
reportConfirmationAge.de = Alter bei Konfirmation ausgeben
reportConfirmationAge.fr = Indication de l'âge lors de la confirmation
reportConfirmationAge.es = Informe de la edad de la confirmación
reportConfirmationAge.pl = Wyświetl wiek podczas bierzmowania

reportAgeAtEmigration    = Report age at emigration
reportAgeAtEmigration.de = Alter bei Auswanderung ausgeben
reportAgeAtEmigration.fr = Indication de l'âge lors de l'émigration
reportAgeAtEmigration.es = Informe de la edad de la emigración
reportAgeAtEmigration.pl = Wyświetl wiek podczas emigracji

reportAgeAtImmigration    = Report age at imigration
reportAgeAtImmigration.de = Alter bei Einwanderung ausgeben
reportAgeAtImmigration.fr = Indication de l'âge lors de l'immigration
reportAgeAtImmigration.es = Informe de la edad de la inmigración
reportAgeAtImmigration.pl = Wyświetl wiek podczas imigracji

reportAgeAtNaturalization    = Report age at naturalization
reportAgeAtNaturalization.de = Alter bei Einbürgerung ausgeben
reportAgeAtNaturalization.fr = Indication de l'âge lors de la naturalisation
reportAgeAtNaturalization.es = Informe de la edad de la naturalización
reportAgeAtNaturalization.pl = Wyświetl wiek podczas naturalizacji

reportAgeAtDivorce    = Report age at divorce
reportAgeAtDivorce.de = Alter bei Scheidung ausgeben
reportAgeAtDivorce.fr = Indication de l'âge lors du divorce
reportAgeAtDivorce.es = Informe de la edad del divorcio
reportAgeAtDivorce.pl = Wyświetl wiek podczas rozwodu

reportAgeAtChildBirth    = Report age at birth of children
reportAgeAtChildBirth.de = Alter bei Geburt der Kinder ausgeben
reportAgeAtChildBirth.fr = Indication de l'âge lors de la naissance des enfants
reportAgeAtChildBirth.es = Informe de la edad en el nacimiento de los hijos
reportAgeAtChildBirth.pl = Wyświetl wiek podczas narodzin dzieci

childBirths           = Child birth(s)
childBirths.de        = Geburt der Kinder
childBirths.fr        = Naissance(s) des enfants
childBirths.es        = Nacimiento de los hijos
childBirths.pl        = Narodziny dzieci

reportAgeSinceBirth    = Report age since birth (until now)
reportAgeSinceBirth.de = Alter seit Geburt (bis heute) ausgeben
reportAgeSinceBirth.fr = Indication de l'âge depuis la naissance (jusqu'à maintenant)
reportAgeSinceBirth.es = Informe de la edad desde el nacimiento hasta ahora
reportAgeSinceBirth.pl = Wyświetl wiek od urodzenia (do teraz)

sinceBirth    = Since birth (until now {0})
sinceBirth.de = Seit Geburt (bis heute {0})
sinceBirth.fr = Depuis la naissance (jusqu'à maintenant {0})
sinceBirth.es = Desde el nacimiento (hasta ahora {0})
sinceBirth.pl = Od urodzenia (do teraz {0})

noData    = no data
noData.de = keine Daten
noData.fr = aucune donnée
noData.es = sin datos
noData.pl = brak danych

*********************************************************************************************************************/
