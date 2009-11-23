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
 *
 * $Revision$ $Author$ $Date$
 */
package genj.plugin.sosa;

import genj.option.OptionProvider;
import genj.option.PropertyOption;

import java.util.List;

/** 
 * Options for plugin package 
 */
public class SosaOptions extends OptionProvider {

	/** 
	 * THE instance of our _sosa_ options - we have to make 
	 * sure it's actually the SosaOptions - not gedcom options 
	 * we keep one instance of here 
	 */
	private static SosaOptions instance = new SosaOptions();

	/** Sosa extension flag */
	public boolean isExtendSosaIndexation=true;

	/** 
	 * callback - provide options during system init 
	 */
	public List getOptions() {

		// introspect for options 
		return PropertyOption.introspect(getInstance());
	}

	/** 
	 * accessor - singleton instance 
	 */
	public static SosaOptions getInstance() {
		return instance;
	}

} 
