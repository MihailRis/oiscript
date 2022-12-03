package mihailris.oiscript;

public class TestBed {
    public static void main(String[] args) throws InterruptedException {
        final Object lock = new Object();
        Thread task = new Thread(interrubleRunnable(() -> {
            System.out.println("T: 1");
            Thread.sleep(500);
            synchronized (lock){
                lock.notifyAll();
                lock.wait();
            }
            System.out.println("T: 2");
            Thread.sleep(500);
            synchronized (lock){
                lock.notifyAll();
                lock.wait();
            }
            System.out.println("T: end");
            synchronized (lock){
                lock.notifyAll();
            }
        }));
        System.out.println("M: 1");
        Thread.sleep(500);
        synchronized (lock) {
            task.start();
            lock.wait();
        }

        System.out.println("M: 2");
        Thread.sleep(500);
        synchronized (lock) {
            lock.notifyAll();
            lock.wait();
        };
        System.out.println("M: end");
        Thread.sleep(500);
        synchronized (lock) {
            lock.notifyAll();
            lock.wait();
        };
    }

    public interface InterrableRunnable {
        void run() throws InterruptedException;
    }

    public static Runnable interrubleRunnable(InterrableRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}
