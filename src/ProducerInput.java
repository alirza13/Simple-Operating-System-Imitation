import java.util.Scanner;

public class ProducerInput extends Thread {
    StorageInput storage;

    public ProducerInput (StorageInput storage)
    {
        this.storage = storage;
    }

    private int produceItem () {
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("Enter a number: ");
        int number = reader.nextInt();
        reader.close();
        return number;
    }

    public void run()
    {
        while (true){
            storage.produceInput();
        }

    }
}
