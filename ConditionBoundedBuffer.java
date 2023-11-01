import java.util.concurrent.locks.*;

class Main {
    public static void main(String[] args) throws InterruptedException {
        final ConditionBoundedBuffer<Integer> buffer = new ConditionBoundedBuffer<>();

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 50; i++) {
                    buffer.put(i);
                    System.out.println("Produced " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer1 = new Thread(() -> {
            try {
                for (int i = 0; i < 25; i++) {
                    System.out.println("Consumer 1 consumed " + buffer.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer2 = new Thread(() -> {
            try {
                for (int i = 0; i < 25; i++) {
                    System.out.println("Consumer 2 consumed " + buffer.take());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        Thread.sleep(1000); // Let the producer put some items in the buffer
        consumer1.start();
        consumer2.start();

        producer.join();
        consumer1.join();
        consumer2.join();
    }
}

class ConditionBoundedBuffer<T> {
    protected final Lock lock = new ReentrantLock();
    // CONDITION PREDICATE: notFull (count < items.length)
    private final Condition notFull = lock.newCondition();
    // CONDITION PREDICATE: notEmpty (count > 0)
    private final Condition notEmpty = lock.newCondition();
    private static final int BUFFER_SIZE = 100;
    private final T[] items = (T[]) new Object[BUFFER_SIZE];
    private int tail, head, count;

    // BLOCKS-UNTIL: notFull
    public void put(T x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length)
                notFull.await();
            items[tail] = x;
            if (++tail == items.length)
                tail = 0;
            ++count;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // BLOCKS-UNTIL: notEmpty
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0)
                notEmpty.await();
            T x = items[head];
            items[head] = null;
            if (++head == items.length)
                head = 0;
            --count;
            notFull.signal();
            return x;
        } finally {
            lock.unlock();
        }
    }
}
