public class MainApplication {

	public static void main(String[] args) {
		Assembler assembler = new Assembler();
		OS os = new OS(5000);
		os.loadProcess("assemblyInput1.asm", assembler);

		os.start();
	}
}
