class Main {

    public static void main(String[] args) throws Exception {

        final ReadWriteLock rwl = new ReadWriteLock();

        Thread t1 = new Thread(() -> {
            try {
                System.out.println("Attempting to acquire write lock in t1: " + System.currentTimeMillis());
                rwl.acquireWriteLock();
                System.out.println("Write lock acquired in t1: " + System.currentTimeMillis());

                // Simulates write lock being held for a while
                Thread.sleep(2000);

                rwl.releaseWriteLock();
                System.out.println("Write lock released in t1: " + System.currentTimeMillis());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                System.out.println("Attempting to acquire write lock in t2: " + System.currentTimeMillis());
                rwl.acquireWriteLock();
                System.out.println("Write lock acquired in t2: " + System.currentTimeMillis());

                rwl.releaseWriteLock();
                System.out.println("Write lock released in t2: " + System.currentTimeMillis());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread tReader1 = new Thread(() -> {
            try {
                rwl.acquireReadLock();
                System.out.println("Read lock acquired in tReader1: " + System.currentTimeMillis());

                Thread.sleep(2000);

                rwl.releaseReadLock();
                System.out.println("Read lock released in tReader1: " + System.currentTimeMillis());

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        Thread tReader2 = new Thread(() -> {
            try {
                rwl.acquireReadLock();
                System.out.println("Read lock acquired in tReader2: " + System.currentTimeMillis());

                rwl.releaseReadLock();
                System.out.println("Read lock released in tReader2: " + System.currentTimeMillis());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });

        tReader1.start();
        t1.start();
        Thread.sleep(3000);
        tReader2.start();
        Thread.sleep(1000);
        t2.start();
        tReader1.join();
        tReader2.join();
        t2.join();
    }
}

class ReadWriteLock {

    boolean isWriteLocked = false;
    int readers = 0;

    public synchronized void acquireReadLock() throws InterruptedException {

        while (isWriteLocked) {
            wait();
        }

        readers++;
    }

    public synchronized void releaseReadLock() {
        readers--;
        notify();
    }

    public synchronized void acquireWriteLock() throws InterruptedException {

        while (isWriteLocked || readers != 0) {
            wait();
        }

        isWriteLocked = true;
    }

    public synchronized void releaseWriteLock() {
        isWriteLocked = false;
        notifyAll();
    }
}
