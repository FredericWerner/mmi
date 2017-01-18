package constellation;

import processing.core.PApplet;

public class Button {
	String label;
	float x; // center x position
	float y; // center y position
	float w; // width of button
	float h; // height of button
	PApplet parent; // The parent PApplet that we will render ourselves onto

	Button(PApplet p, String labelB, float xpos, float ypos, float widthB, float heightB) {
		parent = p;
		label = labelB;
		w = widthB;
		h = heightB;
		x = xpos - widthB/2;
		y = ypos - heightB/2;
	}

	void display() {
		parent.fill(218);
		parent.noStroke();
		parent.rect(x, y, w, h);
		parent.textAlign(parent.CENTER, parent.CENTER);
		parent.fill(0);
		parent.textSize(20);
		parent.text(label, x + (w / 2), y + (h / 2));
	}

	boolean mouseIsOver() {
		if (parent.mouseX > x && parent.mouseX < (x + w) && parent.mouseY > y && parent.mouseY < (y + h)) {
			return true;
		}
		return false;
	}
}