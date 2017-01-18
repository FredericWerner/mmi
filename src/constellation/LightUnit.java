package constellation;

public class LightUnit implements Runnable{
	@Override
	public void run() {
		System.out.println("Starting Light unit.");
	}

	void performAction(MainUnit.Light light, SpeechUnit.Command command){
		//TODO: actually perform action
		System.out.println("Triggered light: " + light + " with Command " + command + " .");
	}
}
