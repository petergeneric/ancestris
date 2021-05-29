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
package genj.gedcom;

/**
 * Interface for Listeners of changes in gedcom data
 */
public class GedcomListenerAdapter implements GedcomListener, GedcomMetaListener {
  
  @Override
  public void gedcomHeaderChanged(Gedcom gedcom) {
  }
  
  @Override
  public void gedcomWriteLockAcquired(Gedcom gedcom) {
  }
  
  @Override
  public void gedcomWriteLockReleased(Gedcom gedcom) {
  }
  
  @Override
  public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
  }
  
  @Override
  public void gedcomAfterUnitOfWork(Gedcom gedcom) {
  }

  @Override
  public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
  }

  @Override
  public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
  }
  
  @Override
  public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
  }
  
  @Override
  public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
  }
  
  @Override
  public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property removed) {
  }
  
} //GedcomListenerAdapter
