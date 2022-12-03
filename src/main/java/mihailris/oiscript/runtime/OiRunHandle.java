package mihailris.oiscript.runtime;

import mihailris.oiscript.exceptions.RuntimeInterruptedException;

public class OiRunHandle {
    public final Object lock = new Object();
    public Thread thread;
    public boolean finished;

    public long skip;
    public long wait;

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
    }

    public void switchControl() {
        synchronized (lock) {
            lock.notifyAll();
            try {
                lock.wait();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
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
}
