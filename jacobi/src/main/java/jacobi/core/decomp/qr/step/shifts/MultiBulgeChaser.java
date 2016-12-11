/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jacobi.core.decomp.qr.step.shifts;

import jacobi.core.decomp.qr.step.shifts.SingleBulgeChaser.Arguments;
import jacobi.core.util.Threads;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 
 * @author Y.K. Chan
 */
public class MultiBulgeChaser {

    public MultiBulgeChaser() {
        this(new SingleBulgeChaser());
    }

    public MultiBulgeChaser(SingleBulgeChaser chaser) {
        this.chaser = chaser;
    }
    
    public List<Batch> compute(Arguments args, List<DoubleShift> shifts) {        
        return Threads.invokeAll(this.createWorkers(args, shifts));
    }
    
    protected List<Supplier<Batch>> createWorkers(Arguments args, List<DoubleShift> shifts) {
        State state = new State(shifts.size());
        List<Supplier<Batch>> workers = new ArrayList<>();
        for(int i = 0; i < shifts.size(); i++){
            workers.add(this.createWorker(i, args, shifts.get(i), state));
        }
        return workers;
    }
    
    protected Supplier<Batch> createWorker(int id, Arguments args, DoubleShift shift, State state) {
        int skip = (state.getWorkerCount() - id - 1) * 3;
        return () -> {
            for(int i = 0; i < skip; i++){
                state.reach();
            }
            Batch batch = this.chaser.compute(args, shift, (i) -> state.reach());
            while(!state.isDone()){
                if(id == 0){
                    state.complete();
                }
                state.reach();
            }
            return batch;
        };
    }

    private SingleBulgeChaser chaser;
    
    protected static class State {

        public State(int numWorkers) {
            this.barrier = new CyclicBarrier(numWorkers);
            this.done = new AtomicBoolean(false);
        }
        
        public int getWorkerCount() {
            return this.barrier.getParties();
        }
        
        public void reach() {
            try {
                this.barrier.await();
            } catch (InterruptedException | BrokenBarrierException ex) {
                throw new IllegalStateException(ex);
            }
        }
        
        public boolean isDone() {
            return this.done.get();
        }
        
        public void complete() {
            this.done.set(true);
        }
        
        private CyclicBarrier barrier;
        private AtomicBoolean done;
    }
}
