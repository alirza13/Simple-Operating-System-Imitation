public class Memory {

	private int memorySize;
	private char[] memory;
	private int emptyIndex;
	private int [] bitmap;

	public Memory(int size,int [] bitmap) {
		memorySize = size;
		memory = new char[size];
		this.bitmap = bitmap;
		emptyIndex = 0;
	}

	void addInstructions(char[] buffer, int bufferSize, int BR)
	{
		for (int i = BR; i < bufferSize+BR; i++)
		{
			this.memory[i] = buffer[i - BR];
			this.bitmap[i] = 1;
		}

		emptyIndex += bufferSize;
	}


	char[]getInstruction(int PC, int BR)
	{
		char[]instruction = new char[4];
		instruction[0]=memory[PC+BR];
		instruction[1]=memory[PC+BR+1];
		instruction[2]=memory[PC+BR+2];
		instruction[3]=memory[PC+BR+3];

		return instruction;

	}

	int getEmptyIndex()
	{
		return this.emptyIndex;
	}

	public int getMemorySize() {
		return memorySize;
	}

}
