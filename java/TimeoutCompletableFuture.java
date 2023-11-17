import java.time.Duration;
import java.util.concurrent.*;

class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = TimeoutCompletableFuture.create(Duration.ofMillis(500));
        long start = System.nanoTime();
        future.get();
        long end = System.nanoTime();
        System.out.println("Future completed after delay? " + (Duration.ofNanos(end - start).toMillis() >= 500));

        CompletableFuture<Void> cancellableFuture = TimeoutCompletableFuture.create(Duration.ofSeconds(2));
        boolean cancelled = cancellableFuture.cancel(true);
        System.out.println("Future was cancelled: " + cancelled);
        System.out.println("Future is done: " + cancellableFuture.isDone());
        System.out.println("Future is cancelled: " + cancellableFuture.isCancelled());
    }
}

class TimeoutCompletableFuture<T> extends CompletableFuture<T> implements Runnable {
    final ScheduledFuture<?> timer;

    TimeoutCompletableFuture(Duration delay) {
        this(Executors.newSingleThreadScheduledExecutor(), delay);
    }

    TimeoutCompletableFuture(ScheduledExecutorService scheduledExecutorService, Duration delay) {
        this.timer = scheduledExecutorService.schedule(this, delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void run() {
        this.complete(null);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return timer.cancel(mayInterruptIfRunning) && super.cancel(mayInterruptIfRunning);
    }

    static <T> CompletableFuture<T> create(Duration timeout) {
        return new TimeoutCompletableFuture<>(timeout);
    }
}
