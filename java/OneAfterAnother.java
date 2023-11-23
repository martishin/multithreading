class Main {
    public static void main(String[] args) {
        FooBar fooBar = new FooBar(3);

        Thread t1 = new FooBarThread(fooBar, "foo");
        Thread t2 = new FooBarThread(fooBar, "bar");

        t2.start();
        t1.start();
    }
}

class FooBar {
    private int n;
    private int flag = 0;

    public FooBar(int n) {
        this.n = n;
    }

    public void foo() {

        for (int i = 1; i <= n; i++) {
            synchronized (this) {
                while (flag == 1) {
                    try {
                        this.wait();
                    } catch (Exception e) {

                    }
                }
                System.out.print("Foo");
                flag = 1;
                this.notifyAll();
            }
        }
    }

    public void bar() {
        for (int i = 1; i <= n; i++) {
            synchronized (this) {
                while (flag == 0) {
                    try {
                        this.wait();
                    } catch (Exception e) {

                    }
                }
                System.out.println("Bar");
                flag = 0;
                this.notifyAll();
            }
        }
    }
}


class FooBarThread extends Thread {
    FooBar fooBar;
    String method;

    public FooBarThread(FooBar fooBar, String method) {
        this.fooBar = fooBar;
        this.method = method;
    }

    public void run() {
        if ("foo".equals(method)) {
            fooBar.foo();
        } else if ("bar".equals(method)) {
            fooBar.bar();
        }
    }
}
