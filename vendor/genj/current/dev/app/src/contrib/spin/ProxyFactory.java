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
package spin;

import java.lang.reflect.Method;

/**
 * A factory of proxies which intercept invocations, using Evaluators
 * to evaluate them.
 * 
 * @see spin.Evaluator
 */
public abstract class ProxyFactory {

    /**
     * The equals method of class <code>object</code>.
     */
    private static final Method equalsMethod;
    static {
        try {
          // NM 20070301 use getMethod() since getDeclaredMethod() doesn't work in Applet
          equalsMethod = Object.class.getMethod("equals", new Class[]{Object.class});
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    /**
     * Create a proxy for the given object that evaluates invocations 
     * with the given evaluator.
     * 
     * @param object    object to create proxy for
     * @param evaluator evaluator to evaluate invocations with
     * @return          new proxy
     */
    public abstract Object createProxy(Object object, Evaluator evaluator);
    
    /**
     * Test if the given object is a proxy created by this factory.
     * 
     * @param object    object to test
     * @return          <code>true</code> if given object is a <em>Spin</em> proxy,
     *                  <code>false</code> otherwise
     */
    public abstract boolean isProxy(Object object);

    /**
     * Test if the given proxies of this factory are intercepting the same object.
     * 
     * @param proxy1    first proxy
     * @param proxy2    second proxy
     * @return          true if both proxies are intercepting the same object
     */
    protected abstract boolean areProxyEqual(Object proxy1, Object proxy2);
    
    /**
     * Evaluate the given invocation with the given evaluator.
     * 
     * @param evaluator     evaluator to evaluate with
     * @param proxy         proxy that intcepted the invocation
     * @param invocation    the invocation to evaluate
     * @return              result of evaluation
     * @throws Throwable
     */
    protected Object evaluteInvocation(Evaluator evaluator, Object proxy, Invocation invocation) throws Throwable {
        if (equalsMethod.equals(invocation.getMethod())) {
            return new Boolean(isProxy(invocation.getArguments()[0]) &&
                               areProxyEqual(proxy, invocation.getArguments()[0]));
        } else {
            evaluator.evaluate(invocation);
          
            return invocation.resultOrThrow();
        }
    }
}