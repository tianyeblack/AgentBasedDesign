package agentstrails;

import java.util.ArrayList;

import processing.core.*;
import toxi.geom.Vec3D;
import toxi.volume.VolumetricBrush;

class Agent {
	PApplet parent;
	VolumetricBrush brush;
	
	int ID;
	Vec3D loc;// loc.
	Vec3D vel;
	Vec3D acc = new Vec3D();// acceleration.
	Vec3D start;

	float maxvel = 3;// A floating variable for the maximum speed.
	float maxForce = 20;
	boolean runToggle = true;



	// drop trail interval
	int every = 8;
	// max number of trails
	int trailNum = 200;

	ArrayList<Vec3D> trail;
	ArrayList<Vert> vertices;
	ArrayList<Agent> agents;

	float[] scores;
	// basic flocking functions magnitude
	float alignment = .1f;
	float cohesion = .9f;
	float separation = 100f;
	
	//added behavior functions' magnitude
	float faceAttraction = 30f;
	float onSrfMotion=30f;
	float trailFollow=.05f;
	
	//Field Of View for alignment function
	int fovAlign=1;
	//Field Of View for cohesion function
	int fovCoh = 2;
	//Field Of View for separation function
	int fovSep = 3;
	//field of view for Sun Analysis
	int fovScore = 300;
	//distance between agents
	int overkillDist=5;
	
	
	float thresh = 50;
	float isoBrushSize;
	float isoBrushDensity= .1f;
	float agentBoxSize = 10;
	
	String agentType;

	Agent(ArrayList<Vert> vs, ArrayList<Agent> as, float[] ss, int _ID,VolumetricBrush b, PApplet p, String _agentType) {// class(parameters)
			
		parent = p;
		brush = b;
		vertices = vs;
		agents = as;
		scores = ss;
		ID = _ID;
		agentType= _agentType;
		loc = vertices.get(ID).getLocation();
		start = loc.copy();
		if (agentType.equals("a")) {
			vel = new Vec3D( parent.random(-2, 2), parent.random(-2, 2),parent.random(0, 2));// velocity.
		}else if (agentType.equals("b")){
			vel = new Vec3D( parent.random(-4, 4), parent.random(-4, 4),0);// velocity.
		}else if (agentType.equals("c")){
			vel = new Vec3D( parent.random(-4, 4), parent.random(-4, 4),0);// velocity.
		}
		trail = new ArrayList<Vec3D>();
	}

	// /////////////////////////////
	// FUNCTIONS

	// run() A function that encompasses all functions.We use this function in order to group the other functions.
	

	void run() {
		if (runToggle == true) {
			update();
			display();
			if (agentType.equals("a") ||agentType.equals("b")) {
			flock();// flock contains three functions, separation, cohesion, alignment.	
			AgentConnection();
			}
	
			//ATTRACT TOWARDS EVERY MESH POINT
			attractFaces(faceAttraction);
			
			if (agentType.equals("c") ) {
			//ATTRACT TOWARDS POINT WITH HIGHEST SCORE WITHIN FIELD OF VIEW->FOVSCORE
			 moveOnSrf(onSrfMotion);
			 followTrails(trailFollow);
			}
			}
		dropTrail(every, trailNum);
		drawTrail(thresh);
		// drawBoxes(thresh);
	}

	
	//////////////////////FLOCK
	void flock() {
		separation(separation);
		cohesion(cohesion);
		alignment(alignment);
	}
	
	
	  void AgentConnection() {
		    
			
			for (Agent a : agents) { 
	         
	            float obDist = loc.distanceTo(a.loc);
	            if ((obDist>10) && (obDist<20)) {
	             // a.tag ="BB";
	              //vel.scaleSelf(0.9f);
	              parent.strokeWeight(1);
	              parent.stroke(150, 80);
	              parent.line(loc.x, loc.y, loc.z, a.loc.x, a.loc.y, a.loc.z);
	            
	            }
	        
	      }
	    
	  }
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// update() a function for updating the locations of the agents.
	void update() {
		vel.addSelf(acc);// add the vector for velocity to the acceleration.
		vel.limit(maxvel);// limit the maximum speed. (remember we declared the variable maxvel before.							
		loc.addSelf(vel);// add the velocity to the loc. The most basic definition of motion is a velocity vector added to a loc vector.						
		acc.clear();// clear the vector for the acceleration.
		}

	// the display function will show an ellipse by accessing the loc vector's coordinates.
	
