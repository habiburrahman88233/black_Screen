package com.example.blackcamera;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class CameraOverlayService extends AccessibilityService {

    private WindowManager windowManager;
    private View blackView;
    private boolean isShowing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        blackView = new View(this);
        blackView.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence pkgName = event.getPackageName();
            if (pkgName != null) {
                String pkg = pkgName.toString().toLowerCase();

                if (pkg.contains("camera")) {
                    showBlackScreen();
                } else {
                    hideBlackScreen();
                }
            }
        }
    }

    private void showBlackScreen() {
        if (!isShowing && windowManager != null) {
            int layoutType;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutType = WindowManager.LayoutParams.TYPE_PHONE;
            }

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL 
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    PixelFormat.OPAQUE);

            params.gravity = Gravity.CENTER;

            try {
                windowManager.addView(blackView, params);
                isShowing = true;
            } catch (Exception ignored) {}
        }
    }

    private void hideBlackScreen() {
        if (isShowing && windowManager != null) {
            try {
                windowManager.removeView(blackView);
                isShowing = false;
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onInterrupt() {
        hideBlackScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideBlackScreen();
    }
}
