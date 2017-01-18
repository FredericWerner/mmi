package constellation;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.io.IOException;

public class SpeechUnit implements Runnable{

	private final Launcher main;
	private State state;

	private final static String ACOUSTIC_MODEL_PATH  = "resource:/edu/cmu/sphinx/models/en-us/en-us";
	private final static String DICTIONARY_PATH = "file:res/cmudict-mi.dict";
	private final static String LANGUAGE_MODEL_PATH = "resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin";

	private final static String CLOSE_STRING = "close";


	private final static String[] LIGHT_SELECTER = {
			"switch that", "switch this", "select that", "select that", "make that", "make this"
	};


	private enum State{
		IDLE, LIGHT_CHOSEN;
	}

	public enum Command{
		BLUE("blue"),
		RED("red"),
		GREEN("green"),
		YELLOW("yellow"),
		WHITE("white"),
		PURPLE("purple"),
		ON("on"),
		OFF("off"),
		BRIGHTER("brighter"),
		LIGHTER("lighter"),
		UNDEFINED_COMMAND("undefined");

		private final String word;

		Command(String word) {
			this.word = word;
		}

		public String getWord() {
			return word;
		}
	}

	@Override
	public void run() {
		Configuration configuration = new Configuration();


		configuration
				.setAcousticModelPath(ACOUSTIC_MODEL_PATH);
		configuration
				.setDictionaryPath(DICTIONARY_PATH);
		configuration
				.setLanguageModelPath(LANGUAGE_MODEL_PATH);

		System.out.print("Starting recognition....");

		try {
			LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
			recognizer.startRecognition(true);
			System.out.println("Done");
			while(true){
				SpeechResult result = recognizer.getResult();
				System.out.println(result.getHypothesis());
				switch (state){
					case IDLE:
						if(isLightSelection(result)){
							this.state=State.LIGHT_CHOSEN;
							onSelectionTrigger();
						}
						break;
					case LIGHT_CHOSEN:
						Command returnCommand = getCommand(result);
						if(Command.UNDEFINED_COMMAND != returnCommand){
							this.state=State.IDLE;
							onCommand(returnCommand);
						}
						break;
				}
				if(containsClose(result)){
					onClose();
					break;
				}
			}
			recognizer.stopRecognition();


		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("SpeechUnit closed");

	}

	private Command getCommand(SpeechResult result) {
		String hypothesis = result.getHypothesis();
		for(Command command: Command.values()){
			if(hypothesis.contains(command.getWord())){
				return command;
			}
		}
		return Command.UNDEFINED_COMMAND;
	}


	private boolean isLightSelection(SpeechResult result) {
		String hypothesis = result.getHypothesis();
		return stringContainsArrayEntry(hypothesis, LIGHT_SELECTER);
	}

	private boolean stringContainsArrayEntry(String hypothesis, String[] array) {
		//TODO: replace with Java8 Streams
		for (String word: array){
			if(hypothesis.contains(word)){
				return true;
			}
		}
		return false;
	}

	private boolean containsClose(SpeechResult result) {
		return result.getHypothesis().contains(CLOSE_STRING);
	}

	private void onSelectionTrigger(){
		System.out.println("You have triggered the light selection.");
		main.onSelectionTrigger();

	}

	private void onCommand(Command command) {
		System.out.println("You have chosen the command: " + command);
		main.onCommand(command);
	}

	private void onClose(){
		System.out.println("Received close. Tearing down Speech Recognition.");
		main.onClose();
	}

	public SpeechUnit(Launcher main) {
		this.state = State.IDLE;
		this.main = main;
	}

	public static void main(String[] args) throws Exception {
//		SpeechUnit eval = new SpeechUnit();
//		eval.run();
	}
}