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

        // কালো পর্দা তৈরি
        blackView = new View(this);
        blackView.setBackgroundColor(Color.BLACK);

        // স্ক্রিনে যেকোনো জায়গায় একবার টাচ করলেই ক্যামেরা নিজে থেকে কেটে হোম/ব্যাকে চলে যাবে
        blackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                hideBlackScreen();
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }

        CharSequence pkgChar = event.getPackageName();
        if (pkgChar == null) return;

        String pkg = pkgChar.toString().toLowerCase();

        // সিস্টেম বা নিজের অ্যাপে ওভারলে আসবে না
        if (pkg.contains("blackcamera") || pkg.contains("systemui") || pkg.contains("launcher")) {
            hideBlackScreen();
            return;
        }

        // ক্যামেরা শনাক্তকরণ
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

            // FLAG_LAYOUT_IN_SCREEN দিয়ে ফুলস্ক্রিন করা এবং ক্লিক সক্রিয় রাখা
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
