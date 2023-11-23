import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Main {
    public static void main(String args[]) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Set<SupermanHolder> singletonSet = new HashSet<>();

        Callable<SupermanHolder> callableTask = SupermanHolder::getInstance;

        Set<Future<SupermanHolder>> futures = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(callableTask));
        }

        for (Future<SupermanHolder> future : futures) {
            singletonSet.add(future.get());
        }

        if (singletonSet.size() == 1) {
            System.out.println("Superman works as expected. Only one instance was created.");
        } else {
            System.out.println("Multiple instances were created. Superman failed.");
        }

        SupermanHolder.getInstance().fly();

        executor.shutdown();
    }
}

class Superman {
    private static volatile Superman superman;

    private Superman() {

    }

    public static Superman getInstance() {

        if (superman == null) {
            synchronized (Superman.class) {

                if (superman == null) {
                    superman = new Superman();
                }
            }
        }

        return superman;
    }

    public void fly() {
        System.out.println("I am Superman & I can fly !");
    }
}

class SupermanEager {
    private static SupermanEager superman = new SupermanEager();

    private SupermanEager() {
    }

    public static SupermanEager getInstance() {
        return superman;
    }

    public void fly() {
        System.out.println("I am flyyyyinggggg ...");
    }
}

class SupermanHolder {
    private SupermanHolder() {
    }

    private static class Holder {
        private static final SupermanHolder superman = new SupermanHolder();
    }

    public static SupermanHolder getInstance() {
        return Holder.superman;
    }

    public void fly() {
        System.out.println("I can fly ...");
    }
}
