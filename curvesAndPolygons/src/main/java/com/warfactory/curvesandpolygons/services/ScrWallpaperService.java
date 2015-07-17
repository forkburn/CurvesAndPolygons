package com.warfactory.curvesandpolygons.services;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import com.warfactory.curvesandpolygons.R;
import com.warfactory.curvesandpolygons.model.MovingLoopsRenderer;
import com.warfactory.curvesandpolygons.thread.ScrRunnable;

public class ScrWallpaperService extends WallpaperService {

    public static final String SHARED_PREFS_NAME = "oldTimesScreenSaverSettings";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {

        return new OldTimesScrEngine();
    }

    /**
     * This inner class implements the actual live wall paper
     */
    class OldTimesScrEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final int DEFAULT_NUM_LOOP = 10;
        private static final int DEFAULT_NUM_NODE = 5;

        private SurfaceHolder mSurfaceHolder;

        private MovingLoopsRenderer mScr;

        private ScrRunnable mScrRunnable;
        private Thread thread;

        private boolean mVisible;

        private SharedPreferences mPrefs;

        private int width;
        private int height;

        public OldTimesScrEngine() {
            width = WallpaperManager.getInstance(getApplicationContext()).getDesiredMinimumWidth();
            height = WallpaperManager.getInstance(getApplicationContext()).getDesiredMinimumHeight();

            // get the prefs
            mPrefs = getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);

            // create the model
            mScr = new MovingLoopsRenderer(getNumLoopPref(),
                    getNumNodePref(),
                    width, height,
                    getLoopTypePref()); //FIXME
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            mSurfaceHolder = surfaceHolder;

        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            if (mVisible) {
                startRendererThread();
            } else {
                stopRendererThread();
            }
        }


        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            this.width = width;
            this.height = height;
            mScr.setWidth(width);
            mScr.setHeight(height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            stopRendererThread();
        }

        public void startRendererThread() {
            mScrRunnable = new ScrRunnable(mSurfaceHolder, mScr);
            mScrRunnable.setStopFlag(false);
            thread = new Thread(mScrRunnable);
            thread.start();
        }

        public void stopRendererThread() {
            boolean retry = true;
            mScrRunnable.setStopFlag(true);
            while (retry) {
                try {
                    thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
            // check which pref has changed
            if (key.equals(getString(R.string.pref_loop_type_key)) ||
                    key.equals(getString(R.string.pref_num_loop_key)) ||
                    key.equals(getString(R.string.pref_num_node_key))) {

                // recreate the model after pref change
                mScr = new MovingLoopsRenderer(getNumLoopPref(),
                        getNumNodePref(),
                        width, height,
                        getLoopTypePref());


            } else if (key.equals(getString(R.string.pref_bg_color_key))) {
                mScr.setBgColor(getBgColorPref());
            }
        }


        private MovingLoopsRenderer.LoopType getLoopTypePref() {
            String loopTypeKey = getString(R.string.pref_loop_type_key);
            String loopType = mPrefs.getString(loopTypeKey, "");
            String[] validloopType = getResources().getStringArray(R.array.pref_loop_type_value);

            MovingLoopsRenderer.LoopType result = MovingLoopsRenderer.LoopType.CURVE;
            if (loopType.equals(validloopType[0])) {
                result = MovingLoopsRenderer.LoopType.CURVE;
            } else if (loopType.equals(validloopType[1])) {
                result = MovingLoopsRenderer.LoopType.POLYGON;
            }
            return result;
        }

        private int getNumLoopPref() {
            String prefKey = getString(R.string.pref_num_loop_key);
            return mPrefs.getInt(prefKey, DEFAULT_NUM_LOOP);
        }

        private int getNumNodePref() {
            String prefKey = getString(R.string.pref_num_node_key);
            return mPrefs.getInt(prefKey, DEFAULT_NUM_NODE);
        }

        private int getBgColorPref() {
            String prefKey = getString(R.string.pref_bg_color_key);
            return mPrefs.getInt(prefKey, Color.BLACK);
        }
    }
}
