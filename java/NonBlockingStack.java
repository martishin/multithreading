import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class Main {
    public static void main(String[] args) throws Exception {

        NonblockingStack<Integer> stack = new NonblockingStack<>();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        int numThreads = 2;
        CyclicBarrier barrier = new CyclicBarrier(numThreads);

        int testValue = 51;

        try {
            for (int i = 0; i < numThreads; i++) {
                executorService.submit(() -> {
                    for (int i1 = 0; i1 < 10000; i1++) {
                        stack.push(testValue);
                    }

                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException ex) {
                        System.out.println("ignoring exception");
                        //ignore both exceptions
                    }

                    for (int i1 = 0; i1 < 1000; i1++) {
                        stack.pop();
                    }
                });
            }
        } finally {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        }

        System.out.println("Number of elements in the stack = " + stack.size());
    }
}

class StackNode<T> {
    private T item;
    private StackNode<T> next;

    public StackNode(T item) {
        this.item = item;
    }

    public StackNode<T> getNext() {
        return next;
    }

    public void setNext(StackNode<T> stackNode) {
        next = stackNode;
    }

    public T getItem() {
        return this.item;
    }
}

class NonblockingStack<T> {

    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicReference<StackNode<T>> top = new AtomicReference<>();

    public int size() {
        return count.get();
    }

    public void push(T newItem) {
        StackNode<T> oldTop;
        StackNode<T> newTop;
        do {
            oldTop = top.get();
            newTop = new StackNode<>(newItem);
            newTop.setNext(oldTop);
        } while (!top.compareAndSet(oldTop, newTop));

        count.incrementAndGet();
    }

    public T pop() {
        StackNode<T> oldTop;
        StackNode<T> newTop;

        do {
            oldTop = top.get();
            if (oldTop == null) return null;
            newTop = oldTop.getNext();
        } while (!top.compareAndSet(oldTop, newTop));

        count.decrementAndGet();
        return oldTop.getItem();
    }
}

//class CASBasedStack<T> {
//
//    private final SimulatedCompareAndSwap<StackNode<T>> simulatedCAS;
//
//    public CASBasedStack() {
//        simulatedCAS = new SimulatedCompareAndSwap<>(null);
//    }
//
//    public void push(T item) {
//        StackNode<T> oldHead;
//        StackNode<T> newHead;
//
//        do {
//            // retrieve the current value of top
//            oldHead = simulatedCAS.getValue();
//            // create a new StackNode for the passed-in item.
//            newHead = new StackNode<>(item);
//            // Adjust the pointer
//            newHead.setNext(oldHead);
//        } while (!simulatedCAS.compareAndSet(oldHead, newHead));
//        // attempt to atomically check and update
//    }
//
//    public T pop() {
//        StackNode<T> returnValue;
//        StackNode<T> newHead;
//
//        do {
//            // get the current top of the stack
//            returnValue = simulatedCAS.getValue();
//            // if the top is null then simply return null
//            if (returnValue == null) return null;
//            // compute the new top of stack
//            newHead = returnValue.getNext();
//        } while (!simulatedCAS.compareAndSet(returnValue, newHead));
//        // attempt to update the new top of stack
//
//        return returnValue.getItem();
//    }
//}

class SimulatedCompareAndSwap<T> {
    private T value;

    // constructor to initialize the value
    public SimulatedCompareAndSwap(T initValue) {
        value = initValue;
    }

    synchronized T getValue() {
        return value;
    }

    synchronized T compareAndSwap(T expectedValue, T newValue) {
        if (value == null) {
            if (expectedValue == null) {
                value = newValue;
            }
            return null;
        }

        if (value.equals(expectedValue)) {
            value = newValue;
            return expectedValue;
        }

        // return the current value
        return value;
    }

    // This method uses the compareAndSwap() method to indicate if the CAS
    // instruction completed successfully or not.
    synchronized boolean compareAndSet(T expectedValue, T newValue) {
        T returnVal = compareAndSwap(expectedValue, newValue);

        if (returnVal == null && expectedValue == null) return true;
        else if (returnVal == null && expectedValue != null) return false;
        else {
            return returnVal.equals(expectedValue);
        }
    }
}
