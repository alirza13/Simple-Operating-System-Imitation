public class ConsumerFile extends Thread {
    private StorageFile storage;
    private Assembler assembler;
    private volatile boolean isRunning;
    private Memory memory;

    public ConsumerFile (StorageFile storageFile, Memory memory) {
        this.storage = storageFile;
        this.memory = memory;
    }

    public void run () {
        while(true)
        {
            storage.removeProcess();
        }

    }
}
