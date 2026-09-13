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

        // নিখুঁত পিচ ব্ল্যাক ভিউ তৈরি
        blackView = new View(this);
        blackView.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence pkgChar = event.getPackageName();
        if (pkgChar == null) return;

        String pkg = pkgChar.toString().toLowerCase();

        // নিজের অ্যাপের মধ্যে ব্ল্যাক স্ক্রিন আসবে না
        if (pkg.contains("blackcamera")) {
            hideBlackScreen();
            return;
        }

        // শাওমির সিস্টেম ক্যামেরা এবং সাধারণ ক্যামেরা প্যাকেজ ডিটেকশন
        boolean isCamera = pkg.contains("camera") || 
                           pkg.equals("com.miui.camera") || 
                           pkg.equals("com.android.camera");

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

            // FLAG_NOT_TOUCHABLE দেওয়া হয়েছে যাতে সোয়াইপ বা হোম জেসচার সরাসরি কাজ করে
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
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
