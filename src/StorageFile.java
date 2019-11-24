import java.util.List;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class StorageFile {
    private Semaphore mutexFileInput;
    private volatile List<ProcessImage> fileInputQueue;
    private volatile List<ProcessImage> fileReadyQueue;
    private int [] bitmap;
    private Assembler assembler = new Assembler();
    private Memory memory;

    private int MAX_STORAGE = 10;
    private int itemSize = 0;

    private int insertionIndex = 0;
    private int removalIndex = 0;

    StorageFile (Semaphore mtx , List<ProcessImage> fileInputQueue,List<ProcessImage> fileReadyQueue, int [] bitmap, Memory memory) {
        this.mutexFileInput = mtx;
        this.fileInputQueue = fileInputQueue;
        this.fileReadyQueue = fileReadyQueue;
        this.bitmap = bitmap;
        this.memory = memory;
    }

    public synchronized void insertProcess (ProcessImage process)
    {
        try
        {
            if(itemSize == MAX_STORAGE)
            {
                System.out.println("Producer Went to sleep");
                goToSleep();
            }

            fileInputQueue.add(insertionIndex,process);
            insertionIndex = (insertionIndex + 1) % MAX_STORAGE;
            itemSize++;
            System.out.println("An item is added to storage. Storage size: " + itemSize);

            if(itemSize == 1)
            {
                System.out.println("Consumer WOKE UP");
                notify();
            }
        }
        catch (Exception e)
        {
        }
    }

    public synchronized  void removeProcess () {

        if (itemSize == 0)
        {
            System.out.println("Consumer went to sleep");
            goToSleep();
        }

        ProcessImage process = fileInputQueue.get(removalIndex);
        boolean isEnough = FirstFit(process);
        if (isEnough)
        {
            System.out.println("DEBUG " + process.processName);
            char[] charProcess = assembler.readBinaryFile(process.LR, process.processName.substring(0,process.processName.length() - 3) + "bin");
            System.out.println("Loading process to memory...");
            this.memory.addInstructions(charProcess, process.LR, memory.getEmptyIndex());
            try
            {
                mutexFileInput.acquire();
                fileReadyQueue.add(new ProcessImage(process.processName, memory.getEmptyIndex(),process.LR)); // readyqueue ya ekledi.
                mutexFileInput.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //fileInputQueue.remove(removalIndex);
            removalIndex = (removalIndex + 1) % MAX_STORAGE;
            itemSize--;
            System.out.println("An item is removed from storage. Storage size: " + itemSize);
        }
        else {
            try
            {
                sleep(200);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if(itemSize == MAX_STORAGE - 1)
            notify();
    }

    private boolean FirstFit (ProcessImage processImage) {
        boolean isEnough = false;
        for (int i = 0; i < bitmap.length; i++)
        {
            if (bitmap[i] == 0 && i + processImage.LR < bitmap.length)
            {
                isEnough = true;
            }
        }
        return  isEnough;
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
