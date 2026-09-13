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
        if (pkgChar == null) return;

        String pkg = pkgChar.toString().toLowerCase();

        // নিজের অ্যাপকে ফিল্টার থেকে বাদ দেওয়া হলো যাতে নিজের অ্যাপে ব্ল্যাক স্ক্রিন না আসে
        if (pkg.equals(getPackageName().toLowerCase())) {
            hideBlackScreen();
            return;
        }

        // শাওমি HyperOS-এর মূল ক্যামেরা প্যাকেজ শনাক্তকরণ
        boolean isCamera = pkg.equals("com.miui.camera") || 
                           pkg.equals("com.android.camera") || 
                           pkg.contains("googlecamera");

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

            // FLAG_NOT_TOUCHABLE দেওয়া হয়েছে যাতে স্ক্রিন কালো হলেও ব্যাক/হোম বাটন ও সোয়াইপ ঠিকঠাক কাজ করে
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
