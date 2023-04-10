package threadbase;

import java.util.concurrent.TimeUnit;

/**
 * JMM 可见性相关.
 * 总结只有两种方式:
 * 1. 内存屏障
 * 2. 上下文切换
 *
 * @author heng.wang
 */
public class Demo01 {
    private boolean flag = true;
    private int count = 0;


    public void refresh() {
        flag = false;
        System.out.println(Thread.currentThread().getName() + "修改flag" + flag);
    }

    public void load() throws NoSuchFieldException, IllegalAccessException {
        System.out.println(Thread.currentThread().getName() + "开始执行");
        while (flag) {
            count++;
//            1. 内存屏障
//            Field f = Unsafe.class.getDeclaredField("theUnsafe");
//            f.setAccessible(true);
//            Unsafe unsafe = (Unsafe) f.get(null);
//            unsafe.storeFence();

            //2. 让出CPU时间片 这个时候会切换上下文 线程执行前会同步内存数据
//            如果一个线程的优先级较高，它可能会连续多次获取到CPU资源，从而导致Thread.yield()失效，
//            因为yield()只是给其他优先级相同或更高的线程一个执行机会，如果当前线程优先级最高，那么其他线程也就无法争夺CPU资源，就算执行了yield()也无法保证可见性。
//            Thread.yield();

            //3.源码也是synchronize
//            System.out.println(count);

            //4. JUC
//            LockSupport.unpark(Thread.currentThread());


            //5. 使用Integer
            /**
             * @see java.lang.Integer#value
             */
//            Integer 拆包的时候是new 了一个对象 其中构造器的value 是一个final 的int 类型
//            JMM 对final关键字保证可见性

//            在Java内存模型中，final关键字的语义被赋予了特殊的含义，它可以保证被final修饰的变量在多线程环境下的可见性和不可变性。
//            具体来说，当一个线程首次访问被final修饰的变量时，JVM会确保该变量的值已经被正确初始化，并将该变量缓存在线程的本地内存中。当其他线程访问该变量时，它们会从主内存中读取该变量的值，而不是从自己的本地内存中读取。
//            这种行为保证了被final修饰的变量在多线程环境下的可见性，因为它保证了所有线程都看到了相同的变量值。同时，由于被final修饰的变量不能被重新赋值，它也保证了该变量的不可变性。
//            需要注意的是，final关键字只能保证被final修饰的变量本身的不可变性，而不能保证它所引用的对象的不可变性。如果一个final变量引用了一个可变对象，那么该对象的属性仍然可以被修改。
//            另外，需要注意的是，final关键字只能保证被final修饰的变量在构造函数执行完毕后才能被其他线程访问。如果在构造函数中使用了未初始化的final变量，那么会导致未定义的行为。因此，应该保证在使用final变量之前，其值已经被正确初始化。

//            Q:对啊 为了保证可见性  在主内存初始化就好了 反正也不会被修改  线程 读取并保存到各自的工作内存中    正是因为不会被修改  索引工作内存 和主内存中一直是同样的  那为什么要去主内存中读取?

//            A:是的，为了保证可见性，final变量的值在主内存中分配和初始化，线程在第一次读取final变量时会从主内存中获取其最新值，并将其保存到线程的工作内存中。
//            之后，线程在对final变量进行读取时，可以从自己的工作内存中获取其值，而不必每次都从主内存中获取。
//            然而，为了保证不同线程之间的数据同步，Java编程语言规范要求，每个线程在访问final变量时都必须从主内存中读取其最新值，而不是从自己的工作内存中读取。
//            这是因为，尽管final变量的值在初始化后不会被修改，但是Java内存模型并不保证不同线程之间的工作内存是同步的。
//            如果某个线程从自己的工作内存中获取final变量的值，而该值尚未被其他线程写入到主内存中，那么该线程将无法读取到最新的final变量的值，从而导致数据不一致的问题。
//            因此，即使final变量的值不会被修改，每个线程在访问final变量时都必须从主内存中读取其最新值，以保证不同线程之间的数据同步。
//            其实主要是针对引用类型 final修饰的引用类型 虽然引用地址不会改变 但是 对象的属性是可以更改的   所以从主内存读取.
//            当线程从主内存中读取final变量的值时，会使用一种称为“volatile读取”的机制。在这种机制下，线程在读取变量值时，会强制刷新自己的本地内存，并从主内存中读取最新的变量值，以确保可见性。


            //6. thread sleep 也是通过内存屏障
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Thread.currentThread().getName() + "跳出循环" + "count=" + count);
    }

    public static void main(String[] args) {
        Demo01 main = new Demo01();
        new Thread(() -> {
            try {
                main.load();
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).start();
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(main::refresh).start();
    }
}