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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A single invocation on a <em>Spin</em> proxy handled by {@link Evaluator}s.
 */
public class Invocation {

    /**
     * The object this invocation is evaluated on.
     */
    private Object object;

    /**
     * The method to invoce.
     */
    private Method method;

    /**
     * The arguments of the method to invoce.
     */
    private Object[] args;

    /**
     * Was this invocation evaluated.
     */
    private boolean evaluated;

    /**
     * An optional throwable catched in evaluation of this invocation.
     */
    private Throwable throwable;

    /**
     * The result of this invocation.
     */
    private Object result;

    /**
     * Create an invocation of the given method on the given object with
     * the given arguments.
     * 
     * @param object    object to invoke method on
     * @param method    method to invoke
     * @param args      arguments for the method invocation
     */
    public Invocation(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this.args   = args;
    }
    
    /**
     * Set the object this invocation is evaluated on.
     * 
     * @param object  object to evaluate on
     */
    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * Get the object this invocation is evaluated on.
     * 
     * @return the object this invocation is evaluated on
     */
    public Object getObject() {
        return object;
    }

    /**
     * Set the method to invoke.
     * 
     * @param method
     *            method to invoke
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Get the invoked method.
     * 
     * @return the invoked method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Set the arguments for the invoked method.
     * 
     * @param args
     *            the arguments for the invoked method
     */
    public void setArguments(Object[] args) {
        this.args = args;
    }

    /**
     * Get the arguments for the invoked method.
     * 
     * @return the arguments for the invoked method
     */
    public Object[] getArguments() {
        return args;
    }

    /**
     * Get the result of evaluation
     * 
     * @return  the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Set the result of evaluation
     * 
     * @param result    the result
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * Get the throwable thrown on evaluation.
     * 
     * @return  the throwable
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * Set the throwable thrown on evaluation.
     * 
     * @param throwable the throwable
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * Evaluate the return value (or a possibly thrown <code>Throwable</code>)
     * by invoking to method with the arguments on the wrapped object.
     */
    public void evaluate() {
        if (evaluated) {
            throw new IllegalStateException("already evaluated");
        }

        try {
            result = method.invoke(object, args);
        } catch (InvocationTargetException ex) {
            this.throwable = ex.getTargetException();
        } catch (Throwable throwable) {
            this.throwable = throwable;
        }

        evaluated = true;
    }

    /**
     * Test if this invocation is already evaluated.
     * 
     * @return <code>true</code> if evaluation has finished
     */
    public boolean isEvaluated() {
        return evaluated;
    }

    /**
     * Get the result or throwable of this invocation's evaluation.
     * 
     * @return result       result of evaluation
     * @throws Throwable    throwable of evaluation
     */
    public Object resultOrThrow() throws Throwable {
        if (throwable != null) {
            throw throwable;
        } else {
            return result;
        }
    }
}