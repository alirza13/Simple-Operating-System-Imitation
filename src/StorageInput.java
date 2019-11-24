import javafx.scene.control.SeparatorMenuItem;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class StorageInput {
    private volatile List<Integer> inputQueue;
    private volatile List<ProcessImage> blockedQueue;
    private volatile List<ProcessImage> readyQueue;
    Semaphore mutex;

    private int MAX_STORAGE = 10;
    private int itemSize = 0;

    private int insertionIndex = 0;
    private int removalIndex = 0;

    public StorageInput(List<Integer> inputQueue, List<ProcessImage> blockedQueue,List<ProcessImage> readyQueue, Semaphore mutex) {
        this.blockedQueue = blockedQueue;
        this.inputQueue = inputQueue;
        this.mutex = mutex;
        this.readyQueue = readyQueue;
    }

    public synchronized void produceInput ()
    {
        if(itemSize == MAX_STORAGE)
        {
            System.out.println("Producer Input Went to sleep");
            goToSleep();
        }
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter a number: ");

        int number = reader.nextInt();

        inputQueue.add(insertionIndex,number);
        insertionIndex = (insertionIndex + 1) % MAX_STORAGE;
        itemSize++;
        System.out.println("An item is added to storage. Storage size: " + itemSize);

        if(itemSize == 1)
        {
            System.out.println("Consumer Input WOKE UP");
            notify();
        }
    }

    public synchronized void removeInput ()
    {
        try {
            if (itemSize == 0)
            {
                System.out.println("Consumer went to sleep");
                goToSleep();
            }

            int number = inputQueue.get(removalIndex);
            boolean isBlockedEmpty;
            mutex.acquire();
            isBlockedEmpty = blockedQueue.isEmpty();
            mutex.release();
            System.out.println("Inside of isBlockedEmpty  ");
            System.out.println("Inside of isBlockedEmpty  ");
            System.out.println("Inside of isBlockedEmpty  ");

            if (!isBlockedEmpty){


                mutex.acquire();
                ProcessImage process = blockedQueue.remove(0);
                process.V = number;
                System.out.println("Setting process V register to " + number);
                readyQueue.add(process);
                mutex.release();
                removalIndex = (removalIndex + 1) % MAX_STORAGE;
                itemSize--;
                System.out.println("An item is removed from storage. Storage size: " + itemSize);
            }
            else {
                sleep (200);
            }

            if(itemSize == MAX_STORAGE - 1){
                System.out.println("Producer Input WOKE UP");
                notify();
            }
        }

        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void goToSleep() {
        try
        {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
