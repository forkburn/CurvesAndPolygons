package com.warfactory.curvesandpolygons.model;

import java.util.ArrayList;

import javax.vecmath.Point2d;

import android.graphics.Canvas;
import android.graphics.Paint;

public class CurveLoop extends Loop {

	// auxiliary points used for drawing curves
	private ArrayList<Point2d> auxPoints;
	
	public CurveLoop(int numPoints) {
		super(numPoints);
		auxPoints = new ArrayList<Point2d>();
		for (int i=0; i<numPoints; i++){
			auxPoints.add(new Point2d());
		}
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		int numPoints = points.size();

		// calculate position of the control points
		for (int i=0; i<numPoints-1; i++){
			// put the control point between the 2 nodes
			auxPoints.get(i).interpolate(points.get(i), points.get(i+1), 0.5f);
		}
		auxPoints.get(numPoints-1).interpolate(points.get(numPoints-1),points.get(0), 0.5f);

		// draw a path along the points on the layer, using control points
		path.reset();
		// start at the first point
		path.moveTo((float)auxPoints.get(0).x, (float)auxPoints.get(0).y);
		for (int i=1; i<numPoints; i++){
			path.quadTo((float)points.get(i).x, (float)points.get(i).y, 
					(float)auxPoints.get(i).x, (float)auxPoints.get(i).y);
		}
		path.quadTo((float)points.get(0).x, (float)points.get(0).y, 
				(float)auxPoints.get(0).x, (float)auxPoints.get(0).y);
		path.close();

		// draw the path to the canvas
		paint.setColor(color);
		canvas.drawPath(path, paint);

	}

}
