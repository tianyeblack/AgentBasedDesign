package agentstrails;

import java.util.ArrayList;

import processing.core.*;
import toxi.geom.Vec3D;
import toxi.volume.VolumetricBrush;

class Agent {
	PApplet parent;
	VolumetricBrush brush;
	int ID;
	Vec3D loc;//loc.
	Vec3D vel;
	Vec3D acc= new Vec3D();//acceleration.
	Vec3D start;
	
	float maxvel=2;//A floating variable for the maximum speed.
	float maxForce = 10;
	boolean runToggle=true;
	int fovScore=300;
	int fovCoh=600;
	int every=5;
	
	ArrayList <Vec3D> trail;
	
	ArrayList <Vert> vertices;
	ArrayList <Agent> agents;
	float[] scores;
	
	Agent(ArrayList<Vert> vs, ArrayList<Agent> as, float[] ss, int _ID, VolumetricBrush b, PApplet p) {//class(parameters)
		parent = p;
		brush = b;
		vertices = vs;
		agents = as; 
		scores = ss;
		ID=_ID;
		loc = vertices.get(ID).getLocation(); 
		start = loc.copy();
		vel = new Vec3D(.5f* parent.random (-2, 2), .5f* parent.random(-2, 2), 0);//velocity.
		trail = new ArrayList<Vec3D>();
	}
	///////////////////////////////
	//FUNCTIONS
	// run() A function that encompasses all functions.
	//We use this function in order to group the other functions.
	void run() {
		if (runToggle==true) {
			update();
			//flock();//flock contains three functions, separation, cohesion, and alignment.
			display();
			dropTrail(every, 100);
			///ATTRACT TOWARDS EVERY MESH POINT     
			//attractFaces(.015);
			///ATTRACT TOWARDS POINT WITH HIGHEST SCORE WITHIN FIEL OF VIEW (FOVSCORE)
			moveOnSrf(.05f);
			followTrails(.05f);
		}
		drawTrail(50);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// update() a function for updating the loc of the object.
	void update() {
		vel.addSelf(acc);//add the vector for velocity to the acceleration.
		vel.limit(maxvel);//limit the maximum speed. (remember we declared the variable maxvel before.
		loc.addSelf(vel);//add the velocity to the loc. The most basic definition of motion is a velocity vector added to a loc vector.
		acc.clear();//clear the vector for the acceleration.
	}
	//the display function will show an ellipse by accessing the loc vector's coordinates.
	void display() {
		parent.stroke(0, 250, 0);//fill color, a dark green.
		parent.strokeWeight(2);//no stroke for the shape.
		parent.point(loc.x, loc.y, loc.z);//an ellipse with center in the loc vector's coordinates, and 4 units wide and 4 units tall.
	}

	void dropTrail(int every, int limit) {
		if (parent.frameCount == 1 || parent.frameCount % every == 0) {
			trail.add(loc.copy());
			while (trail.size () >= limit+1)
				trail.remove(0);
		}
	}

	void drawTrail(float thresh) {
		if (trail.size() > 1) {
			//		      for (int i = 1; i < trail.size(); i++) {
			//		        Vec3D v1 = (Vec3D) trail.get(i);
			//		        Vec3D v2 = (Vec3D) trail.get(i-1);
			//
			//		        float d = v1.distanceTo(v2);
			//		        if (d < thresh) {
			//		          stroke(30, 20*i);
			//		          strokeWeight(1);
			//		          line(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
			//		        }
			//		      }
//			parent.stroke(0.5f);
//			parent.strokeWeight(1);
//			for (Vec3D v : trail) {
//				parent.pushMatrix();
//				parent.translate(v.x(), v.y(), v.z());
//				parent.box(5);
//				parent.popMatrix();
//			}
			parent.stroke(0, 255, 0);
			parent.strokeWeight(5);
//			for (Vec3D v : trail) {
//				parent.point(v.x, v.y, v.z);
//				brush.setSize(0.3f);
//				brush.drawAtAbsolutePos(v, .9f);
//			}
			for (int i = 0; i < trail.size() - 4; i += 4) {
				Vec3D v = trail.get(i);
				parent.point(v.x(), v.y(), v.z());
				brush.setSize(1f);
				brush.drawAtAbsolutePos(v, 1.0f);
			}
		}
	}

	///////////////////////////////////////
	void moveOnSrf(float magnitude) {
		int highID=0;
		float thisScore = scores[ID];
		float localHighScore=0;

		for (int i=0; i< vertices.size(); i++) {
			Vert c = (Vert) vertices.get(i);
			Vec3D ctr = c.getLocation();
			float distance=loc.distanceTo(ctr);    
			if (distance>0 && distance<fovScore) {
				float otherScore = scores[i];
				if (otherScore < thisScore && otherScore != thisScore) {
					highID = i;
					thisScore=otherScore;
					localHighScore=scores[i];          
					Vert localMax = (Vert) vertices.get(highID);
					float dis= loc.distanceTo(localMax.getLocation());
					if (thisScore==localHighScore && dis<10) {
						runToggle=false;
					}
				}
			}
		}

		Vert v = (Vert) vertices.get(highID);
		Vec3D target = v.getLocation();
		Vec3D steeringVector = steer(target, false);
		steeringVector.scaleSelf(magnitude);
		acc.addSelf(steeringVector);
	}

	/////////////////////////////////////// FOLLOW TRAILS OF ATHER AGENTS  
	void followTrails(float magnitude) {
		int cloAID=0;
		int cloTID=0;
		float cloDist = 1000;
		Vec3D closestTrail=new Vec3D();
		Vec3D closestTrailFWD= new Vec3D();
		Vec3D steer=new Vec3D();
		for (int i=0; i<agents.size();i++) {
			Agent myAgent = (Agent) agents.get(i);
			if (myAgent!=this) {
				if (myAgent.trail.size()>3) {
					for (int j=0; j<myAgent.trail.size();j++) {
						Vec3D trail = (Vec3D) myAgent.trail.get(j);
						float distance= loc.distanceTo(trail);
						if (distance<cloDist) {
							cloAID=i;
							cloTID=j;
							cloDist=distance;
						}
					}
				}
			}
		}
		if (trail.size()>3) {
			Agent cloA = (Agent) agents.get(cloAID);
			Vec3D temp = (Vec3D) cloA.trail.get(cloTID);
			closestTrail=temp;

			if (cloTID<cloA.trail.size()-1) {
				Vec3D temp2 = (Vec3D) cloA.trail.get(cloTID+1); 
				closestTrailFWD=temp2;
			}
			else if (cloTID==cloA.trail.size()-1) {
				Vec3D temp3 = (Vec3D) cloA.trail.get(cloTID);
				closestTrailFWD=temp3;
			}

			//Vec3D mid =new Vec3D(((closestTrail.x+closestTrailFWD.x)/2), ((closestTrail.y+closestTrailFWD.y)/2), ((closestTrail.z+closestTrailFWD.z)/2));
			Vec3D mid= getNormalPoint(loc, closestTrail, closestTrailFWD);
//			parent.strokeWeight(1);
//			parent.stroke(0, 0, 255);
//			line(loc.x, loc.y, loc.z, mid.x, mid.y, mid.z);
			float distance=loc.distanceTo(mid);
			if (distance<50) {
				seek(mid, magnitude);
				Vec3D heading = new Vec3D(closestTrailFWD.x-closestTrail.x, closestTrailFWD.y-closestTrail.y, closestTrailFWD.z-closestTrail.z);
				steer.addSelf(heading);

				//////////////////// //////KILLING THE AGENT WHEN IT IS CLOSE TO THE TRAIL
				if (distance<1) {
					runToggle=false;
				}
			}
			steer.scaleSelf(magnitude);
			acc.addSelf(steer);
		}
	}
	
	Vec3D getNormalPoint(Vec3D p, Vec3D a, Vec3D b) {
		Vec3D ap = p.sub(a);
		Vec3D ab = b.sub(a);
		ab.normalize();
		// Project vector "diff" onto line by using the dot product
		ab.scaleSelf(ap.dot(ab));
		Vec3D normalPoint = a.add(ab);
		return normalPoint;
	}
	void seek(Vec3D target, float factor) {
		Vec3D v = steer(target, false);
		v.scaleSelf(factor);
		acc.addSelf(v);
	}
	Vec3D steer(Vec3D target, boolean slowdown) {
		Vec3D steer; 
		Vec3D desired = target.sub(loc);  
		float d = desired.magnitude();  
		if (d > 0) {
			desired.normalize();
			if ((slowdown) && (d < 100.0f)) desired.scaleSelf(maxvel*(d/100.0f));
			else desired.scaleSelf(maxvel);
			steer = desired.sub(vel).limit(maxForce);
		} 
		else {
			steer = new Vec3D();
		}
		return steer;
	}
	
	ArrayList<Vec3D> getTrail() {
		return trail;
	}
}
