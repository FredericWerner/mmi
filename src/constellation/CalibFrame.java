package constellation;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import SimpleOpenNI.SimpleOpenNI;
import java.awt.BorderLayout;
import processing.core.*;

public class CalibFrame extends java.awt.Frame {
	private static final long serialVersionUID = -8312777389085939599L;
	int xLoc;
	int yLoc;
	int h;
	int w;
	String name;
	CalibApplet calib;

	CalibFrame(int x_, int y_, int w_, int h_, String name_) {
		super("Embedded PApplet");
		xLoc = x_;
		yLoc = y_;
		w = w_;
		h = h_;
		name = name_;
		calib = new CalibApplet();
		add(calib, BorderLayout.CENTER);
		calib.init();
		setBounds(xLoc, yLoc, w, h);
		setTitle(name);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            	calib.noLoop();
            	dispose();
            }
        });
	}

	public static class CalibApplet extends PApplet {
		// serialization of the class
		private static final long serialVersionUID = -2073021198946817730L;
		// steps of calibration
		final static int CALIB_START = 0;
		final static int CALIB_BACK_LEFT = 1;
		final static int CALIB_BACK_RIGHT = 2;
		final static int CALIB_FRONT_RIGHT = 3;
		final static int CALIB_FRONT_LEFT = 4;
		final static int CALIB_LIGHT_1 = 5;
		final static int CALIB_LIGHT_2 = 6;
		final static int CALIB_LIGHT_3 = 7;
		public final static int CALIB_DONE = 8;
		public static int calibMode = CALIB_START;
		boolean screenFlag = true;
		public static PVector backLeft = new PVector();
		public static PVector backRight = new PVector();
		public static PVector frontRight = new PVector();
		public static PVector frontLeft = new PVector();
		public static PVector light1 = new PVector();
		public static PVector light2 = new PVector();
		public static PVector light3 = new PVector();
		public static PVector tempVec1 = new PVector();
		public static PVector tempVec2 = new PVector();
		public static PVector tempVec3 = new PVector();
		public static PVector tempVec4 = new PVector();
		public static PVector tempVec5 = new PVector();
		public static PVector tempVec6 = new PVector();
		public static PVector tempVec7 = new PVector();
		SimpleOpenNI context;

		public void setup() {
			size(640, 480);
			
			context = new SimpleOpenNI(this);
			context.setMirror(false);
			// enable depthMap generation
			context.enableDepth();
			if (context.isInit() == false) {
				println("Can't open the depthMap, maybe the camera is not connected!");
				exit();
			}
			// align depth data to image data
			context.alternativeViewPointDepthToImage();
		}

		public void draw() {
			// update the cam
			context.update();

			image(context.depthImage(), 0, 0);

			// draw text background
			pushStyle();
			noStroke();
			fill(0, 200, 0, 100);
			rect(0, 0, width, 40);
			popStyle();

			switch (calibMode) {
			case CALIB_START:
				text("To start the calibration press SPACE!", 5, 30);
				break;
			case CALIB_BACK_LEFT:
				text("Set the point at the back left corner of the ceiling and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_BACK_RIGHT:
				text("Set the point at the back right corner of the ceiling and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_FRONT_RIGHT:
				text("Set the point at the front right corner of the ceiling and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_FRONT_LEFT:
				text("Set the point at the front left corner of the ceiling and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_LIGHT_1:
				text("Set the position of light 1 and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_LIGHT_2:
				text("Set the position of light 2 and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_LIGHT_3:
				text("Set the position of light 3 and press SPACE to confirm.", 5, 30);
				break;
			case CALIB_DONE:
				text("New ceiling is defined! You can close this window.", 5, 30);
				break;
			}

			// draw
			drawCalibPoint();

			pushStyle();
			strokeWeight(3);
			noFill();

			context.convertRealWorldToProjective(backLeft, tempVec1);
			context.convertRealWorldToProjective(backRight, tempVec2);
			context.convertRealWorldToProjective(frontRight, tempVec3);
			context.convertRealWorldToProjective(frontLeft, tempVec4);
			context.convertRealWorldToProjective(light1, tempVec5);
			context.convertRealWorldToProjective(light2, tempVec6);
			context.convertRealWorldToProjective(light3, tempVec7);

			stroke(255, 255, 255, 150);
			ellipse(tempVec1.x, tempVec1.y, 10, 10);
			line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);
			line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);
			line(tempVec3.x, tempVec3.y, tempVec4.x, tempVec4.y);
			line(tempVec4.x, tempVec4.y, tempVec1.x, tempVec1.y);
			stroke(255, 0, 0);
			ellipse(tempVec5.x, tempVec5.y, 10, 10);
			stroke(0, 255, 0);
			ellipse(tempVec6.x, tempVec6.y, 10, 10);
			stroke(0, 0, 255);
			ellipse(tempVec7.x, tempVec7.y, 10, 10);

			popStyle();
		}

		public void drawCalibPoint() {
			pushStyle();

			strokeWeight(3);
			noFill();

			switch (calibMode) {
			case CALIB_START:
				break;
			case CALIB_BACK_LEFT:
				context.convertRealWorldToProjective(backLeft, tempVec1);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);
				break;

			case CALIB_BACK_RIGHT:
				// draw the null point
				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				stroke(255, 255, 255, 150);
				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				break;
			case CALIB_FRONT_RIGHT:

				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);
				context.convertRealWorldToProjective(frontRight, tempVec3);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				ellipse(tempVec3.x, tempVec3.y, 10, 10);
				line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);
				break;

			case CALIB_FRONT_LEFT:

				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);
				context.convertRealWorldToProjective(frontRight, tempVec3);
				context.convertRealWorldToProjective(frontLeft, tempVec4);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				ellipse(tempVec3.x, tempVec3.y, 10, 10);
				line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);

				ellipse(tempVec4.x, tempVec4.y, 10, 10);
				line(tempVec3.x, tempVec3.y, tempVec4.x, tempVec4.y);

				line(tempVec4.x, tempVec4.y, tempVec1.x, tempVec1.y);
				break;

			case CALIB_LIGHT_1:

				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);
				context.convertRealWorldToProjective(frontRight, tempVec3);
				context.convertRealWorldToProjective(frontLeft, tempVec4);
				context.convertRealWorldToProjective(light1, tempVec5);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				ellipse(tempVec3.x, tempVec3.y, 10, 10);
				line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);

				ellipse(tempVec4.x, tempVec4.y, 10, 10);
				line(tempVec3.x, tempVec3.y, tempVec4.x, tempVec4.y);

				line(tempVec4.x, tempVec4.y, tempVec1.x, tempVec1.y);

				stroke(255, 0, 0);
				ellipse(tempVec5.x, tempVec5.y, 10, 10);
				break;

			case CALIB_LIGHT_2:

				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);
				context.convertRealWorldToProjective(frontRight, tempVec3);
				context.convertRealWorldToProjective(frontLeft, tempVec4);
				context.convertRealWorldToProjective(light1, tempVec5);
				context.convertRealWorldToProjective(light2, tempVec6);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				ellipse(tempVec3.x, tempVec3.y, 10, 10);
				line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);

				ellipse(tempVec4.x, tempVec4.y, 10, 10);
				line(tempVec3.x, tempVec3.y, tempVec4.x, tempVec4.y);

				line(tempVec4.x, tempVec4.y, tempVec1.x, tempVec1.y);

				stroke(255, 0, 0);
				ellipse(tempVec5.x, tempVec5.y, 10, 10);

				stroke(0, 255, 0);
				ellipse(tempVec6.x, tempVec6.y, 10, 10);

				break;

			case CALIB_LIGHT_3:

				context.convertRealWorldToProjective(backLeft, tempVec1);
				context.convertRealWorldToProjective(backRight, tempVec2);
				context.convertRealWorldToProjective(frontRight, tempVec3);
				context.convertRealWorldToProjective(frontLeft, tempVec4);
				context.convertRealWorldToProjective(light1, tempVec5);
				context.convertRealWorldToProjective(light2, tempVec6);
				context.convertRealWorldToProjective(light3, tempVec7);

				stroke(255, 255, 255, 150);
				ellipse(tempVec1.x, tempVec1.y, 10, 10);

				ellipse(tempVec2.x, tempVec2.y, 10, 10);
				line(tempVec1.x, tempVec1.y, tempVec2.x, tempVec2.y);

				ellipse(tempVec3.x, tempVec3.y, 10, 10);
				line(tempVec2.x, tempVec2.y, tempVec3.x, tempVec3.y);

				ellipse(tempVec4.x, tempVec4.y, 10, 10);
				line(tempVec3.x, tempVec3.y, tempVec4.x, tempVec4.y);

				line(tempVec4.x, tempVec4.y, tempVec1.x, tempVec1.y);

				stroke(255, 0, 0);
				ellipse(tempVec5.x, tempVec5.y, 10, 10);

				stroke(0, 255, 0);
				ellipse(tempVec6.x, tempVec6.y, 10, 10);
				stroke(0, 0, 255);
				ellipse(tempVec7.x, tempVec7.y, 10, 10);

				break;

			case CALIB_DONE:
				break;
			}

			popStyle();
		}

		public void keyPressed() {
			switch (key) {
			case '1':
				screenFlag = !screenFlag;
				break;
			case ' ':
				calibMode++;
				if (calibMode == CALIB_DONE) {
					println("Set the user defined ceiling");
					println("backLeft: " + backLeft);
					println("backRight: " + backRight);
					println("frontRight: " + frontRight);
					println("frontLeft: " + frontLeft);
					println("light 1: " + light1);
					println("light 2: " + light2);
					println("light 3: " + light3);
				}

				break;
			}
		}

		public void mousePressed() {
			if (mouseButton == LEFT) {
				PVector[] realWorldMap = context.depthMapRealWorld();
				int index = mouseX + mouseY * context.depthWidth();

				switch (calibMode) {
				case CALIB_BACK_LEFT:
					backLeft.set(realWorldMap[index]);
					break;
				case CALIB_BACK_RIGHT:
					backRight.set(realWorldMap[index]);
					break;
				case CALIB_FRONT_RIGHT:
					frontRight.set(realWorldMap[index]);
					break;
				case CALIB_FRONT_LEFT:
					frontLeft.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_1:
					light1.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_2:
					light2.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_3:
					light3.set(realWorldMap[index]);
					break;
				}
			} else {
				PVector[] realWorldMap = context.depthMapRealWorld();
				int index = mouseX + mouseY * context.depthWidth();
				println("Point3d: " + realWorldMap[index].x + "," + realWorldMap[index].y + ","
						+ realWorldMap[index].z);
			}
		}

		public void mouseDragged() {
			if (mouseButton == LEFT) {
				PVector[] realWorldMap = context.depthMapRealWorld();
				int index = mouseX + mouseY * context.depthWidth();

				switch (calibMode) {
				case CALIB_BACK_LEFT:
					backLeft.set(realWorldMap[index]);
					break;
				case CALIB_BACK_RIGHT:
					backRight.set(realWorldMap[index]);
					break;
				case CALIB_FRONT_RIGHT:
					frontRight.set(realWorldMap[index]);
					break;
				case CALIB_FRONT_LEFT:
					frontLeft.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_1:
					light1.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_2:
					light2.set(realWorldMap[index]);
					break;
				case CALIB_LIGHT_3:
					light3.set(realWorldMap[index]);
					break;
				}
			}
		}
	}
}