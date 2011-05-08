/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2009 Nils Meier <nils@meiers.net>
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
 *
 * $Revision: 1.1 $ $Author: pewu $ $Date: 2009/10/21 08:40:54 $
 */
package genj.report;

import genj.common.ContextListWidget;
import genj.fo.Document;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.view.ViewContext;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Abstract base class for reports that generate annotation output.
 * Displays the annotations in a window or saves them as a FO document.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
public abstract class AnnotationsReport extends Report
{
	/**
	 * List of collected annotations.
	 */
	private List<Context> annotations = new ArrayList<Context>();

	/**
	 * List header or other message when the annotation list is empty.
	 */
	private String message;

	/**
	 * Switch to start in windowed mode (false) or to generate a FO document (true).
	 */
    public boolean startAsFo = false;

    /**
     * Starts the report.
     */
	@Override
	public void start(Object context) throws Throwable
	{
		Gedcom gedcom = getGedcom(context);

		// Reset variables
		annotations.clear();
		message = getName();

		// Start actual report.
		super.start(context);

		if (!startAsFo)
			showAnnotationsToUser(gedcom, message, annotations);
		else
		{
			Document doc = new Document(getName());
			doc.startSection(message);
			for (Context ctx : annotations)
			{
				doc.addText(ctx.getText());
				doc.nextParagraph();
			}
		    showDocumentToUser(doc);
		}
	}

	protected void setMessage(String message)
	{
		this.message = message;
	}

	protected void addAnnotation(Context ctx)
	{
		annotations.add(ctx);
	}

	protected void addAnnotation(Entity entity)
	{
		annotations.add(new ViewContext(entity));
	}

	protected void addAnnotation(Entity entity, String text)
	{
		annotations.add(new ViewContext(entity).setText(text));
	}

	protected void addAnnotation(Property property, String text)
	{
		annotations.add(new ViewContext(property).setText(text));
	}

	protected void sortAnnotations()
	{
		Collections.sort(annotations);
	}

	/**
	 *We're not using the console.
	 */
    @Override
	public boolean usesStandardOut()
	{
		return false;
	}

	/**
	 * Show annotations containing text and references to Gedcom objects.
	 */
	private void showAnnotationsToUser(Gedcom gedcom, String msg,
			List<Context> annotations)
	{
		if (annotations.isEmpty())
		{
		      getOptionFromUser(message, Report.OPTION_OK);
		}
		else
		{
			// prepare content
			JPanel content = new JPanel(new BorderLayout());
			content.add(BorderLayout.NORTH, new JLabel(msg));
			content.add(BorderLayout.CENTER, new JScrollPane(
					new ContextListWidget(gedcom, annotations)));

			showComponentToUser(content);
		}
	}

	/**
	 * Extract the Gedcom object from some kind of GenJ context object
	 * @param context  some context object
	 * @return  Gedcom object associated with context object
	 */
	private static Gedcom getGedcom(Object context)
	{
		if (context instanceof Gedcom)
			return (Gedcom)context;
		if (context instanceof Entity)
			return ((Entity)context).getGedcom();
		if (context instanceof Property)
			return ((Property)context).getGedcom();
		if (context instanceof Entity[])
			return ((Entity[])context)[0].getGedcom();
		if (context instanceof Property[])
			return ((Property[])context)[0].getGedcom();
		return null;
	}
}
