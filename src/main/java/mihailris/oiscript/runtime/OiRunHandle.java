package mihailris.oiscript.runtime;

import mihailris.oiscript.Context;

public class OiRunHandle {
    public final Object lock = new Object();
    public Thread thread;
    public boolean finished;

    public long skip;
    public long wait;
    public Exception exception;

    private long lastSwitch;

    public void continueProc() {
        if (lastSwitch == 0) {
            continueProc(0);
        } else {
            long delta = System.currentTimeMillis() - lastSwitch;
            if (wait > 0 && delta < 1) {
                return;
            }
            continueProc(delta);
        }
    }

    public void continueProc(long dt) {
        lastSwitch = System.currentTimeMillis();
        if (finished) {
            throw new IllegalStateException("procedure is finished");
        }
        if (skip > 0) {
            skip--;
            return;
        }
        if (wait > 0){
            wait -= dt;
            if (wait > 0)
                return;
        }
        switchControl();
        if (exception != null) {
            throw new RuntimeException(exception);
        }
    }

    public void switchControl() {
        synchronized (lock) {
            lock.notifyAll();
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (outFunction != null) {
            outReturned = outFunction.execute(outContext, outArgs);
            outFunction = null;
            switchControl();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void skip(long frames) {
        skip = frames;
        switchControl();
    }

    public void waitDelay(long delay) {
        wait += delay;
        switchControl();
    }


    Function outFunction;
    Context outContext;
    Object[] outArgs;
    Object outReturned;

    public Object callOutside(Function function, Context context, Object[] args) {
        outFunction = function;
        outContext = context;
        outArgs = args;
        outReturned = null;
        switchControl();
        outContext = null;
        outArgs = null;
        return outReturned;
    }
}
