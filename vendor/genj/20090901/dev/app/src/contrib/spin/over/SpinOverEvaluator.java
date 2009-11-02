/**
 * Spin - transparent threading solution for non-freezing Swing applications.
 * Copyright (C) 2002 Sven Meier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package spin.over;

import javax.swing.SwingUtilities;

import spin.Invocation;
import spin.Evaluator;

/**
 * An evaluator for spin-over, i.e. all invocations are evaluated on the EDT.
 */
public class SpinOverEvaluator extends Evaluator {

    private static boolean defaultWait = true;
    
    private boolean wait;

    /**
     * Create an evaluator for spin-over using the default wait setting.
     * 
     * @see #setDefaultWait(boolean)
     */
    public SpinOverEvaluator() {
        this(defaultWait);
    }

    /**
     * Create an evaluator for spin-over.
     * 
     * @param wait  should the invocation wait for the evaluation to complete
     */
    public SpinOverEvaluator(boolean wait) {
        this.wait = wait;
    }

    /**
     * Spin the given invocation on the EDT.
     * 
     * @param invocation
     *            invocation to spin-over
     */
    public final void evaluate(final Invocation invocation) throws Throwable {

        if (SwingUtilities.isEventDispatchThread()) {
            invocation.evaluate();
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    invocation.evaluate();
                }
            };
            if (wait) {
                SwingUtilities.invokeAndWait(runnable);
            } else {
                if (invocation.getMethod().getReturnType() != Void.TYPE) {
                    onInvokeLaterNonVoidReturnType(invocation);
                }
                SwingUtilities.invokeLater(runnable);
            }
        }
    }

    protected void onInvokeLaterNonVoidReturnType(Invocation invocation) throws IllegalArgumentException {
        throw new IllegalArgumentException("invokeLater with non-void return type");
    }

    public static boolean getDefaultWait() {
        return defaultWait;
    }

    public static void setDefaultWait(boolean wait) {
        defaultWait = wait;
    }
}