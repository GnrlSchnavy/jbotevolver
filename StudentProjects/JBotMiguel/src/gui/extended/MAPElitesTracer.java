package gui.extended;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;

import mathutils.Vector2d;
import multiobjective.MOChromosome;
import novelty.BehaviourResult;
import novelty.ExpandedFitness;
import novelty.results.VectorBehaviourExtraResult;
import novelty.results.VectorBehaviourResult;
import simulation.Simulator;
import simulation.physicalobjects.Marker;
import simulation.util.Arguments;
import updatables.Tracer;
import evaluationfunctions.OrientationEvaluationFunction;
import evolution.MAPElitesPopulation;

public class MAPElitesTracer extends Tracer {
	
	public MAPElitesTracer(Arguments args) {
		super(args);
	}
	
	public void drawMapElites(Simulator sim, MAPElitesPopulation pop) {
		
		setup(sim);
		width = 1;
		height = 1;
		
		Graphics2D g = createCanvas(sim);
		
		drawGrid(g);
		
		for(int x = 0 ; x < pop.getMap().length ; x++) {
			for(int y = 0 ; y < pop.getMap()[x].length ; y++) {
				
				MOChromosome res = pop.getMap()[x][y];
				
				if(res != null) {
					
					ExpandedFitness fit = (ExpandedFitness)res.getEvaluationResult();
					BehaviourResult br = (BehaviourResult)fit.getCorrespondingEvaluation(1);
					
					if(br instanceof VectorBehaviourExtraResult) {
						double[] behavior = (double[])br.value();
						Vector2d pos = new Vector2d(behavior[0],behavior[1]);
						
						double orientation = ((VectorBehaviourExtraResult)br).getExtraValue();
						
						double fitness = OrientationEvaluationFunction.calculateOrientationFitness(pos, orientation);
						
						int[] supposedLocation = pop.getLocationFromBehaviorVector(behavior);
						
						Marker m;
						
						//switch x and y, and angle
						orientation+=Math.PI/2;
						orientation*=-1;
						orientation+=Math.PI;
						pos.x = (y-pop.getMap()[x].length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						pos.y = (x-pop.getMap().length/2)*pop.getMapResolution()+pop.getMapResolution()/2;
						
						if(supposedLocation[0] != x || supposedLocation[1] != y) {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, Color.BLACK);
						} else {
							m = new Marker(sim, "m", pos.x, pos.y, orientation, 0.05, 0.02, getColor(fitness));
						}
						
						drawMarker(g,m);
					} else {
						if(br instanceof VectorBehaviourResult) {
							double[] behavior = (double[])br.value();
							Vector2d pos = new Vector2d(behavior[0],behavior[1]);
							
							Marker m = new Marker(sim, "m", pos.x, pos.y, Math.PI, 0.01, 0, Color.RED);
							drawMarker(g,m);
						}
					}
				}
			}
		}
		
		writeGraphics(g,sim,"");
	}
	
	protected void drawGrid(Graphics2D g) {
		double inc = 0.05;
		float lineWidth = 0.5f;

		Stroke original = g.getStroke();
		BasicStroke dashed = new BasicStroke(lineWidth,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                2.0f, new float[]{1f}, 0.0f);
        g.setStroke(dashed);
        
        double limit = 0.4;
        
        DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
        
        g.setFont(new Font("Arial", Font.PLAIN, 15)); 
        
		for(double y = -limit, count = 0 ; y <= limit ; y+=inc) {
			IntPos a = transform(-limit, y);
			IntPos b = transform(limit, y);
			g.setColor(Color.GRAY);
			g.drawLine(a.x, a.y, b.x, b.y);
			
			double d = new Double(df2.format(y)).doubleValue();

			if(d == -0) d = 0;
			
			if(count++ % 2 != 0 && d != 0 || d < -(limit)+inc/2.0)
				continue;
			
			int xs = -26;
			int xy = 5;
			
			if(d < 0)
				xs-=2;
			g.setColor(Color.BLACK);
			g.drawString(""+d, a.x+xs, a.y+xy);
		}
		
		for(double x = -limit, count = 0 ; x <= limit ; x+=inc) {
			IntPos a = transform(x, -limit);
			IntPos b = transform(x, limit);
			g.setColor(Color.GRAY);
			g.drawLine(a.x, a.y, b.x, b.y);

			double d = new Double(df2.format(x)).doubleValue();
			
			if(d == -0) d = 0;
			
			if(count++ % 2 != 0 && d != 0.0 || d < -(limit)+inc/2.0)
				continue;
			
			int xs = -9;
			int xy = 16;
			
			if(d < 0)
				xs-=2;
			g.setColor(Color.BLACK);
			g.drawString(""+d, a.x+xs, a.y+xy);
		}
		
		IntPos a = transform(-limit, -limit);
		g.drawString("-"+new Double(df2.format(limit)).doubleValue(), a.x-26, a.y+16);
		
		a = transform(0, -limit);
		
		g.setFont(new Font("Arial", Font.PLAIN, 22));
		g.drawString("Lateral displacement (m)", a.x-130, a.y+40);
		
		a = transform(0, limit);
		
		AffineTransform orig = g.getTransform();
		g.rotate(-Math.PI/2);
		g.drawString("Forward displacement (m)", -a.x-130, a.y-40);
		g.setTransform(orig);
		
//		Forward displacement (m)
//		Lateral displacement (m)
			
		g.setStroke(original);
	}
	
	protected void drawMarker(Graphics2D g, Marker m) {
		
		IntPos a = transform(m.getPosition().getX(), m.getPosition().getY());
		
		int markerSize = 2;
		double markerLength = m.getLength();
		
		g.setColor(m.getColor());
		g.drawOval(a.x-markerSize/2, a.y-markerSize/2, markerSize, markerSize);
		
		double orientation = m.getOrientation();
		Vector2d endPoint = new Vector2d(m.getPosition());
		endPoint.add(new Vector2d(markerLength*Math.cos(orientation),markerLength*Math.sin(orientation)));
		
		IntPos end = transform(endPoint.x, endPoint.y);
		
		g.drawLine(a.x, a.y, end.x, end.y);
	}
	
	private Color getColor(double fitness) {
		Color firstCol = Color.GREEN;
		Color secondCol = Color.RED;
		int R = (int)Math.abs(firstCol.getRed() * fitness + secondCol.getRed()* (1 - fitness));
		int G = (int)Math.abs(firstCol.getGreen() * fitness + secondCol.getGreen()* (1 - fitness));
		int B = (int)Math.abs(firstCol.getBlue() * fitness + secondCol.getBlue()* (1 - fitness));
		
		return new Color(R,G,B);
	}

	@Override
	public void terminate(Simulator simulator) {
		
	}

	@Override
	public void snapshot(Simulator simulator) {
			
	}

}
