import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Main {

    public static void main(String[] args) throws Exception {
        final SemaphoreOnLock semaphore = new SemaphoreOnLock(1);

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("Thread t1 attempting to acquire: " + System.currentTimeMillis());
                semaphore.acquire();
                System.out.println("Semaphore acquired by t1: " + System.currentTimeMillis());

                Thread.sleep(5000);

                semaphore.release();
                System.out.println("Semaphore released by t1: " + System.currentTimeMillis());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("Thread t2 attempting to acquire: " + System.currentTimeMillis());
                semaphore.acquire();
                System.out.println("Semaphore acquired by t2: " + System.currentTimeMillis());

                Thread.sleep(1000);

                semaphore.release();
                System.out.println("Semaphore released by t2: " + System.currentTimeMillis());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        t1.start();
        Thread.sleep(500);
        t2.start();
        t1.join();
        t2.join();
    }
}

class SemaphoreOnLock {
    private final Lock lock = new ReentrantLock();
    // CONDITION PREDICATE: permitsAvailable (permits > 0)
    private final Condition permitsAvailable = lock.newCondition();
    private int permits;

    SemaphoreOnLock(int initialPermits) {
        lock.lock();
        try {
            permits = initialPermits;
        } finally {
            lock.unlock();
        }
    }

    // BLOCKS-UNTIL: permitsAvailable
    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            while (permits <= 0)
                permitsAvailable.await();
            --permits;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            ++permits;
            permitsAvailable.signal();
        } finally {
            lock.unlock();
        }
    }
}
