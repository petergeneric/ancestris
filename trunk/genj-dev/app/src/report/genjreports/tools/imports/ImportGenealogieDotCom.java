/**
 * Reports are Freeware Code Snippets
 *
 * This report is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * sur une base de gedrepohr.pl (Patrick TEXIER) pour la correction des REPO
 * Le reste des traitements par Daniel ANDRE 
 */

package genjreports.tools.imports;

import genj.report.Report;

import java.io.File;

/**
 * The import function for Heredis originated Gedcom files
 */
public class ImportGenealogieDotCom extends Importer{

	/**
	 * Constructor
	 */
	public ImportGenealogieDotCom(Report report, File fileIn) {
		super(report,fileIn);
	}
}

