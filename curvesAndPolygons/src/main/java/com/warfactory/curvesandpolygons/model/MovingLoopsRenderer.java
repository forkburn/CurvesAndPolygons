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
import android.util.Log;

public class MovingLoopsRenderer {

    public static final int DEFAULT_FRAME_INTERVAL = 20;
    // series of loops moving on the screen
    protected ArrayList<Loop> loops = new ArrayList<Loop>();
    // a pointer to the current loop which we'll process in next frame
    protected int currentLoopIdx;

    // milliseconds between frames
    private long frameUpdateInterval = DEFAULT_FRAME_INTERVAL;

    public void updateFrame(Canvas canvas) {
        long frameStart = System.currentTimeMillis();
        updateFramePhysics();
        drawFrame(canvas);
        long frameEnd = System.currentTimeMillis();
        long timeTillNextFrame = frameStart + frameUpdateInterval - frameEnd;
        if (timeTillNextFrame > 0 ) {
            try {
                Thread.sleep(timeTillNextFrame);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    // the invisible moving nodes on the screen, which decides where the loop go
    protected class MovingNode {
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
    private static final int COLOR_CHANGE_INTERVAL = 100;
    private int colorChangeTimer;
    // the "target" color, towards which our currentColor will gradually transform over the COLOR_CHANGE_INTERVAL
    private int targetColor;
    private Random random = new Random();
    private int currentColor = Color.WHITE;

    public enum RenderMode {SOLID_COLOR, STROKE_ONLY}

    ;
    private RenderMode currentRenderMode = RenderMode.STROKE_ONLY;

    public enum LoopType {CURVE, POLYGON}

    ;

    public MovingLoopsRenderer(int numLoops, int numPoints, int width, int height, LoopType loopType) {
        this.setWidth(width);
        this.setHeight(height);
        // generate the loops and put points in them
        for (int i = 0; i < numLoops; i++) {
            if (loopType == LoopType.POLYGON) {
                loops.add(new PolygonLoop(numPoints));
            } else {
                loops.add(new CurveLoop(numPoints));
            }
        }

        // generate the nodes
        for (int i = 0; i < numPoints; i++) {
            MovingNode newNode = new MovingNode();
            // randomize the nodes
            newNode.pos = new Point2d(Math.random() * width, Math.random() * height);
            double speed = getRandomSpeed();
            double angle = Math.random() * 2 * Math.PI;
            newNode.vel = new Vector2d(speed * Math.cos(angle), speed * Math.sin(angle));
            movingNodes.add(newNode);
        }

        path.setFillType(Path.FillType.WINDING);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(false);
        targetColor = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }


    private double getRandomSpeed() {
        return speedAverage + (Math.random() * 2 * speedVariance) - speedVariance;
    }


    /**
     * To be called in each frame
     */
    private synchronized void updateFramePhysics() {
        // move the nodes. handle bouncing on bounding box
        moveNodes();

        // update the pointer to the current Loop
        updateCurrentLoop();

        // position the current loop according to the pos of the nodes
        updateCurrentLoopPos();

        // on a certain interval, set the target color to a random one
        updateTargetColor();

        // base on the target color, set color for this frame
        transformCurrentColor();

        // paint the current loop
        updateCurrentLoopColor();

    }

    private void updateTargetColor() {
        colorChangeTimer++;
        if (colorChangeTimer > COLOR_CHANGE_INTERVAL) {
            colorChangeTimer = 0;
            updateTargetTransformColor();
        }
    }


    /**
     * transform the current color towards the "target" color
     */
    private void transformCurrentColor() {
        int r = Color.red(currentColor) + (Color.red(targetColor) - Color.red(currentColor)) / COLOR_CHANGE_INTERVAL;
        int g = Color.green(currentColor) + (Color.green(targetColor) - Color.green(currentColor)) / COLOR_CHANGE_INTERVAL;
        int b = Color.blue(currentColor) + (Color.blue(targetColor) - Color.blue(currentColor)) / COLOR_CHANGE_INTERVAL;
        currentColor = Color.rgb(r, g, b);
    }

    private void updateTargetTransformColor() {
        // set the target color of the color transform to a random color
        targetColor = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }


    /**
     * Render everything onto a canvas
     *
     * @param canvas
     */
    private synchronized void drawFrame(Canvas canvas) {
        canvas.drawColor(getBgColor());
        // drawFrame all loops
        drawAllLoops(canvas);
    }

    protected void moveNodes() {
        for (int i = 0; i < movingNodes.size(); i++) {
            movingNodes.get(i).pos.add(movingNodes.get(i).vel);
            // if the node is moving out of the valid area
            if (movingNodes.get(i).pos.x > getWidth()) {
                movingNodes.get(i).pos.x = getWidth();
                movingNodes.get(i).vel.x *= -1;
                movingNodes.get(i).vel.normalize();
                movingNodes.get(i).vel.scale(getRandomSpeed());

            } else if (movingNodes.get(i).pos.x < 0) {
                movingNodes.get(i).pos.x = 0;
                movingNodes.get(i).vel.x *= -1;
                movingNodes.get(i).vel.normalize();
                movingNodes.get(i).vel.scale(getRandomSpeed());
            }

            if (movingNodes.get(i).pos.y > getHeight()) {
                movingNodes.get(i).pos.y = getHeight();
                movingNodes.get(i).vel.y *= -1;
                movingNodes.get(i).vel.normalize();
                movingNodes.get(i).vel.scale(getRandomSpeed());
            } else if (movingNodes.get(i).pos.y < 0) {
                movingNodes.get(i).pos.y = 0;
                movingNodes.get(i).vel.y *= -1;
                movingNodes.get(i).vel.normalize();
                movingNodes.get(i).vel.scale(getRandomSpeed());
            }
        }
    }

    protected void updateCurrentLoop() {
        currentLoopIdx++;
        if (currentLoopIdx > loops.size() - 1) {
            currentLoopIdx = 0;
        }
    }

    protected void updateCurrentLoopPos() {
        // set the current loop's points to the nodes point positions
        for (int i = 0; i < movingNodes.size(); i++) {
            loops.get(currentLoopIdx).getPoints().get(i).set(movingNodes.get(i).pos);
        }
    }

    protected void updateCurrentLoopColor() {
        loops.get(currentLoopIdx).setColor(currentColor);
    }

    /**
     * Draw all the loops, with the least recent loop at the bottom f the canvas, and the
     * currentLoopIdx at the top
     *
     * @param canvas
     */
    protected void drawAllLoops(Canvas canvas) {
        // drawFrame the loops after currentLoopIdx first
        int i = currentLoopIdx + 1;
        while (i < loops.size()) {
            loops.get(i).draw(canvas, paint);
            i++;
        }
        // drawFrame the loops before currentLoopIdx
        i = 0;
        while (i <= currentLoopIdx) {
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
        if (currentRenderMode == RenderMode.STROKE_ONLY) {
            paint.setStyle(Style.STROKE);
        } else if (currentRenderMode == RenderMode.SOLID_COLOR) {
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

