/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.variables.view;

import com.sun.istack.internal.Nullable;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;
import solver.explanations.Deduction;
import solver.explanations.Explanation;
import solver.explanations.VariableState;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.delta.NoDelta;

/**
 * "A view implements the same operations as a variable. A view stores a reference to a variable.
 * Invoking an operation on the view exectutes the appropriate operation on the view's varaible."
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations" <br/>
 * C. Shulte and G. Tack.<br/>
 * Eleventh International Conference on Principles and Practice of Constraint Programming
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public abstract class IntView extends AbstractVariable<IntDelta, IntView, IntView> implements IView<IntDelta, IntView>, IntVar {

    protected final IntVar var;

    protected IntDelta delta;

    protected boolean reactOnRemoval;

    protected HeuristicVal heuristicVal;

    public IntView(String name, IntVar var, Solver solver) {
        super(name, solver);
        this.var = var;
        this.delta = NoDelta.singleton;
        this.reactOnRemoval = false;
        makeList(this);
        this.var.subscribeView(this);
    }

    @Override
    public final int getTypeAndKind() {
        return Variable.VIEW + var.getTypeAndKind();
    }

    public IntVar getVariable() {
        return var;
    }

    @Override
    public int getDomainSize() {
        return var.getDomainSize();
    }

    @Override
    public boolean hasEnumeratedDomain() {
        return var.hasEnumeratedDomain();
    }

    @Override
    public boolean instantiated() {
        return var.instantiated();
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        this.heuristicVal = heuristicVal;
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return heuristicVal;
    }

    public IntDelta getDelta() {
        return delta;
    }


    @Override
    public void notifyMonitors(EventType event, ICause cause) throws ContradictionException {
        if ((modificationEvents & event.mask) != 0) {
            records.forEach(afterModification.set(this, event, cause));
        }
        notifyViews(event, cause);
    }

    @Override
    public void transformEvent(EventType evt, ICause cause) throws ContradictionException {
        notifyMonitors(evt, cause);
    }

    @Override
    public Explanation explain(VariableState what) {
        return var.explain(what);
    }

    @Override
    public Explanation explain(@Nullable Deduction d) {
        return var.explain(VariableState.DOM);
    }

    @Override
    public boolean reactOnPromotion() {
        return reactOnRemoval;
    }

    @Override
    public void contradiction(ICause cause, EventType event, String message) throws ContradictionException {
//        records.forEach(onContradiction.set(this, event, cause));
        solver.getEngine().fails(cause, this, message);
    }

    ///////////// SERVICES REQUIRED FROM CAUSE ////////////////////////////
    @Override
    public Constraint getConstraint() {
        return null;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return 0;
    }

    @Override
    public void incFail() {
    }

    @Override
    public long getFails() {
        return 0;
    }
}