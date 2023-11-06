import java.util.concurrent.CountDownLatch;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        final ValueLatch<Integer> valueLatch = new ValueLatch<>();

        Thread getter = new Thread(() -> {
           try {}
        });

        Thread getter = new Thread(() -> {
            try {
                System.out.println("Getter waiting for value...");
                Integer value = valueLatch.getValue();
                System.out.println("Getter got value: " + value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread setter = new Thread(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("Setter setting value...");
                valueLatch.setValue(42);
                System.out.println("Setter set value to 42");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("Starting threads...");
        getter.start();
        setter.start();

        getter.join();
        setter.join();
    }
}

class ValueLatch<T> {
    private T value = null;
    private final CountDownLatch done = new CountDownLatch(1);

    public boolean isSet() {
        return (done.getCount() == 0);
    }

    public synchronized void setValue(T newValue) {
        if (!isSet()) {
            value = newValue;
            done.countDown();
        }
    }

    public T getValue() throws InterruptedException {
        done.await();
        synchronized (this) {
            return value;
        }
    }
}