	void display() {
		if (this.agentType.equals("a")) {
		parent.stroke(0, 250, 0);// fill color, a dark green.
		parent.strokeWeight(4);// no stroke for the shape.
		parent.point(loc.x, loc.y, loc.z);// an ellipse with center in the loc vector's coordinates, and 4 units wide and 4 units tall.
		}
		if (this.agentType.equals("a")) {
			parent.stroke(0,0,250);// fill color, a dark green.
			parent.strokeWeight(4);// no stroke for the shape.
			parent.point(loc.x, loc.y, loc.z);// an ellipse with center in the loc vector's coordinates, and 4 units wide and 4 units tall.
			}
		
		if (this.agentType.equals("c")) {
			parent.stroke(255, 255, 0);// fill color, a dark green.
			parent.strokeWeight(1);// no stroke for the shape.
			parent.point(loc.x, loc.y, loc.z);// an ellipse with center in the loc vector's coordinates, and 4 units wide and 4 units tall.
		}
		
	}



	/////////////////////////////////////////////////////////////////////////////
	// keep a log of the agents' motion (points) in a certain interval

	void dropTrail(int every, int limit) {
		if (parent.frameCount == 1 || parent.frameCount % every == 0) {
			trail.add(loc.copy());
			while (trail.size() >= limit + 1)
				trail.remove(0);
		}
		// ///////////////////////////////////
		// ////////Drop a box around each left trail
	}

