package simulation.robot.sensors;

import java.util.ArrayList;
import java.util.Random;
import mathutils.Vector2d;
import simulation.Simulator;
import simulation.environment.Environment;
import simulation.physicalobjects.ClosePhysicalObjects;
import simulation.physicalobjects.ClosePhysicalObjects.CloseObjectIterator;
import simulation.physicalobjects.GeometricCalculator;
import simulation.physicalobjects.GeometricInfo;
import simulation.physicalobjects.PhysicalObject;
import simulation.physicalobjects.PhysicalObjectDistance;
import simulation.physicalobjects.checkers.AllowObstacleChecker;
import simulation.physicalobjects.checkers.AllowedObjectsChecker;
import simulation.robot.Robot;
import simulation.util.Arguments;

public abstract class ConeTypeSensor extends Sensor {

	public static final float NOISESTDEV = 0.05f; 
	public static final int DEFAULT_RANGE = 1;

	protected   ClosePhysicalObjects closeObjects;
	protected double 			   range;
	protected double[]             readings;
	protected double[] 			   angles;
	protected int 				   numberOfSensors;
	protected Vector2d 			   sensorPosition 	= new Vector2d();
	protected double openingAngle = 90;

	protected Environment env;
	protected Double time;
	protected GeometricCalculator geoCalc;
	protected Random random;
	
	protected ClosePhysicalObjects closeObstacles;
	protected boolean checkObstacles = false;
	protected double[] obstacleReadings;
	
	public ConeTypeSensor(Simulator simulator, int id, Robot robot, Arguments args) {
		super(simulator,id, robot, args);
		this.geoCalc = simulator.getGeoCalculator();
		this.env = simulator.getEnvironment();
		this.time = simulator.getTime();
		this.random = simulator.getRandom();
		
		numberOfSensors = (args.getArgumentIsDefined("numbersensors")) ? args.getArgumentAsInt("numbersensors") : 1;
		range = (args.getArgumentIsDefined("range")) ? args.getArgumentAsDouble("range") : 1;
		openingAngle = Math.toRadians((args.getArgumentIsDefined("angle")) ? args.getArgumentAsDouble("angle") : 90);
		
		checkObstacles = args.getArgumentAsIntOrSetDefault("checkobstacles",0) == 1;
		
		this.readings 		= new double[numberOfSensors];
		this.angles 		= new double[numberOfSensors];
		setupPositions(numberOfSensors);
		
		if(checkObstacles) {
			setAllowedObstaclesChecker(new AllowObstacleChecker(robot.getId()*100));
			this.obstacleReadings = new double[numberOfSensors];
		}
	}
	
	public void setAllowedObstaclesChecker(AllowedObjectsChecker aoc) {
		if(aoc != null)
			this.closeObstacles = new ClosePhysicalObjects(env, time, range,aoc);
	}
	
	public void setAllowedObjectsChecker(AllowedObjectsChecker aoc) {
		if(aoc != null)
			this.closeObjects 	= new ClosePhysicalObjects(env, time,range,aoc);
	}
	
	public void setupPositions(Vector2d[] positions) {
		Vector2d frontVector = new Vector2d(1,0); 
		for (int i=0;i< numberOfSensors;i++){
			Vector2d v = positions[i];
			angles[i] = (v.getY()<0?-1:1)*v.angle(frontVector);
		}
	}
	
	public void setupPositions(int numberSensors) {
		double delta = 2 * Math.PI / numberSensors;
		double angle = 0;
		for (int i=0;i< numberSensors;i++){
			angles[i] = angle;
			angle+=delta;
		}
	}

	public double[] getAngles() {
		return angles;
	}
	
	public ClosePhysicalObjects getCloseObjects() {
		return closeObjects;
	}

	public double getRange() {
		return range;
	}
	
	public double getOpeningAngle() {
		return openingAngle;
	}

	public void update(double time, ArrayList<PhysicalObject> teleported) {
		if(closeObjects != null)
			closeObjects.update(time, teleported);
		try { 
			for(int j = 0; j < readings.length; j++){
				readings[j] = 0.0;
			}
			CloseObjectIterator iterator = getCloseObjects().iterator();
			while(iterator.hasNext()){
				PhysicalObjectDistance source=iterator.next();
				if (source.getObject().isEnabled()){
					calculateSourceContributions(source);
					iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(
							sensorPosition, source.getObject(), time));
				}
			}
			
			if(checkObstacles)
				checkObstacles(time, teleported);

		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}
	
	protected void checkObstacles(double time, ArrayList<PhysicalObject> teleported) {
		for(int j = 0; j < obstacleReadings.length; j++){
			obstacleReadings[j] = 0.0;
		}
		
		if(closeObstacles != null)
			closeObstacles.update(time, teleported);
		CloseObjectIterator iterator = closeObstacles.iterator();
		while(iterator.hasNext()){
			PhysicalObjectDistance source=iterator.next();
			if (source.getObject().isEnabled()){
				calculatedObstacleContributions(source);
				iterator.updateCurrentDistance(this.geoCalc.getDistanceBetween(sensorPosition, source.getObject(), time));
			}
		}
	}

	public double getSensorReading(int sensorNumber){
		return readings[sensorNumber];
	}
	
	protected void calculatedObstacleContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < obstacleReadings.length; j++){
			obstacleReadings[j] = Math.max(calculateContributionToSensor(j, source)*(1 + random.nextGaussian()* NOISESTDEV), readings[j]);
		}
	}

	protected void calculateSourceContributions(PhysicalObjectDistance source) {
		for(int j = 0; j < readings.length; j++){
			readings[j] = Math.max(calculateContributionToSensor(j, source)*(1 + 
					random.nextGaussian()* NOISESTDEV), readings[j]);
		}
	}

	protected GeometricInfo getSensorGeometricInfo(int sensorNumber,
			PhysicalObjectDistance source) {
		double orientation=angles[sensorNumber]+robot.getOrientation();
//		sensorPosition.set(Math.cos(orientation)*robot.getRadius()+robot.getPosition().getX(),
//				Math.sin(orientation)*robot.getRadius()+robot.getPosition().getY());
		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetween(sensorPosition, 
				orientation, source.getObject(), time);
		return sensorInfo;
	}

	protected GeometricInfo getSensorGeometricInfo(int sensorNumber, Vector2d toPoint){
		double orientation=angles[sensorNumber]+robot.getOrientation();
		sensorPosition.set(robot.getPosition().getX(), robot.getPosition().getY());
		GeometricInfo sensorInfo = geoCalc.getGeometricInfoBetweenPoints(
				sensorPosition, orientation, toPoint, time);
		return sensorInfo;
	}
	
	protected abstract double calculateContributionToSensor(int i, PhysicalObjectDistance source);

	public int getNumberOfSensors() {
		return numberOfSensors;
	}
	
	public void setRange(double range) {
		this.range = range;
	}
	
	public void setOpeningAngle(double openingAngle) {
		this.openingAngle = openingAngle;
	}
}