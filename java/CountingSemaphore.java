import java.util.ArrayList;
import java.util.List;

class Main {
    public static void main(String[] args) throws Exception {

        final var semaphore = new CountingSemaphore(1);

        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            int finalI = i;
            threads.add(new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("Ping " + finalI);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            threads.add(new Thread(() -> {
                try {
                    semaphore.release();
                    System.out.println("Pong " + finalI);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
        }

        for (var t : threads) {
            t.start();
        }

        for (var t : threads) {
            t.join();
        }

        System.out.println(semaphore.getUsedPermits());
    }
}

public class CountingSemaphore {

    private int usedPermits = 0;

    public int getUsedPermits() {
        return usedPermits;
    }

    private final int maxCount;

    public CountingSemaphore(int count) {
        this.maxCount = count;
    }

    public CountingSemaphore(int count, int initialPermits) {
        this.maxCount = count;
        this.usedPermits = this.maxCount - initialPermits;
    }

    public synchronized void acquire() throws InterruptedException {

        while (usedPermits == maxCount)
            wait();

        notify();
        usedPermits++;
    }

    public synchronized void release() throws InterruptedException {

        while (usedPermits == 0)
            wait();

        usedPermits--;
        notify();
    }
}
