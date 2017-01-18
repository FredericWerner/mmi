
/*  ************** READ ME *************
 * CONSTELLATION
 *  Speech and Gesture Light System Control
 *  
*	Tested with ASUS Xtion Pro
*	
*   Please make sure that:
*   - Processing 2 is installed
*   - Simple Open NI is installed
*	- the camera is connected
*	to the computer via a USB 2.0 port
*	- run this program as a Java Applet
*   
*   Refer to Constellation Setup Guide
*   https://docs.google.com/document/d/1HpO8hGLaa7HpF74TBYGDHB-de1lfbYJK24u8smRUuSE
*
*------------------------------------------------------
*[TO DO]
* - fix the calibration mode to do it several times if needed
* ------------------------------------------------------
* 
* Coder: Stephane GARTI
* Date: January 2017
* Organisation: TU Berlin
*/

package constellation;
import processing.core.*;

public class Launcher extends PApplet {
	// serialization (required by java on this class)
	private static final long serialVersionUID = -397761594665618496L;


	private SpeechUnit voice;
	private GestureUnit gesture;
	private LightUnit light;
	private Launcher.State state;

	private boolean shouldStop;
	private SpeechUnit.Command command;
	private MainUnit.Light selectedLight;

	private enum State{
		//TODO: maybe model tear down as State as well
		IDLE,
		TRIGGERED, WAITING_FOR_COMMAND,
		INSTRUCTED;

	}

	public static void main(String[] args) {
		PApplet.main("Launcher");

	}
	
	private CalibFrame calibWindow;
	private LaserFrame laserWindow;
	private Button buttonCalib, buttonLaser;

	public void setup() {
		size(640, 480);
		frameRate(15);
		buttonCalib = new Button(this, "Start Calibration", width / 2, height / 3, 200, 100);
		buttonLaser = new Button(this, "Start Light Control", width / 2, 2 * height / 3, 200, 100);
		this.voice = new SpeechUnit(this);
		this.gesture = new GestureUnit();
		this.light = new LightUnit();
		this.state = Launcher.State.IDLE;
		this.shouldStop = false;
		initialize();
		this.run();
	}

	public void draw() {
		background(255);
		buttonCalib.display();
		buttonLaser.display();
	}

	// mouse button clicked
	public void mousePressed() {
		if (buttonCalib.mouseIsOver()) {
			calibWindow = new CalibFrame(0, 0, 640, 480, "Calibration");
		}

		if (buttonLaser.mouseIsOver()) {
			laserWindow = new LaserFrame(0, 0, 1024, 768, "Laser Beam");
		}
	}


	@Override
	public void run() {
		System.out.println("Running main loop");
		while(!shouldStop){
			switch (this.state){
				case IDLE:
					break;
				case TRIGGERED:
					this.selectedLight = gesture.determineLight();
					switchState(State.WAITING_FOR_COMMAND);
					break;
				case WAITING_FOR_COMMAND:
					break;
				case INSTRUCTED:
					light.performAction(this.selectedLight, this.command);
					switchState(State.IDLE);
					break;
			}
		}
		System.out.println("Tear down complete.");
		System.exit(1);
	}

	private void initialize() {
		(new Thread(this.voice)).start();
		(new Thread(this.gesture)).start();
		(new Thread(this.light)).start();
	}

	void onSelectionTrigger(){
		switchState(State.TRIGGERED);
	}

	void onCommand(SpeechUnit.Command command){
		this.command = command;
		switchState(State.INSTRUCTED);
	}

	void onClose(){
		this.shouldStop = true;
	}

	private void switchState(State state){
		System.out.println("Switching state to:" + state);
		this.state = state;
	}
}
