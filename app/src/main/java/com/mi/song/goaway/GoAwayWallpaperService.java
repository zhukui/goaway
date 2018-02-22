package com.mi.song.goaway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.Locale;

/**
 * @author by songhang on 2018/2/21 Email: songhang@xiaomi.com
 */

public class GoAwayWallpaperService extends WallpaperService {
    private static final String TAG = "goaway";
    private long mUsedTime;
    private long mStartTime;
    private AwayEngine mAwayEngine;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mUsedTime = TimeUtil.getTodayUsedTime(context);
                mStartTime = System.currentTimeMillis();
                mAwayEngine.doDraw();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                TimeUtil.setTodayUsedTime(context, calcNewUsedTime());
            }
        }
    };

    private long calcNewUsedTime() {
        long endTime = System.currentTimeMillis();
        // 新的一天开始了
        if (!TimeUtil.isSameDay(mStartTime, endTime)) {
            mUsedTime = 0;
            mStartTime = TimeUtil.getZeroTime(endTime);
        }
        return mUsedTime + (endTime - mStartTime);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUsedTime = TimeUtil.getTodayUsedTime(GoAwayWallpaperService.this);
        mStartTime = System.currentTimeMillis();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public Engine onCreateEngine() {
        return mAwayEngine = new AwayEngine();
    }

    private class AwayEngine extends Engine {
        private Paint textPaint;
        private float height, width;
        private float bottom = 24; // 距离底部
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            WindowManager wm = (WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            height = wm.getDefaultDisplay().getHeight();
            width = wm.getDefaultDisplay().getWidth();

            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(24);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);
            textPaint.setDither(true);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                doDraw();
            } else {

            }
        }

        private void doDraw() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            if (surfaceHolder == null) {
                return;
            }

            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }

            canvas.drawColor(Color.BLUE);
            String useStr = isChineseLanguage() ? "已使用: " : "used: ";
            String content = useStr + TimeUtil.timeToString(calcNewUsedTime());
            canvas.drawText(content, width / 2, height - bottom, textPaint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
        }

    }

    public static boolean isChineseLanguage(){
        return TextUtils.equals(Locale.getDefault().getLanguage(),Locale.CHINA.getLanguage());
    }
}