package agentstrails;

import java.io.PrintWriter;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.pdf.*;
import peasy.*;
import processing.opengl.*;
import toxi.geom.*;
import toxi.geom.mesh.TriangleMesh;
import toxi.processing.ToxiclibsSupport;
import toxi.volume.*;
import controlP5.*;

@SuppressWarnings({"unused", "serial"})

public class AgentsTrails extends PApplet {
	PeasyCam cam;
	PrintWriter output;
	VolumetricBrush brush;
	VolumetricSpaceArray volume;
	IsoSurface surface;
	TriangleMesh mesh = new TriangleMesh("mesh");
	TriangleMesh meshA = new TriangleMesh("meshA");
	TriangleMesh meshB = new TriangleMesh("meshB");
	ToxiclibsSupport gfx;
	ControlP5 ui;
	
	int counter=0;
	//Mesh variables
	ArrayList<Vert> vertices;
	float[] scores;

	//Agents variables
	ArrayList<Agent> agents;
	int pop=600;
	boolean runToggle = true;
	boolean capture = false;
	boolean record = false;
	boolean strok = false;
	boolean compute = true;

	//Box size
	int bX = 3000;
	int bY = 2000;
	int bZ = 1000;
	//Affects the mesh	
	float ISO= 0.5f;
	//Affects the resolution and the FrameRate
	int GRID = 300;
	// Dimensions of the space we are working
	int DIM = 3000;
	//level where agents of type 1 are being created 
	int creationLevel=50;

	Vec3D SCALE = new Vec3D(DIM, DIM, DIM);
	String agentType;
	
	int type ; 
	
	void createAgents(int type) {
		if (pop>vertices.size()) {
			pop=vertices.size();
			println("agent population is " + pop);  
		}
		//create agents at a certain level	
		if (type==1) {
			int ID;
			for (int i = 0; i<pop; i++) {
				ID=i;
				Vert v = (Vert) vertices.get(ID);
				Vec3D vec = v.getLocation();
				if (vec.z<creationLevel) {
					Agent myAgent= new Agent(vertices, agents, scores, ID, brush, this, "a");//We create an instance of the class. We name it myAgent.
					agents.add(myAgent);//We add the created instance of the Agent class to the ArrayList.
				}
			}
		}
		//create agents from random points of the environment
		if (type==2) {
			for (int i = 0; i<pop; i++) {
				Agent myAgent= new Agent(vertices, agents, scores, (int)(random(vertices.size())), brush, this, "b");//We create an instance of the class. We name it myAgent.
				agents.add(myAgent);//We add the created instance of the Agent class to the ArrayList.
			}
		}
		//create agents from all points of the environment
		if (type==3) {
			for (int i = 0; i<vertices.size(); i++) {
				Agent myAgent= new Agent(vertices, agents, scores, i, brush, this, "c");
				agents.add(myAgent);
			}
		}

		println(agents.size());
	}

	
	///RUN AGEnts
	void runAgents() {
		for (Agent a : agents) a.run();
	}

	
////Export the positions of the agents
	void exportText() {
		output = createWriter("output/agentPositions" + frameCount + ".txt"); 
		int count = 0 ;
		for (Agent a : agents) {
			output.println(a.loc.x + "," + a.loc.y + "," + a.loc.z);
		}
		output.flush();
		output.close();
	}

	/////Import Environment/Geometry
	
	void getGeometry() {

		//import the whole SuRFACE
		String lines[] = loadStrings("catenary_mesh_relaxed_03a.txt");
		println("there are " + lines.length + " lines in the elevation point file...");
		scores = new float[lines.length];
		for (int i=0; i<lines.length;i++) {
			//println(lines[i]);
			String parts[] = split(lines[i], "}");
			parts[0]=parts[0].substring(1);  
			String coordinates[]=split(parts[0], ", ");
			//println("the X is: "+coordinates[0]+"the Y is: "+coordinates[1]+"the Z is: "+coordinates[2]+","+"the score is: "+parts[1]);
			Vert newVert = new Vert(new Vec3D(Float.parseFloat(coordinates[0]), Float.parseFloat(coordinates[1]), Float.parseFloat(coordinates[2])), i, brush, this);
			vertices.add(newVert);
			scores[i]=new Float(parts[1]);
		}
	}
	
/////Display imported Environment

	void displayVerts() {
		for (Vert v : vertices) v. display();
	}

////////////////////////////////////////////////////////////////////////////////////////////
/////Main Program
	
	public void setup() {
		size(1200, 800, OPENGL);
		smooth();
		cam = new PeasyCam(this, 600);
		cam.lookAt(800, -200, 800);

		volume = new VolumetricSpaceArray(SCALE, GRID, GRID, GRID);
		surface = new ArrayIsoSurface(volume);
		brush = new RoundBrush(volume, 3f);

		gfx = new ToxiclibsSupport(this);
		ui = new ControlP5(this);
		ui.setAutoDraw(false);
		//cam.rotateX(-.3*PI);
		//cam.rotateY(PI);
		vertices = new ArrayList<Vert>();
		agents = new ArrayList<Agent>();
		getGeometry();
		println(vertices.size());
		
		
		createAgents(1);
		createAgents(2);
		createAgents(3);

		ui.addSlider("ISO",0,1,ISO,20,20,300,14);
	}

	public void draw() {
		if (capture){
			saveFrame("MSA_agent_cat_Trails" + frameCount + ".png");
		}
		if (record) {
			beginRaw(PDF, "msa_catenary_Srf_trails"+ frameCount+".pdf") ;
		}
		background(255);
		lights();
		displayVerts();
		runAgents();
		
		// A bounding box for a better view 		
		stroke(0, 0, 192);
		strokeWeight(.5f);
		noFill();
		box(bX,bY,bZ);

		if (frameCount % 5 == 0 && compute) {
			surface.reset();
			surface.computeSurfaceMesh(mesh, ISO);
	
		}

		if (strok) {
			stroke(0.1f);
		} else {
				noStroke();
				fill(80);	
				
		}
		gfx.mesh(mesh, true);
	
		
		
		
		if (frameCount == 1 || (frameCount % 20 == 0 && frameCount < 2500)) {
			exportText();
		}
		if (record) {
			endRaw();
			record = false;
		}
		if (ui.window(this).isMouseOver()) {
			cam.setActive(false);
		} else {
			cam.setActive(true);
		}
		gui();
	}

	void gui() {
		hint(DISABLE_DEPTH_TEST);
		cam.beginHUD();
		ui.draw();
		cam.endHUD();
		hint(ENABLE_DEPTH_TEST);
	}

	public void keyPressed() {
		if (key=='s'){
			mesh.saveAsSTL(sketchPath(mesh.name + frameCount+ counter + ".stl"));
			counter = counter + 1;
			println ("Saved Successfull");
		}
		if (key=='e' || key== 'E') {
			// saveFrame("/output/seq-####.jpg");
			saveFrame("MSA_agent_cat_Trails" + frameCount + ".png");
			println("saved a frame");
		}
		if (key=='r') {
			capture= !capture;
		}
		if (key == 'n') {
			for (Agent a : agents) a.runToggle = !a.runToggle;
			runToggle = !runToggle;
		}
		if (key == 'p') {
			record= !record;
		}
		if (key == 'k') {
			strok = !strok;
		}
		if (key == 'c') {
			compute = !compute;
		}
	}

	public static void main(String _args[]) {
		PApplet.main(new String[] { agentstrails.AgentsTrails.class.getName() });
	}
}
