/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2011 Ancestris
 *
 * Author: Daniel Andre (daniel@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package genj.app;

import genj.util.Trackable;

/**
 * Progress Callback
 */
public interface ProgressListener {

  /**
   * notificaton that a long running process has started
   * @param process
   */
  public void processStarted(Trackable process);

  /**
   * notificaton that a long running process has finished
   * @param process
   */
  public void processStopped(Trackable process);
}
