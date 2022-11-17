/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * 底部弹出对话框基类
 *
 * @author liyalong
 * @version BaseBottomSheetDialog.java, v 0.1 2022年09月27日 16:07 liyalong
 */
public abstract class BaseBottomSheetDialog extends BottomSheetDialogFragment {

    private KeyboardUtil keyboardUtil;

    // 是否使用对话框内容的原始高度
    private boolean useOriginalHeight;

    public BaseBottomSheetDialog(boolean useOriginalHeight) {
        this.useOriginalHeight = useOriginalHeight;
    }

    @Override
    public void onStart() {
        super.onStart();
        setDialogStyle();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeDialogStyle();
    }

    private void setDialogStyle() {
        View view = getView();

        // 设置对话框高度
        if (!useOriginalHeight) {
            int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.9);
            view.getLayoutParams().height = height;

            // 设置对话框直接弹出到指定高度
            BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
            behavior.setPeekHeight(height);
            //behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // 设置软键盘弹出时不顶起对话框 (这是BottomSheetDialogFragment的通病)
        // 方案1：软键盘会把输入框遮住
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        // 方案2：顶起后重新拉回来，最终效果没问题，缺点是对话框会闪一下
        keyboardUtil = new KeyboardUtil(getActivity(), view);
    }

    private void removeDialogStyle() {
        // 关闭对话框前注销该监听器，避免回到activity页面后继续触发
        if (keyboardUtil != null) {
            keyboardUtil.removeListener();
            keyboardUtil = null;
        }
    }

    /**
     * 参考 https://stackoverflow.com/questions/48302171
     * 作了微调
     */
    public class KeyboardUtil {

        private View decorView;

        private View contentView;

        //a small helper to allow showing the editText focus
        ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                //r will be populated with the coordinates of your view that area still visible.
                decorView.getWindowVisibleDisplayFrame(r);

                //get screen height and calculate the difference with the useable area from the r
                int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
                int diff = height - r.bottom;

                // 加一点偏移量，留出底部空间  modified by leshu 2022.09.27
                int offset = dpToPx(30);

                //if it could be a keyboard add the padding to the view
                if (diff != 0) {
                    // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
                    //check if the padding is 0 (if yes set the padding for the keyboard)
                    if (contentView.getPaddingBottom() != diff) {
                        //set the padding of the contentView for the keyboard
                        contentView.setPadding(0, 0, 0, diff + offset);
                    }
                } else {
                    //check if the padding is != 0 (if yes reset the padding)
                    if (contentView.getPaddingBottom() != 0) {
                        //reset the padding of the contentView
                        contentView.setPadding(0, 0, 0, 0);
                    }
                }
            }
        };

        private int dpToPx(float valueInDp) {
            DisplayMetrics metrics = decorView.getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
        }

        public KeyboardUtil(Activity act, View contentView) {
            this.decorView = act.getWindow().getDecorView();
            this.contentView = contentView;

            //only required on newer android versions. it was working on API level 19
            if (Build.VERSION.SDK_INT >= 19) {
                decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        }

        public void removeListener() {
            if (Build.VERSION.SDK_INT >= 19) {
                decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        }
    }

}