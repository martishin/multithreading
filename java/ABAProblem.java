import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicStampedReference;

class Main {

    private static final ConcurrentLinkedQueue<Node> availableNodes = new ConcurrentLinkedQueue<>();
    private static final AtomicStampedReference<Node> head = new AtomicStampedReference<>(null, 1);

    public static void main(String[] args) throws Exception {
        Node currHead = null;
        Node node = null;

        // nodes are inserted by the main thread with values
        // ranging from 0 to 9
        for (int i = 0; i < 10; i++) {
            node = new Node(i);
            node.next = currHead;
            int currStamp = head.getStamp();
            head.compareAndSet(currHead, node, currStamp, currStamp + 1);
            currHead = node;
        }

        System.out.println("Initial list : ");
        printNodes();

        // creating Thread1
        Thread thread1 = new Thread(() -> {

            // Thread1 reads-in the current head and its next node
            Node currentHead = head.getReference();
            int currHeadStamp = head.getStamp();
            Node nextHead = currentHead.next;

            System.out.println("Thread 1 sees head (with stamp " + currHeadStamp + ") = " + currentHead.val + " and head.next = " + nextHead.val);

            // sleep Thread1
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                // ignore
            }

            System.out.println("Thread1 about to compare and set");

            if (head.compareAndSet(currentHead, nextHead, currHeadStamp, currHeadStamp + 1)) {
                System.out.println("Thread1 successfully updated head. List looks as follows: ");
                printNodes();
            } else {
                System.out.println("CAS failed in Thread1");
            }
        });

        thread1.start();

        // set-up Thread2
        Thread thread2 = new Thread(() -> {

            // wait for Thread 1 to reach its sleep statement
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // ignore
            }

            // dequeue first five nodes from the list and place them
            // in the free nodes list
            Node currHead1 = null;
            int currStamp;
            for (int i = 0; i < 5; i++) {
                currHead1 = head.getReference();
                currStamp = head.getStamp();
                head.compareAndSet(currHead1, currHead1.next, currStamp, currStamp + 1);
                currHead1.val = -1; // set to -1 to denote the node is in recycle list
                currHead1.next = null;
                availableNodes.add(currHead1);
            }

            currHead1 = head.getReference();
            currStamp = head.getStamp();
            Node newHead = availableNodes.remove();
            newHead.val = 99; // set a new value
            newHead.next = currHead1;
            if (head.compareAndSet(currHead1, newHead, currStamp, currStamp + 1)) {
                System.out.println("Thread 2 successfully updates. List is as follows : ");
                printNodes();
            }
        });

        thread2.start();

        // wait for threads to exit
        thread1.join();
        thread2.join();

        System.out.println("List when main program exits is as follows ");
        printNodes();

    }

    // helper method to print the list
    static void printNodes() {
        Node currHead = head.getReference();
        int currStamp = head.getStamp();
        boolean start = true;
        while (currHead != null) {
            if (start) {
                start = false;
                System.out.print(currHead.val + " (head with stamp " + currStamp + ") -> ");
            } else {
                System.out.print(currHead.val + " -> ");
            }
            currHead = currHead.next;
        }
        System.out.println();
    }
}

class Node {
    public Node(int val) {
        this.val = val;
    }

    int val;
    Node next;
}
