package com.warfactory.curvesandpolygons.thread;


import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.warfactory.curvesandpolygons.model.MovingLoopsRenderer;


public class ScrRunnable implements Runnable {


    private SurfaceHolder mHolder;
    private MovingLoopsRenderer mScr;

    // flag indicating whether thread should continue to run
    private boolean stopFlag = false;

    public ScrRunnable(SurfaceHolder holder, MovingLoopsRenderer mScr) {
        mHolder = holder;
        this.mScr = mScr;
    }

    @Override
    public void run() {
        while (!stopFlag) {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (mHolder) {
                        mScr.updatePhysics();
                        mScr.draw(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public boolean getStopFlag() {
        return stopFlag;
    }

}  