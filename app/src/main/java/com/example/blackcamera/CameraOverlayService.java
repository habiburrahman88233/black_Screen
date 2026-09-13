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
        if (event == null) return;

        CharSequence pkgChar = event.getPackageName();
        CharSequence clsChar = event.getClassName();

        String pkg = (pkgChar != null) ? pkgChar.toString().toLowerCase() : "";
        String cls = (clsChar != null) ? clsChar.toString().toLowerCase() : "";

        // Xiaomi ও অন্যান্য সব ধরনের ক্যামেরা অ্যাপের প্যাকেজ ও ক্লাস নেম ডিটেকশন
        boolean isCamera = pkg.contains("camera") || 
                           pkg.contains("miui.camera") || 
                           pkg.contains("android.camera") || 
                           cls.contains("camera");

        if (isCamera) {
            showBlackScreen();
        } else {
            hideBlackScreen();
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
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
