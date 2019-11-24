import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class ProducerFile extends Thread {

    private StorageFile storage;
    private Assembler assembler;
    private volatile boolean isRunning;
    private Memory memory;

    ProducerFile (StorageFile storage, Memory memory){
       this.storage = storage;
       this.assembler = new Assembler();
       this.memory = memory;
    }

    private void produce_item()
    {
        String fileName = "inputSequence.txt";
        String line = null;
        String fileASMPath = null;
        int waitTime;

        try {

            // FileReader reads text files in the default encoding.
            FileReader fileReader =
                    new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =
                    new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                    String[] splited = line.split(" ");
                    fileASMPath = splited[0];
                    waitTime = Integer.parseInt(splited[1]);
                    System.out.println( "Creating binary file for "+ fileASMPath+"...") ;
                    String outputFile = fileASMPath.substring(0,fileASMPath.length() - 3);
                    int instructionSize = assembler.createBinaryFile(fileASMPath, outputFile + "bin");
                    storage.insertProcess (new ProcessImage(fileASMPath,memory.getEmptyIndex(),instructionSize));
                    sleep(waitTime);

            }
            // Always close files.
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void run()
    {
        while(true)
        {
            produce_item();

        }
    }
}
