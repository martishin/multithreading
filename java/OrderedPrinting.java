import java.util.concurrent.CountDownLatch;

class Main {
    public static void main(String[] args) {
        OrderedPrinting obj = new OrderedPrinting();

        OrderedPrintingThread t1 = new OrderedPrintingThread(obj, "first");
        OrderedPrintingThread t2 = new OrderedPrintingThread(obj, "second");
        OrderedPrintingThread t3 = new OrderedPrintingThread(obj, "third");

        t3.start();
        t2.start();
        t1.start();

    }
}

class OrderedPrinting {
    CountDownLatch latch1;
    CountDownLatch latch2;

    public OrderedPrinting() {
        latch1 = new CountDownLatch(1);
        latch2 = new CountDownLatch(1);
    }

    public void printFirst() throws InterruptedException {
        System.out.println("First");
        latch1.countDown();
    }

    public void printSecond() throws InterruptedException {
        latch1.await();
        System.out.println("Second");
        latch2.countDown();
    }

    public void printThird() throws InterruptedException {
        latch2.await();
        System.out.println("Third");
    }
}

class OrderedPrintingThread extends Thread {
    private OrderedPrinting obj;
    private String method;

    public OrderedPrintingThread(OrderedPrinting obj, String method) {
        this.method = method;
        this.obj = obj;
    }

    public void run() {
        if ("first".equals(method)) {
            try {
                obj.printFirst();
            } catch (InterruptedException e) {

            }
        } else if ("second".equals(method)) {
            try {
                obj.printSecond();
            } catch (InterruptedException e) {

            }
        } else if ("third".equals(method)) {
            try {
                obj.printThird();
            } catch (InterruptedException e) {

            }
        }
    }
}
