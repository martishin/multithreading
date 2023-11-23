import java.util.Random;

class Main {

    private static final int SIZE = 25;
    private static final Random random = new Random(System.currentTimeMillis());
    private static final int[] input = new int[SIZE];

    static private void createTestData() {
        for (int i = 0; i < SIZE; i++) {
            input[i] = random.nextInt(10000);
        }
    }

    static private void printArray() {
        System.out.println();
        for (int i = 0; i < Main.input.length; i++)
            System.out.print(" " + Main.input[i] + " ");
        System.out.println();
    }

    public static void main(String[] args) {
        createTestData();

        System.out.println("Unsorted Array");
        printArray();
        long start = System.currentTimeMillis();
        (new MultiThreadedMergeSort()).mergeSort(0, input.length - 1, input);
        long end = System.currentTimeMillis();
        System.out.println("\n\nTime taken to sort = " + (end - start) + " milliseconds");
        System.out.println("Sorted Array");
        printArray();
    }
}

class MultiThreadedMergeSort {

    private static final int SIZE = 25;
    private final int[] input = new int[SIZE];
    private final int[] scratch = new int[SIZE];

    void mergeSort(final int start, final int end, final int[] input) {

        if (start == end) {
            return;
        }

        final int mid = start + ((end - start) / 2);

        // sort first half
        Thread worker1 = new Thread(() -> mergeSort(start, mid, input));

        // sort second half
        Thread worker2 = new Thread(() -> mergeSort(mid + 1, end, input));

        // start the threads
        worker1.start();
        worker2.start();

        try {
            worker1.join();
            worker2.join();
        } catch (InterruptedException ie) {
            // swallow
        }

        // merge the two sorted arrays
        int i = start;
        int j = mid + 1;
        int k;

        for (k = start; k <= end; k++) {
            scratch[k] = input[k];
        }

        k = start;
        while (k <= end) {

            if (i <= mid && j <= end) {
                input[k] = Math.min(scratch[i], scratch[j]);

                if (input[k] == scratch[i]) {
                    i++;
                } else {
                    j++;
                }
            } else if (i <= mid && j > end) {
                input[k] = scratch[i];
                i++;
            } else {
                input[k] = scratch[j];
                j++;
            }
            k++;
        }
    }
}
