import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Main {
    public static void main(String[] args) throws InterruptedException {
        final DeferredCallbackExecutor deferredCallbackExecutor = new DeferredCallbackExecutor();

        Thread service = new Thread(() -> {
            try {
                deferredCallbackExecutor.start();
            } catch (InterruptedException ie) {
            }
        });

        service.start();

        Thread lateThread = new Thread(() -> {
            DeferredCallbackExecutor.CallBack cb = new DeferredCallbackExecutor.CallBack(8, "Hello this is the callback submitted first");
            deferredCallbackExecutor.registerCallback(cb);
        });
        lateThread.start();

        Thread.sleep(3000);

        Thread earlyThread = new Thread(() -> {
            DeferredCallbackExecutor.CallBack cb = new DeferredCallbackExecutor.CallBack(1, "Hello this is callback sumbitted second");
            deferredCallbackExecutor.registerCallback(cb);
        });
        earlyThread.start();

        lateThread.join();
        earlyThread.join();
    }
}

class DeferredCallbackExecutor {
    Queue<CallBack> q = new PriorityQueue<>((o1, o2) -> (int) (o1.executeAt - o2.executeAt));
    ReentrantLock lock = new ReentrantLock();
    Condition newCallbackArrived = lock.newCondition();

    private long findSleepDuration() {
        long currentTime = System.currentTimeMillis();
        return q.peek().executeAt - currentTime;
    }

    public void start() throws InterruptedException {
        long sleepFor = 0;

        while (true) {

            lock.lock();

            while (q.isEmpty()) {
                newCallbackArrived.await();
            }

            while (!q.isEmpty()) {
                sleepFor = findSleepDuration();

                if (sleepFor <= 0)
                    break;

                newCallbackArrived.await(sleepFor, TimeUnit.MILLISECONDS);
            }

            CallBack cb = q.poll();
            System.out.println(
                "Executed at " + System.currentTimeMillis() / 1000 + " required at " + cb.executeAt / 1000
                    + ": message:" + cb.message);

            lock.unlock();
        }
    }

    public void registerCallback(CallBack callBack) {
        lock.lock();
        q.add(callBack);
        newCallbackArrived.signal();
        lock.unlock();
    }

    static class CallBack {
        long executeAt;
        String message;

        public CallBack(long executeAfter, String message) {
            this.executeAt = System.currentTimeMillis() + executeAfter * 1000;
            this.message = message;
        }
    }
}
