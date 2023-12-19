import java.util.concurrent.atomic.AtomicReference;

class Main {
    public static void main(String[] args) throws InterruptedException {
        var stack = new ConcurrentLinkedQueue<Integer>();

        var t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                stack.offer(i);
            }
        });

        var t2 = new Thread(() -> {
            for (int i = 1000; i < 2000; i++) {
                stack.offer(i);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int counter = 0;
        while (!stack.isEmpty()) {
            stack.poll();
            counter++;
        }

        if (counter == 2000) {
            System.out.println("ConcurrentLinkedQueue is working correctly");
        } else {
            System.out.println("ConcurrentLinkedQueue is not working correctly");
        }
    }
}

class ConcurrentLinkedQueue<E> {
    private static class Node<E> {
        final E item;
        final AtomicReference<Node<E>> next;

        public Node(E item, Node<E> next) {
            this.item = item;
            this.next = new AtomicReference<>(next);
        }
    }

    private final Node<E> dummy = new Node<>(null, null);
    private final AtomicReference<Node<E>> head
        = new AtomicReference<>(dummy);
    private final AtomicReference<Node<E>> tail
        = new AtomicReference<>(dummy);

    public boolean offer(E item) {
        Node<E> newNode = new Node<>(item, null);
        while (true) {
            Node<E> curTail = tail.get();
            Node<E> tailNext = curTail.next.get();
            if (curTail == tail.get()) {
                if (tailNext != null) {
                    // Queue in intermediate state, advance tail
                    tail.compareAndSet(curTail, tailNext);
                } else {
                    // In quiescent state, try inserting new node
                    if (curTail.next.compareAndSet(null, newNode)) {
                        // Insertion succeeded, try advancing tail
                        tail.compareAndSet(curTail, newNode);
                        return true;
                    }
                }
            }
        }
    }

    public E poll() {
        while (true) {
            Node<E> oldHead = head.get();
            Node<E> oldTail = tail.get();
            Node<E> headNext = oldHead.next.get();
            if (oldHead == head.get()) {
                if (oldHead == oldTail) {
                    if (headNext == null) {
                        // Queue is empty
                        return null;
                    }
                    tail.compareAndSet(oldTail, headNext);
                } else {
                    E item = headNext.item;
                    if (head.compareAndSet(oldHead, headNext)) {
                        return item;
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        while (true) {
            Node<E> oldHead = head.get();
            Node<E> oldTail = tail.get();
            Node<E> headNext = oldHead.next.get();
            if (oldHead == head.get()) {
                if (oldHead == oldTail && headNext == null) {
                    // Queue is empty
                    return true;
                } else {
                    // Queue is not empty
                    return false;
                }
            }
        }
    }
}
