package lab3a;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class HoneyPot {
    private final int capacity;
    private int honeyCount;
    private final Lock lock = new ReentrantLock();
    private final Condition beeCondition = lock.newCondition();
    private final Condition bearCondition = lock.newCondition();

    public HoneyPot(int capacity) {
        this.capacity = capacity;
        this.honeyCount = 0;
    }

    public void putHoney() throws InterruptedException {
        lock.lock();
        try {
            while (honeyCount == capacity) {
                beeCondition.await();
            }

            honeyCount++;
            System.out.println("Бджола поклала порцію меду. Поточна кількість: " + honeyCount);

            if (honeyCount == capacity) {
                bearCondition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void eatHoney() throws InterruptedException {
        lock.lock();
        try {
            while (honeyCount < capacity) {
                bearCondition.await();
            }

            System.out.println("Ведмідь їсть мед.");
            honeyCount = 0;
            System.out.println("Ведмідь закінчив їсти мед. Поточна кількість: " + honeyCount);

            beeCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

class Bee extends Thread {
    private final HoneyPot honeyPot;

    public Bee(HoneyPot honeyPot) {
        this.honeyPot = honeyPot;
    }

    @Override
    public void run() {
        try {
            while (true) {
                honeyPot.putHoney();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Bear extends Thread {
    private final HoneyPot honeyPot;

    public Bear(HoneyPot honeyPot) {
        this.honeyPot = honeyPot;
    }

    @Override
    public void run() {
        try {
            while (true) {
                honeyPot.eatHoney();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Main_A {
    public static void main(String[] args) {
        HoneyPot honeyPot = new HoneyPot(10);

        Bear bear = new Bear(honeyPot);
        bear.start();

        for (int i = 1; i <= 10; i++) {
            Bee bee = new Bee(honeyPot);
            bee.start();
        }
    }
}

