import java.util.Random;
import java.util.concurrent.Semaphore;

class Main {

    public static void main(String[] args) throws InterruptedException {
        final DiningPhilosophers dp = new DiningPhilosophers();

        Thread p1 = new Thread(() -> DiningPhilosophers.startPhilosoper(dp, 0));
        Thread p2 = new Thread(() -> DiningPhilosophers.startPhilosoper(dp, 1));
        Thread p3 = new Thread(() -> DiningPhilosophers.startPhilosoper(dp, 2));
        Thread p4 = new Thread(() -> DiningPhilosophers.startPhilosoper(dp, 3));
        Thread p5 = new Thread(() -> DiningPhilosophers.startPhilosoper(dp, 4));

        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();
        p5.join();
    }
}

class DiningPhilosophers {

    private static final Random random = new Random(System.currentTimeMillis());

    private final Object[] forks = new Object[5];
    private final Semaphore maxDiners = new Semaphore(4);

    public DiningPhilosophers() {
        for (int i = 0; i < 5; i++) {
            forks[i] = new Object();
        }
    }

    public void lifecycleOfPhilosopher(int id) throws InterruptedException {
        while (true) {
            contemplate();
            eat(id);
        }
    }

    void contemplate() throws InterruptedException {
        Thread.sleep(random.nextInt(50));
    }

    void eat(int id) throws InterruptedException {
        maxDiners.acquire();

        synchronized (forks[id]) {
            synchronized (forks[(id + 1) % 5]) {
                System.out.println("Philosopher " + id + " is eating");
                // Simulate eating
                Thread.sleep(new Random().nextInt(50));
            }
        }

        maxDiners.release();
    }

    static void startPhilosoper(DiningPhilosophers dp, int id) {
        try {
            dp.lifecycleOfPhilosopher(id);
        } catch (InterruptedException ie) {

        }
    }
}
