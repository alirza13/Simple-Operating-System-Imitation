import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class OS extends Thread {

	private final int QUANTUM = 5;

	private CPU cpu;
	private Memory memory;
	private volatile List<ProcessImage> readyQueue;
	private volatile List<ProcessImage> blockedQueue;
	private volatile List<ProcessImage> fileInputQueue;
	private volatile List<Integer>   consolInputQueue;
	private volatile int bitmap [];
	private Semaphore mutex;
	private InputThread inputThread;
	private ProducerFile producerFile;
	private ConsumerFile consumerFile;
	private StorageFile storageFile;
	private StorageInput storageInput;
	private ConsumerInput consumerInput;
	private ProducerInput producerInput;

	public OS(int size) {
		bitmap  = new int [size];

		for (int i = 0; i < size; i++){
			bitmap[i] = 0;
		}

		this.memory = new Memory(size,bitmap);
		this.cpu = new CPU(memory);
		this.mutex=new Semaphore(1);
		this.readyQueue = new ArrayList<ProcessImage>();
		this.blockedQueue = new ArrayList<ProcessImage>();
		this.fileInputQueue = new ArrayList<ProcessImage>();
		this.consolInputQueue = new ArrayList<Integer>();
		this.storageFile = new StorageFile(mutex,fileInputQueue,readyQueue,bitmap,memory);
		this.producerFile = new ProducerFile(storageFile,memory);
		this.consumerFile = new ConsumerFile(storageFile,memory);
		producerFile.start();
		consumerFile.start();
		this.storageInput = new StorageInput(consolInputQueue,blockedQueue,readyQueue,mutex);
		this.producerInput = new ProducerInput(storageInput);
		this.consumerInput = new ConsumerInput(storageInput);
		producerInput.start();
		consumerInput.start();
		this.inputThread = new InputThread(mutex, blockedQueue, readyQueue);
		inputThread.start();
	}


	public void loadProcess(String processFile,Assembler assembler)
	{
		try {
			System.out.println( "Creating binary file for "+ processFile+"...") ;
			int instructionSize = assembler.createBinaryFile(processFile, "assemblyInput.bin");
			char[] process = assembler.readBinaryFile(instructionSize, "assemblyInput.bin");

			System.out.println("Loading process to memory...");

			mutex.acquire();

			readyQueue.add(new ProcessImage(processFile,memory.getEmptyIndex(),instructionSize)); // readyqueue ya ekledi.

			mutex.release();

			this.memory.addInstructions(process, instructionSize, memory.getEmptyIndex()); // memory e koydu.
			System.out.println("Process is loaded !");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			while (true) {

				mutex.acquire();
				boolean isBlockedQueueEmpty = blockedQueue.isEmpty();
				boolean isReadyQueueEmpty = readyQueue.isEmpty();
				mutex.release();

				if(isBlockedQueueEmpty && isReadyQueueEmpty) {
					break;
				}

				if (!isReadyQueueEmpty) {
					System.out.println("Executing " + (readyQueue.get(0)).processName);
					cpu.transferFromImage(readyQueue.get(0));
					for (int i = 0; i < QUANTUM; i++) {
						if (cpu.getPC() < cpu.getLR()) {
							cpu.fetch(); 
							int returnCode = cpu.decodeExecute();

							if (returnCode == 0)  {
								System.out.println("Process " + readyQueue.get(0).processName + " made a system call for ");
								if (cpu.getV() == 0) {
									System.out.println( "Input, transfering to blocked queue and waiting for input...");
									ProcessImage p=new ProcessImage();
									this.cpu.transferToImage(p);
									
									mutex.acquire();
									readyQueue.remove(0);
									blockedQueue.add(p);
									mutex.release();
								} 
								else { //syscall for output
									System.out.print("Output Value: ");
									ProcessImage p=new ProcessImage();
									cpu.transferToImage(p);

									mutex.acquire();
									readyQueue.remove(0);
									System.out.println( p.V +"\n");
									readyQueue.add(p);
									mutex.release();
								}
								//Process blocked, need to end quantum prematurely
								break;
							}
						}
						else {
							System.out.println("Process " + readyQueue.get(0).processName +" has been finished! Removing from the queue...\n" );
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);
							p.writeToDumpFile();

							mutex.acquire();
							readyQueue.remove(0);
							mutex.release();
							break;
						}

						if (i == QUANTUM - 1) {
							//quantum finished put the process at the end of readyQ
							System.out.println ("Context Switch! Allocated quantum have been reached, switching to next process...\n");
							ProcessImage p = new ProcessImage();
							cpu.transferToImage(p);  

							mutex.acquire();
							readyQueue.remove(0);
							readyQueue.add(p);
							mutex.release();
						}
					}
				}
			}
			inputThread.stopThread();
			System.out.println("Execution of all processes has finished!");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
