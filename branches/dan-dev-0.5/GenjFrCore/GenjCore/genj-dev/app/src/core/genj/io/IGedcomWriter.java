/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genj.io;

import genj.util.Trackable;

/**
 *
 * @author daniel.andre
 */
public interface IGedcomWriter extends Trackable {
  public void write() throws GedcomIOException;
}
