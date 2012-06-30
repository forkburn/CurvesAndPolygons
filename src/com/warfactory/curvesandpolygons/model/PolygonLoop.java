package com.warfactory.curvesandpolygons.model;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PolygonLoop extends Loop {

	public PolygonLoop(int numPoints) {
		super(numPoints);
	}

	@Override
	public void draw(Canvas canvas, Paint paint) {
		paint.setColor(color);
		path.reset();
		path.moveTo((float)points.get(0).x, (float)points.get(0).y);
		
		for (int i=1; i<points.size(); i++){
			path.lineTo((float)points.get(i).x,(float)points.get(i).y);
		}
		path.lineTo((float)points.get(0).x, (float)points.get(0).y);
		path.close();
		
		canvas.drawPath(path, paint);
	}

}
