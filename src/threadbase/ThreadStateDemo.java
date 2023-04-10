package threadbase;

import java.util.concurrent.TimeUnit;

/**
 * @author heng.wang
 */
public class ThreadStateDemo {

    public static void main(String[] args) {
        new Thread(() -> System.out.println("启动线程")).start();
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(() -> System.out.println("启动线程2")).start();
    }
}
