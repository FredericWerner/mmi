package constellation;
import java.awt.Frame;
import java.awt.event.*;
import SimpleOpenNI.SimpleOpenNI;
import java.awt.BorderLayout;
import processing.core.*;

public class LaserFrame extends Frame {
	private static final long serialVersionUID = -8312777389085939599L;
	int xLoc;
	int yLoc;
	int h;
	int w;
	String name;
	LaserApplet laser;
	public static int lightSelected;

	LaserFrame(int x_, int y_, int w_, int h_, String name_) {
		super("Embedded PApplet");
		xLoc = x_;
		yLoc = y_;
		w = w_;
		h = h_;
		name = name_;
		laser = new LaserApplet();
		add(laser, BorderLayout.CENTER);
		laser.init();
		setBounds(xLoc, yLoc, w, h);
		setTitle(name);
		setVisible(true);
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
            	laser.noLoop();
            	dispose();
            }
        });
	}

	public static int getLightSelected() {
		return lightSelected;
	}

	public class LaserApplet extends PApplet {
		private static final long serialVersionUID = -743200068153160983L;
		float zoomF = 0.5f;
		float rotX = radians(180);  // by default rotate the hole scene 180deg around the x-axis,
		float rotY = radians(0);
		boolean autoCalib = true;
		int white = color(255, 255, 255);
		int[] userClr = new int[] { color(255, 0, 0), color(0, 255, 0), color(0, 0, 255), color(255, 255, 0),
				color(255, 0, 255), color(0, 255, 255) };
		float sphereRadius = 400;
		PVector backLeft, backRight, frontRight, frontLeft;
		PVector light1, light2, light3;
		SimpleOpenNI context;

		public void setup() {
			size(1024, 768, P3D);
			// ceiling and lights
			
//			backLeft =   CalibFrame.CalibApplet.backLeft;
//			backRight =  CalibFrame.CalibApplet.backRight;
//			frontRight = CalibFrame.CalibApplet.frontRight;
//			frontLeft =  CalibFrame.CalibApplet.frontLeft;
//			light1 =     CalibFrame.CalibApplet.light1;
//			light2 =     CalibFrame.CalibApplet.light2;
//			light3 =     CalibFrame.CalibApplet.light3;
			
			backLeft = new PVector (-1847.564f, -270.76373f, 9084.0f);
			backRight = new PVector (3201.383f, -270.76373f, 9084.0f);
			frontRight = new PVector(1824.925f,  1222.9535f, 3614.0f);
			frontLeft = new PVector(-1973.762f,  1251.1647f, 3816.0f);
			light1 = new PVector    (112.8585f,  1003.1871f, 3576.0f);
			light2 = new PVector   (-1151.925f,  899.94214f, 2933.0f);
			light3 = new PVector    (1392.872f,  430.38202f, 4463.0f);


			// camera setup
			context = new SimpleOpenNI(this);
			context.setMirror(true);
			if (context.isInit() == false) {
				println("Can't init SimpleOpenNI, maybe the camera is not connected!");
				exit();
				return;
			}
			context.enableDepth();
			context.enableUser();
			// transform display to show perspective
			perspective(radians(45), (float) width / height, 10, 150000);
		}

		public void draw() {
			background(0, 0, 0);

			// update the cam
			context.update();

			// set the scene pos
			translate(width / 2, height / 2, 0);
			rotateX(rotX);
			rotateY(rotY);
			scale(zoomF);

			int[] depthMap = context.depthMap();
			int steps = 4; // to speed up the drawing, draw every fourth point
			int index;
			PVector realWorldPoint;

			translate(0, 0, -1000); // set the rotation center of the scene 1000
									// infront of the camera

			// points projected representation
			strokeWeight(2);
			stroke(100);
			for (int y = 0; y < context.depthHeight(); y += steps) {
				for (int x = 0; x < context.depthWidth(); x += steps) {
					index = x + y * context.depthWidth();
					if (depthMap[index] > 0) {
						realWorldPoint = context.depthMapRealWorld()[index];
						point(realWorldPoint.x, realWorldPoint.y, realWorldPoint.z);
					}
				}
			}

			// draw the skeleton if it's available, for each the users
			int[] userList = context.getUsers();

			for (int i = 0; i < userList.length; i++) {
				if (context.isTrackingSkeleton(userList[i])) {
					drawIntersection(userList[i]);
				}
			}
			pushStyle(); // style layer: modifications of style until popStyle
			stroke(255, 255, 255, 150);
			strokeWeight(3);
			line3D(backLeft, backRight);
			line3D(backRight, frontRight);
			line3D(frontRight, frontLeft);
			line3D(frontLeft, backLeft);
			ellipse3D(light1, 30, white);
			ellipse3D(light2, 30, white);
			ellipse3D(light3, 30, white);
			popStyle();

		}

		public void drawIntersection(int userId) {

			// get the position of the elbow and the arm
			PVector jointPos1 = new PVector();
			PVector jointPos2 = new PVector();
			// call to get skeleton data from the cam (returns confidence)
			float c1 = context.getJointPositionSkeleton(userId, SimpleOpenNI.SKEL_LEFT_ELBOW, jointPos1);
			float c2 = context.getJointPositionSkeleton(userId, SimpleOpenNI.SKEL_LEFT_HAND, jointPos2);
			// draw ray from the hand + 4m
			PVector dir = PVector.sub(jointPos2, jointPos1);
			dir.normalize();
			PVector rayEnd = PVector.mult(dir, 4000);
			rayEnd.add(jointPos2);

			pushStyle(); // style layer: modifications of style until popStyle
			stroke(255);
			strokeWeight(5);
			line(jointPos2.x, jointPos2.y, jointPos2.z, rayEnd.x, rayEnd.y, rayEnd.z);

			if(intersectionLight(jointPos2, dir, light1, userClr[0])) {lightSelected = 1;}
			else if(intersectionLight(jointPos2, dir, light2, userClr[1])) {lightSelected =2;}
			else if(intersectionLight(jointPos2, dir, light3, userClr[2])) {lightSelected =3;}
			popStyle();
		}

		// test for intersections
		public boolean intersectionLight(PVector joint, PVector direction, PVector light, int c) {

			PVector hit1 = new PVector();
			PVector hit2 = new PVector();

			// Open NI call: need an origin, a direction, a sphere target with a
			// radius
			// Output: two vector of intersection with the sphere
			int intersectionSphere = SimpleOpenNI.raySphereIntersection(joint, direction, light, sphereRadius, hit1,
					hit2);
			
			if (intersectionSphere > 0) {
				ellipse3D(light, 50, c);
				return true;
			}
			else {return false;}
		}

		// -----------------------------------------------------------------
		// SimpleOpenNI events

		public void onNewUser(SimpleOpenNI curContext, int userId) {
			println("onNewUser - userId: " + userId);
			println("\tstart tracking skeleton");
			curContext.startTrackingSkeleton(userId);
		}

		public void onLostUser(SimpleOpenNI curContext, int userId) {
			println("onLostUser - userId: " + userId);
		}

		// --------------------------------
		// useful

		public void line3D(PVector p, PVector q) {
			line(p.x, p.y, p.z, q.x, q.y, q.z);
		}

		public void ellipse3D(PVector p, int r, int c) {
			pushMatrix();
			translate(p.x, p.y, p.z);
			fill(c);
			sphere(r);
			popMatrix();
		}

	}

}
