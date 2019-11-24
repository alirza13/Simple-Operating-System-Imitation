public class ConsumerInput extends Thread {
    StorageInput storage;

    public ConsumerInput (StorageInput storage)
    {
        this.storage = storage;
    }

    public void run() {
        while (true)
        {
            storage.removeInput();
        }
    }
}
