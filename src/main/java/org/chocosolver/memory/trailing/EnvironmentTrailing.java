/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.memory.trailing;


import org.chocosolver.memory.*;
import org.chocosolver.memory.structure.IOperation;
import org.chocosolver.memory.trailing.trail.*;
import org.chocosolver.memory.trailing.trail.flatten.*;

/**
 * The root class for managing memory and sessions.
 * <p/>
 * A environment is associated to each problem.
 * It is responsible for managing backtrackable data.
 */
public class EnvironmentTrailing extends AbstractEnvironment {

    /**
     * The maximum numbers of worlds that a
     * {@link org.chocosolver.memory.IStorage} can handle.
     */
    public static final int NBWORLDS = 128;

    /**
     * The maximum numbers of updates that a
     * {@link org.chocosolver.memory.IStorage} can handle.
     */
    public static final int NBUPATES = 256;

    /**
     * The load factor to update {@link org.chocosolver.memory.IStorage}.
     */
    public static final double LOADFACTOR = 2;

    //Contains all the {@link IStorage} trails for
    // storing different kinds of data.

    private IStoredIntTrail intTrail;
    private IStoredBoolTrail boolTrail;
    private IStoredLongTrail longTrail;
    private IStoredDoubleTrail doubleTrail;
    private IOperationTrail operationTrail;
    private StoredIntVectorTrail intVectorTrail;
    private StoredDoubleVectorTrail doubleVectorTrail;

    /**
     * Contains all the {@link org.chocosolver.memory.IStorage} trails for
     * storing different kinds of data.
     */
    private IStorage[] trails = new IStorage[0];
    private int trailSize = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPush() {
        timestamp++;
        //code optim.: replace loop by enumeration
        final int wi = currentWorld + 1;
        for (int i = 0; i < trailSize; i++) {
            trails[i].worldPush(wi);
        }
        currentWorld++;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void worldPop() {
        timestamp++;
        //code optim.: replace loop by enumeration
        final int wi = currentWorld;
        for (int i = trailSize - 1; i >= 0; i--) {
            trails[i].worldPop(wi);
        }
        currentWorld--;
        assert currentWorld>=0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void worldCommit() {
        //code optim.: replace loop by enumeration;
        if (currentWorld == 0) {
            throw new IllegalStateException("Commit in world 0?");
        }
        final int wi = currentWorld;
        for (int i = trailSize; i >= 0; i--) {
            trails[i].worldCommit(wi);
        }
        currentWorld--;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt() {
        return makeInt(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateInt makeInt(final int initialValue) {
        return new StoredInt(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateBool makeBool(final boolean initialValue) {
        return new StoredBool(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateIntVector makeIntVector(final int size, final int initialValue) {
        return new StoredIntVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDoubleVector makeDoubleVector(final int size, final double initialValue) {
        return new StoredDoubleVector(this, size, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat() {
        return makeFloat(Double.NaN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateDouble makeFloat(final double initialValue) {
        return new StoredDouble(this, initialValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong() {
        return makeLong(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IStateLong makeLong(final long init) {
        return new StoredLong(this, init);
    }


    private void increaseTrail() {// TODO check resizing
        IStorage[] tmp = trails;
        trails = new IStorage[tmp.length + 1];
        System.arraycopy(tmp, 0, trails, 0, tmp.length);
    }

    public void setIntTrail(IStoredIntTrail itrail){
        if(intTrail == null) {
            increaseTrail();
            trails[trailSize++] = intTrail = itrail;
        }else{
            throw new UnsupportedOperationException("A trail has already been declared.");
        }
    }

    public IStoredIntTrail getIntTrail() {
        if (intTrail == null) {
            setIntTrail(new StoredIntTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        }
        return intTrail;
    }


    public void setLongTrail(IStoredLongTrail ltrail){
        if(longTrail == null) {
            increaseTrail();
            trails[trailSize++] = longTrail = ltrail;
        }else{
            throw new UnsupportedOperationException("A trail has already been declared.");
        }
    }

    public IStoredLongTrail getLongTrail() {
        if (longTrail == null) {
            setLongTrail(new StoredLongTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        }
        return longTrail;
    }

    public void setBoolTrail(IStoredBoolTrail btrail){
        if(boolTrail == null) {
            increaseTrail();
            trails[trailSize++] = boolTrail = btrail;
        }else{
            throw new UnsupportedOperationException("A trail has already been declared.");
        }
    }

    public IStoredBoolTrail getBoolTrail() {
        if (boolTrail == null) {
            setBoolTrail(new StoredBoolTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        }
        return boolTrail;
    }

    public void setDoubleTrail(IStoredDoubleTrail dtrail){
        if(doubleTrail == null) {
            increaseTrail();
            trails[trailSize++] = doubleTrail = dtrail;
        }else{
            throw new UnsupportedOperationException("A trail has already been declared.");
        }
    }

    public IStoredDoubleTrail getDoubleTrail() {
        if (doubleTrail == null) {
            setDoubleTrail(new StoredDoubleTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        }
        return doubleTrail;
    }

    public void setOperationTrail(IOperationTrail otrail){
        if(operationTrail == null) {
            increaseTrail();
            trails[trailSize++] = operationTrail = otrail;
        }else{
            throw new UnsupportedOperationException("A trail has already been declared.");
        }
    }

    public IOperationTrail getOperationTrail() {
        if (operationTrail == null) {
            setOperationTrail(new OperationTrail(NBUPATES, NBWORLDS, LOADFACTOR));
        }
        return operationTrail;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SPECIFIC DATA STRUCTURES                                                                                       //
    // NOTE: this data structures should not be used...
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public StoredIntVectorTrail getIntVectorTrail() {
        if (intVectorTrail == null) {
            increaseTrail();
            trails[trailSize++] = intVectorTrail = new StoredIntVectorTrail(this, NBUPATES, NBWORLDS, LOADFACTOR);
        }
        return intVectorTrail;
    }

    public StoredDoubleVectorTrail getDoubleVectorTrail() {
        if (doubleVectorTrail == null) {
            increaseTrail();
            trails[trailSize++] = doubleVectorTrail = new StoredDoubleVectorTrail(this, NBUPATES, NBWORLDS, LOADFACTOR);
        }
        return doubleVectorTrail;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void save(IOperation oldValue) {
        getOperationTrail().savePreviousState(oldValue);
    }
}

