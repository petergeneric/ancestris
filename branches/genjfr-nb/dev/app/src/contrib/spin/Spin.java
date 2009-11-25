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

import spin.over.SpinOverEvaluator;

/**
 * <p>
 * <em>Spin</em> offers a transparent threading solution for developing
 * non-freezing Swing applications.
 * </p>
 * <p>
 * Let <code>bean</code> be a reference to a non-visual (possibly
 * multithreaded) bean implementing the interface <code>Bean</code> whose
 * methods have to be called by a Swing component. You can avoid any freezing by
 * using one line of code:
 * 
 * <pre>
 * bean = (Bean)Spin.off(bean);
 * </pre>
 * 
 * Now each method call on <code>bean</code> is executed on a separate thread,
 * while the EDT is continuing to dispatch events. All return values or
 * exceptions are handled by <em>Spin</em> and transparently returned to the
 * calling method.
 * </p>
 * <p>
 * For calls from other threads than the EDT to your Swing component you can use
 * the following (being <ode>XYListener</code> any interface your component
 * implements):
 * 
 * <pre>
 *     bean.addXYListener((XYListener)Spin.over(component); 
 * </pre>
 * 
 * Now all required updates to your component (and/or its model) are
 * transparently excuted on the EDT.
 * </p>
 * 
 * @see #off(Object)
 * @see #over(Object)
 * @see spin.ProxyFactory
 * @see spin.off.SpinOffEvaluator
 * @see spin.over.SpinOverEvaluator
 */
public class Spin {

    private static ProxyFactory defaultProxyFactory = new JDKProxyFactory();

    private static Evaluator defaultOverEvaluator = new SpinOverEvaluator();

    private Object proxy;

    /**
     * Create a <em>Spin</em> wrapper for the given object.
     * 
     * @param object    object to wrap
     * @param evaluator evaluator of invocations on the given object
     */
    public Spin(Object object, Evaluator evaluator) {
        this(object, defaultProxyFactory, evaluator);
    }

    /**
     * Create a <em>Spin</em> wrapper for the given object.
     * 
     * @param object        object to wrap
     * @param proxyFactory  factory for a proxy 
     * @param evaluator     evaluator of invocations on the given object
     */
    public Spin(Object object, ProxyFactory proxyFactory, Evaluator evaluator) {
        if (object == null) {
            throw new IllegalArgumentException("object must not be null");
        }
        if (proxyFactory == null) {
            throw new IllegalArgumentException("proxyFactory must not be null");
        }
        if (evaluator == null) {
            throw new IllegalArgumentException("evaluator must not be null");
        }

        proxy = proxyFactory.createProxy(object, evaluator);
    }

    /**
     * Get a proxy for the wrapped object. <br>
     * The returned object can safely be casted to any interface the wrapped
     * object implements.
     * 
     * @return the new proxy
     */
    public Object getProxy() {
        return proxy;
    }

    /**
     * Convenience method to spin-over the given object with Swing. <br>
     * The returned object can safely be casted to any interface the given
     * object implements.
     * 
     * @param object
     *            the object to spin-over
     * @return proxy for the given object
     * @see #setDefaultProxyFactory(ProxyFactory)
     * @see #setDefaultOverEvaluator(Evaluator)
     */
    public static Object over(Object object) {
        return new Spin(object, defaultProxyFactory, defaultOverEvaluator).getProxy();
    }
}