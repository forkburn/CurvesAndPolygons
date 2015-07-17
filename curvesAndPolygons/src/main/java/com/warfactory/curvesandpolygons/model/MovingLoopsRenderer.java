package com.warfactory.curvesandpolygons.model;

import java.util.ArrayList;
import java.util.Random;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;

public class MovingLoopsRenderer {

	
	protected ArrayList<Loop> loops = new ArrayList<Loop>();
	// a pointer to the current loop we are drawing on
	protected int currentLoop;


	// the invisible moving nodes on the screen
	protected class MovingNode{
		public Point2d pos = new Point2d();
		public Vector2d vel = new Vector2d();
	}
	protected ArrayList<MovingNode> movingNodes = new ArrayList<MovingLoopsRenderer.MovingNode>();

	private int bgColor = Color.BLACK;
	protected Paint paint = new Paint();
	protected Path path = new Path();
	
	// the screen dimension in pixel
	protected int width;
	protected int height;

	// speed of the moving nodes
	protected int speedAverage = 10;
	protected int speedVariance = 5;
	
	// used for the color transformation
	// after how many frames do we change the *target* color
	private static final int RANDOM_COLOR_FRAME_INTERVAL = 100;
	private int randomColorFrameCounter;
	// the "target" color, towards which our currentColor will gradually transform over the RANDOM_COLOR_FRAME_INTERVAL
	private int targetColor;
	private Random random = new Random();
	private int currentColor = Color.WHITE;
	
	public enum RenderMode {SOLID_COLOR, STROKE_ONLY};
	private RenderMode currentRenderMode = RenderMode.STROKE_ONLY;
	
	public enum LoopType {CURVE, POLYGON};
	
	public MovingLoopsRenderer(int numLoops, int numPoints, int width, int height, LoopType loopType){
		this.setWidth(width);
		this.setHeight(height);
		// generate the loops and put points in them
		for (int i=0; i<numLoops; i++){
			if (loopType == LoopType.POLYGON){
				loops.add(new PolygonLoop(numPoints));
			}else{
				loops.add(new CurveLoop(numPoints));
			}
		}

		// generate the nodes
		for (int i=0; i<numPoints; i++){
			MovingNode newNode = new MovingNode();
			// randomize the nodes
			newNode.pos = new Point2d(Math.random()*width, Math.random()*height);
			double speed = getRandomSpeed();
			double angle = Math.random() * 2 * Math.PI;
			newNode.vel = new Vector2d(speed*Math.cos(angle), speed*Math.sin(angle));
			movingNodes.add(newNode);
		}

		path.setFillType(Path.FillType.WINDING);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		targetColor = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
	}


	private double getRandomSpeed() {
		return speedAverage + (Math.random() * 2 * speedVariance) - speedVariance;
	}


	/**
	 * To be called in each frame
	 */
	public synchronized void calc(){
		// move the nodes. handle bounding box
		moveNodes();

		// advance the current Loop 
		advanceCurrentLoop();

		// position the current loop according to the pos of the nodes
		updateCurrentLoopPos();

		// on a certain interval, set the target color to a random one
		randomColorFrameCounter ++;
		if (randomColorFrameCounter > RANDOM_COLOR_FRAME_INTERVAL){
			randomColorFrameCounter = 0;
			updateTargetTransformColor();
		}
		
		transformCurrentColor();
		
		// calculate color for the current loop
		updateCurrentLoopColor();

	}


	/**
	 * transform the current color towards the "target" color
	 */
	private void transformCurrentColor() {
		int r = Color.red(currentColor) + (Color.red(targetColor) - Color.red(currentColor)) / RANDOM_COLOR_FRAME_INTERVAL;
		int g = Color.green(currentColor) + (Color.green(targetColor) - Color.green(currentColor)) / RANDOM_COLOR_FRAME_INTERVAL;
		int b = Color.blue(currentColor) + (Color.blue(targetColor) - Color.blue(currentColor)) / RANDOM_COLOR_FRAME_INTERVAL;
		currentColor = Color.rgb(r, g, b);
	}
	
	private void updateTargetTransformColor() {
		// set the target color of the color transform to a random color
		targetColor = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
	}


	/**
	 * Render everything onto a canvas
	 * @param canvas
	 */
	public synchronized void draw(Canvas canvas){
		canvas.drawColor(getBgColor());
		// draw all loops
		drawAllLoops(canvas);
	}

	protected void moveNodes(){
		for (int i=0; i<movingNodes.size(); i++){
			movingNodes.get(i).pos.add(movingNodes.get(i).vel);
			// if the node is moving out of the valid area
			if (movingNodes.get(i).pos.x > getWidth()){
				movingNodes.get(i).pos.x = getWidth();
				movingNodes.get(i).vel.x *= -1;
				movingNodes.get(i).vel.normalize();
				movingNodes.get(i).vel.scale(getRandomSpeed());
				
			}else if (movingNodes.get(i).pos.x < 0){
				movingNodes.get(i).pos.x = 0;
				movingNodes.get(i).vel.x *= -1;
				movingNodes.get(i).vel.normalize();
				movingNodes.get(i).vel.scale(getRandomSpeed());
			}

			if (movingNodes.get(i).pos.y > getHeight()){
				movingNodes.get(i).pos.y = getHeight();
				movingNodes.get(i).vel.y *= -1;
				movingNodes.get(i).vel.normalize();
				movingNodes.get(i).vel.scale(getRandomSpeed());
			}else if (movingNodes.get(i).pos.y < 0){
				movingNodes.get(i).pos.y = 0;
				movingNodes.get(i).vel.y *= -1;
				movingNodes.get(i).vel.normalize();
				movingNodes.get(i).vel.scale(getRandomSpeed());
			}
		}
	}

	protected void advanceCurrentLoop(){
		currentLoop++;
		if (currentLoop>loops.size()-1){
			currentLoop=0;
		}
	}

	protected void updateCurrentLoopPos() {
		// set the current loop's points to the nodes point positions
		for (int i=0; i<movingNodes.size(); i++){
			loops.get(currentLoop).getPoints().get(i).set(movingNodes.get(i).pos);
		}
	}

	protected void updateCurrentLoopColor() {
		loops.get(currentLoop).setColor(currentColor); 
	}

	/**
	 * Draw all the loops, with the least recent loop at the bottom f the canvas, and the 
	 * currentLoop at the top
	 * @param canvas
	 */
	protected  void drawAllLoops(Canvas canvas) {
		// draw the loops after currentLoop first
		int i = currentLoop + 1;
		while (i<loops.size()){
			loops.get(i).draw(canvas, paint);
			i++;
		}
		// draw the loops before currentLoop
		i=0;
		while (i<=currentLoop){
			loops.get(i).draw(canvas, paint);
			i++;
		}
	}
	
	public int getWidth() {
		return width;
	}


	public void setWidth(int width) {
		this.width = width;
	}


	public int getHeight() {
		return height;
	}


	public void setHeight(int height) {
		this.height = height;
	}


	public RenderMode getCurrentRenderMode() {
		return currentRenderMode;
	}


	public void setCurrentRenderMode(RenderMode currentRenderMode) {
		this.currentRenderMode = currentRenderMode;
		if (currentRenderMode == RenderMode.STROKE_ONLY){
			paint.setStyle(Style.STROKE);
		}else if (currentRenderMode == RenderMode.SOLID_COLOR){
			paint.setStyle(Style.FILL);
		}
	}


	public int getBgColor() {
		return bgColor;
	}


	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

}

