import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class Main {
    public static void main(String args[]) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        Set<Singleton> singletonSet = new HashSet<>();

        Callable<Singleton> callableTask = () -> {
            return Singleton.getInstance();
        };

        Set<Future<Singleton>> futures = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            futures.add(executor.submit(callableTask));
        }

        for (Future<Singleton> future : futures) {
            singletonSet.add(future.get());
        }

        if (singletonSet.size() == 1) {
            System.out.println("Singleton works as expected. Only one instance was created.");
        } else {
            System.out.println("Multiple instances were created. Singleton failed.");
        }

        executor.shutdown();
    }
}

public class Singleton {
    private static Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
	if (instance == null) {
	    synchronized (Singleton.class) {
	        if (instance == null) {
		    instance = new Singleton();
		}
            }
	}
	return instance;
    }
}
