package com.warfactory.curvesandpolygons.model;

import java.util.ArrayList;

import javax.vecmath.Point2d;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

//a layer of polygon/curve
public abstract class Loop{

	protected ArrayList<Point2d> points;
	protected int color;
	protected Path path = new Path();
	
	public Loop(int numPoints){
		setPoints(new ArrayList<Point2d>());
		for (int i=0; i<numPoints; i++){
			getPoints().add(new Point2d());
		}
	}
	
	public abstract void draw(Canvas canvas, Paint paint) ;

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public ArrayList<Point2d> getPoints() {
		return points;
	}

	public void setPoints(ArrayList<Point2d> points) {
		this.points = points;
	}
}
