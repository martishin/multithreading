import java.util.concurrent.atomic.AtomicReference;

class Main {
    public static void main(String[] args) throws InterruptedException {
        var stack = new ConcurrentStack<Integer>();

        var t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                stack.push(i);
            }
        });

        var t2 = new Thread(() -> {
            for (int i = 1000; i < 2000; i++) {
                stack.push(i);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        int counter = 0;
        while (!stack.isEmpty()) {
            stack.pop();
            counter++;
        }

        if (counter == 2000) {
            System.out.println("ConcurrentStack is working correctly");
        } else {
            System.out.println("ConcurrentStack is not working correctly");
        }
    }
}

class ConcurrentStack<E> {
    AtomicReference<Node<E>> top = new AtomicReference<>();

    public void push(E item) {
        Node<E> newHead = new Node<>(item);
        Node<E> oldHead;
        do {
            oldHead = top.get();
            newHead.next = oldHead;
        } while (!top.compareAndSet(oldHead, newHead));
    }

    public E pop() {
        Node<E> oldHead;
        Node<E> newHead;
        do {
            oldHead = top.get();
            if (oldHead == null) {
                return null;
            }
            newHead = oldHead.next;
        } while (!top.compareAndSet(oldHead, newHead));
        return oldHead.item;
    }

    public boolean isEmpty() {
        return top.get() == null;
    }

    private static class Node<E> {
        public final E item;
        public Node<E> next;

        public Node(E item) {
            this.item = item;
        }
    }
}
