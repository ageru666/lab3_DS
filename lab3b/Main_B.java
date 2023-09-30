package lab3b;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

class BarberShop {
    private final Semaphore barberAvailable = new Semaphore(1);
    private final Semaphore customerAvailable = new Semaphore(0);
    private final Semaphore customerReady = new Semaphore(0);
    private final Semaphore haircutFinished = new Semaphore(0);
    private AtomicInteger customerNumber = new AtomicInteger(0);

    public void customerArrives() throws InterruptedException {
        int currentCustomerNumber = customerNumber.incrementAndGet();

        System.out.println("Customer " + currentCustomerNumber + " arrives.");
        if (barberAvailable.tryAcquire()) {
            System.out.println("Customer " + currentCustomerNumber + " wakes up the barber.");
            customerReady.release();
        } else {
            System.out.println("Customer " + currentCustomerNumber + " joins the queue.");
            customerAvailable.release();
        }
        customerReady.acquire();
        System.out.println("Customer " + currentCustomerNumber + " gets a haircut.");
        haircutFinished.acquire();
        barberAvailable.release();
    }

    public void barberWorks() throws InterruptedException {
        while (true) {
            customerAvailable.acquire();
            System.out.println("Barber wakes up and starts cutting hair.");
            Thread.sleep(2000);
            System.out.println("Barber finishes the haircut.");
            System.out.println("Barber escorts the customer.");
            haircutFinished.release();
            customerReady.release();
        }
    }
}

class Customer implements Runnable {
    private final BarberShop shop;

    public Customer(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            shop.customerArrives();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Barber implements Runnable {
    private final BarberShop shop;

    public Barber(BarberShop shop) {
        this.shop = shop;
    }

    @Override
    public void run() {
        try {
            shop.barberWorks();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class Main_B {
    public static void main(String[] args) {
        int numCustomers = 5;
        BarberShop shop = new BarberShop();
        Thread barberThread = new Thread(new Barber(shop));
        barberThread.start();

        Thread[] customerThreads = new Thread[numCustomers];
        for (int i = 0; i < numCustomers; i++) {
            customerThreads[i] = new Thread(new Customer(shop));
            customerThreads[i].start();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }


        for (Thread customerThread : customerThreads) {
            try {
                customerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}