	void drawBoxes(float thresh) {
		if (trail.size() > 1) {
			//parent.stroke(0, 0, 255);
			//parent.strokeWeight(2.0f);
			for (int i = 1; i < trail.size(); i++) {
				Vec3D v1 = (Vec3D) trail.get(i);
				Vec3D v2 = (Vec3D) trail.get(i - 1);

				float d = v1.distanceTo(v2);
				if (d < thresh) {
					parent.stroke(30, 20 * i);
					parent.strokeWeight(1);
					parent.line(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
				}
			}
			
			parent.stroke(0.5f);
			parent.strokeWeight(1);
			for (Vec3D v : trail) {
				parent.pushMatrix();
				parent.translate(v.x(), v.y(), v.z());
				if (this.agentType == "a"){
					parent.fill(255,0,0);
					parent.box(agentBoxSize*1.3f);
				}else if (this.agentType == "b"){
					parent.fill(20);
					parent.box(agentBoxSize);
				}else if (this.agentType == "c"){
					parent.fill(200);
					parent.box(agentBoxSize*.9f);
				}
				
				parent.popMatrix();
			}
		}

	}

	/////////////////////////////////////
	// Draw trails
	void drawTrail(float thresh) {
		if (trail.size() > 1) {
			if(agentType=="a"){
				parent.stroke(0, 255, 0);
				parent.strokeWeight(2.0f);
			}else if(agentType=="b"){
				parent.stroke(255, 255, 0);
				parent.strokeWeight(1.0f);
			}else if(agentType=="c"){		
				parent.stroke(0, 0, 255);
				parent.strokeWeight(1.0f);
				
			}
			for (Vec3D v : trail) {
		
				parent.point(v.x, v.y, v.z);
				brush.setSize(isoBrushSize);
				brush.drawAtAbsolutePos(v, isoBrushDensity);
			
				
			}

		}
	}

	///////////////////////////////////////
	void moveOnSrf(float magnitude) {
		int highID = 0;
		float thisScore = scores[ID];
		float localHighScore = 0;

		for (int i = 0; i < vertices.size(); i++) {
			Vert c = (Vert) vertices.get(i);
			Vec3D ctr = c.getLocation();
			float distance = loc.distanceTo(ctr);
			if (distance > 0 && distance < fovScore) {
				float otherScore = scores[i];
				if (otherScore < thisScore && otherScore != thisScore) {
					highID = i;
					thisScore = otherScore;
					localHighScore = scores[i];
					Vert localMax = (Vert) vertices.get(highID);
					float dis = loc.distanceTo(localMax.getLocation());
					if (thisScore == localHighScore && dis < 10) {
						runToggle = false;
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

	// ///////////////////////////////////// ATTRACT FACES
	void attractFaces(float magnitude) {

		for (int i = 0; i < vertices.size(); i++) {
			Vert c = (Vert) vertices.get(i);
			Vec3D ctr = c.getLocation();
			float distance = loc.distanceTo(ctr);

			if (distance > 0 && distance < 40) {
				Vec3D steeringVector = steer(ctr, false);
				steeringVector.normalizeTo(1 / distance);
				steeringVector.scaleSelf(scores[i]);
				acc.addSelf(steeringVector);
			}
		}

		Vec3D sum = new Vec3D();
		int count = 0;
		for (int i = 0; i < vertices.size(); i++) {
			Vert c = (Vert) vertices.get(i);
			Vec3D ctr = c.getLocation();
			float distance = loc.distanceTo(ctr);
			if (distance > 0 && distance < 50) {
				sum.addSelf(ctr);
				count++;
			}
		}
		if (count > 0) {
			sum.scaleSelf(1.0F / count);
		}
		Vec3D steer = sum.sub(loc);
		steer.scaleSelf(magnitude);
		acc.addSelf(steer);
	}

	// ///////////////////////////////

	// ///////////////////////////////////// FOLLOW TRAILS OF oTHER AGENTS
	void followTrails(float magnitude) {
		int cloAID = 0;
		int cloTID = 0;
		float cloDist = 1000;
		Vec3D closestTrail = new Vec3D();
		Vec3D closestTrailFWD = new Vec3D();
		Vec3D steer = new Vec3D();
		for (int i = 0; i < agents.size(); i++) {
			Agent myAgent = (Agent) agents.get(i);
			if (myAgent != this) {
				if (myAgent.trail.size() > 3) {
					for (int j = 0; j < myAgent.trail.size(); j++) {
						Vec3D trail = (Vec3D) myAgent.trail.get(j);
						float distance = loc.distanceTo(trail);
						if (distance < cloDist) {
							cloAID = i;
							cloTID = j;
							cloDist = distance;
						}
					}
				}
			}
		}
		if (trail.size() > 3) {
			Agent cloA = (Agent) agents.get(cloAID);
			Vec3D temp = (Vec3D) cloA.trail.get(cloTID);
			closestTrail = temp;

			if (cloTID < cloA.trail.size() - 1) {
				Vec3D temp2 = (Vec3D) cloA.trail.get(cloTID + 1);
				closestTrailFWD = temp2;
			} else if (cloTID == cloA.trail.size() - 1) {
				Vec3D temp3 = (Vec3D) cloA.trail.get(cloTID);
				closestTrailFWD = temp3;
			}

			// Vec3D mid =new Vec3D(((closestTrail.x+closestTrailFWD.x)/2),
			// ((closestTrail.y+closestTrailFWD.y)/2),
			// ((closestTrail.z+closestTrailFWD.z)/2));
			Vec3D mid = getNormalPoint(loc, closestTrail, closestTrailFWD);
			// parent.strokeWeight(1);
			// parent.stroke(0, 0, 255);
			// line(loc.x, loc.y, loc.z, mid.x, mid.y, mid.z);
			float distance = loc.distanceTo(mid);
			if (distance < 50) {
				seek(mid, magnitude);
				Vec3D heading = new Vec3D(closestTrailFWD.x - closestTrail.x,
						closestTrailFWD.y - closestTrail.y, closestTrailFWD.z
								- closestTrail.z);
				steer.addSelf(heading);

				// ////////////////////////KILLING THE AGENT WHEN IT IS CLOSE TO
				// THE TRAIL
				if (distance < overkillDist) {
					runToggle = false;
				}
			}
			steer.scaleSelf(magnitude);
			acc.addSelf(steer);
		}
	}
	// ///////////////////////////////////// GET NORMALS
	Vec3D getNormalPoint(Vec3D p, Vec3D a, Vec3D b) {
		Vec3D ap = p.sub(a);
		Vec3D ab = b.sub(a);
		ab.normalize();
		// Project vector "diff" onto line by using the dot product
		ab.scaleSelf(ap.dot(ab));
		Vec3D normalPoint = a.add(ab);
		return normalPoint;
	}
	// ///////////////////////////////////// SEEK for target
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
			if ((slowdown) && (d < 100.0f))
				desired.scaleSelf(maxvel * (d / 100.0f));
			else
				desired.scaleSelf(maxvel);
			steer = desired.sub(vel).limit(maxForce);
		} else {
			steer = new Vec3D();
		}
		return steer;
	}

	ArrayList<Vec3D> getTrail() {
		return trail;
	}

	//////////////// BASIC FLOCKING
	/////////////// ALIGNMENT

	void alignment(float magnitude) {
		// the magnitude parameter will be assigned once we call the function.
		// In this case it will be the global variable for alignment.
		Vec3D steer = new Vec3D();// We are creating a new empty vector to be added to the agent's acceleration.								
		// This will be translated into a change in direction based on the calculations.
		
		int count = 0;// we create a variable called count. It will tell us how many times we have run through the for loop.
						
		for (int i = 0; i < agents.size(); i++) {// a for loop to access all the other agents.
													
			Agent otherAgent = (Agent) agents.get(i);// getting the other agents. We are creating an instance of the Agent class and calling all the other agents into it
												
			float distance = loc.distanceTo(otherAgent.loc);// we evaluate the distance between our own position, and the other agent.
			// (remember this will be done for each of the other agents in the population.)
			
			if (distance > 0 && distance < fovAlign) {// based on that distance, we will activate alignment anytime the agent goes close to any of the rest.								
				steer.addSelf(otherAgent.vel);// the temporary vector created,is added to the other agents' velocity, basically copying its data.
											 // the other agents' velocity tell us where it is going, since the velocity is added to the loc
				count++;
			}
		}
		if (count > 0) {// anytime the count is greater than zero...
			steer.scaleSelf(1.0f / count);// scale the steering vector based on how many times we have added the
			// steering vector.
											
		}
		steer.scaleSelf(magnitude);// We are now multiplying the steering vector
									// times the global variable for alignment.
		acc.addSelf(steer);// Finally, we add the steering vector to the acceleration, effecting change in direction based on all the other						
		// agents' velocity. This operations will make all the agents start to move toward the same direction over time.

	}

	// //////////////////////////////////////////////////////////////////////////////////////////

	/////////////// COHESION

	void cohesion(float magnitude) {// cohesion means that the agents will try
									// to move close to each other.
		Vec3D sum = new Vec3D();// we create an empty vector. It is called sum
								// because in the end it will contain the sum of
								// all other agents' locs.
		int count = 0;// a variable that tells us how many times we've ran
						// through the for loop.
		// a for loop to acces the other agents
		for (int i = 0; i < agents.size(); i++) {
			Agent otherAgent = (Agent) agents.get(i);// we get the other agents.
			float distance = loc.distanceTo(otherAgent.loc);// we evaluate the
															// distance to the
															// other agents.
			if (distance > 0 && distance < fovCoh) {// based on that distance,we will activate cohesion	anytime the agent goes	close to any of the rest.


												
				sum.addSelf(otherAgent.loc);// we add the other agents' loc to
											// the empty vector created.
				// this basically gives us an average of all the other agents'
				// loc. We will then move the agent toward this point.
				count++;// update the count. Add 1.
			}
		}
		if (count > 0) {
			sum.scaleSelf(1.0f / count);// we scale the sum vector, so that the
										// effect dissolves over time.
		}
		Vec3D steer = sum.sub(loc);// we create a steering vector. It is the
									// subtraction of the sum vector from the
									// current loc of the agent.
		// this basically makes a vector that points toward the sum point.
		steer.scaleSelf(magnitude);// we scale it based on the global variable
									// for cohesion.
		acc.addSelf(steer);// now we add the steering vector to the
							// acceleration, resulting in a change of direction
							// that brings the agents together.
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// ///////////// SEPARATION
	void separation(float magnitude) {// separation is a function so that the
										// agents do not crash into each other.
		Vec3D steer = new Vec3D();// again, an empty steering vector.
		int count = 0;// again, a count variable to tell us how many times we've
						// run through the cycle.
		// a for loop to access all the other agents in the ArrayList.
		for (int i = 0; i < agents.size(); i++) {
			Agent otherAgent = (Agent) agents.get(i);// getting the agents.
			float distance = loc.distanceTo(otherAgent.loc);// evaluating the
															// distance to the
															// other agents.
			if (distance > 0 && distance < fovSep) {// if we get too close...
				Vec3D diff = loc.sub(otherAgent.loc);// then create a difference
														// vector. it is the
														// current loc, minus
														// the other agent's
														// loc.
				// this gives us a vector that points away from the other
				// agents.
				diff.normalizeTo(1.0f / distance);// the vector will be
													// normalized to one over
													// the distance evaluated.
				// This helps to control the effect the difference vector will
				// have on the steering by reducing its magnitude.
				steer.addSelf(diff);// we now add the difference vector to the
									// empty steering vector.
				count++;// add one to the count.
			}
		}
		if (count > 0) {
			steer.scaleSelf(1.0f / count);// scale the steering vector based on
											// the count.
		}
		steer.scaleSelf(magnitude);// scale the steering vector based on the
									// global variable for separation.
		acc.addSelf(steer);// finally, add the steering vector to the
							// acceleration.
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
