package agentstrails;

import processing.core.*;
import toxi.geom.*;
import toxi.volume.VolumetricBrush;

class Vert {
	PApplet parent;
	VolumetricBrush brush;
	int ID;
	Vec3D pos;

	Vert(Vec3D position, int _ID, VolumetricBrush b, PApplet p) {
		parent = p;
		brush = b;
		ID = _ID;
		pos = new Vec3D(position.x, position.y, position.z);
	}
	
	void run() {
		display();
		brush.setSize(80);
		brush.drawAtAbsolutePos(pos, .2f);
	}
	
	void display() {    
		parent.stroke(150, 145);
		parent.strokeWeight(1);
		parent.point(pos.x, pos.y, pos.z);
	}

	Vec3D getLocation() {
		return new Vec3D(pos.x, pos.y, pos.z);
	}

	int getID() {
		return ID;
	}
}
