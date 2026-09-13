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
    private String currentPackage = "";

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

        // শুধু উইন্ডো পরিবর্তন (নতুন অ্যাপ ওপেন বা ক্লোজ) ইভেন্ট ধরবে, ভেতরের কন্টেন্ট নয়
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence pkgChar = event.getPackageName();
        if (pkgChar == null) return;

        String pkg = pkgChar.toString().toLowerCase();

        // ওভারলে ভিউ নিজে বা কোনো সিস্টেম ডায়ালগ যেন স্টেট নষ্ট না করে
        if (pkg.contains("blackcamera") || pkg.contains("systemui")) {
            return;
        }

        currentPackage = pkg;

        // Xiaomi ও Android ক্যামেরা সনাক্তকরণ
        boolean isCamera = pkg.equals("com.miui.camera") || 
                           pkg.equals("com.android.camera") || 
                           pkg.contains("camera");

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
                    // FLAG_NOT_FOCUSABLE নিশ্চিত করবে ব্যাক/হোম জেসচার স্বাভাবিক থাকবে
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
