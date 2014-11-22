package simulation.environment;

import java.util.LinkedList;

import mathutils.Vector2d;
import controllers.DoNothingController;
import simulation.Simulator;
import simulation.physicalobjects.LightPole;
import simulation.robot.Robot;
import simulation.util.Arguments;

public class GPSEnvironment extends Environment{
	
	protected LinkedList<LightPole> waypoints = new LinkedList<LightPole>();
	protected String waypointCoordinates = "";
	protected boolean rand = false;
	protected int numberOfRobots = 30;
	
	public GPSEnvironment(Simulator simulator, Arguments args) {
		super(simulator,args);
		waypointCoordinates = args.getArgumentAsString("waypoints");
		rand = args.getArgumentAsIntOrSetDefault("random", 0) == 1;
		numberOfRobots = args.getArgumentAsIntOrSetDefault("numberofrobots", numberOfRobots);
	}

	@Override
	public void setup(Simulator simulator) {
		super.setup(simulator);
		
		String[] splitted = waypointCoordinates.split(";");
		int i = 0;
		Vector2d pos = null;
		for(String s : splitted) {
			String[] sp = s.split(",");
			
			double x = Double.parseDouble(sp[0]);
			double y = Double.parseDouble(sp[1]);
			
			if(rand) {
				x= x/2+simulator.getRandom().nextDouble()*x/2;
				y= y/2+simulator.getRandom().nextDouble()*y/2;
			}
			
			pos = new Vector2d(x,y);
			
			LightPole p = new LightPole(simulator, "wp"+i, x, y, 0.1);
			waypoints.add(p);
			addObject(p);
			i++;
		}
		
		for(i = 0 ; i < numberOfRobots ; i++) {
			Robot rr = new Robot(simulator, new Arguments("x=0,y=0"));
			rr.setController(new DoNothingController(simulator, rr, new Arguments("")));
			addRobot(rr);
			double x = pos.getX()*simulator.getRandom().nextDouble();
			double y = -25.0+simulator.getRandom().nextDouble()*50.0;
			rr.teleportTo(new Vector2d(x,y));
		}
		
		for(Robot r : robots)
			r.setOrientation(simulator.getRandom().nextDouble()*Math.PI*2);
	}

	@Override
	public void update(double time) {
		
	}

}