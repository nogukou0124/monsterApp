package com.example.monsterapp.views;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.monsterapp.models.entity.MonsterViewData;

import java.util.ArrayList;
import java.util.List;

/**
 * モンスター描画クラス
 */
public class MonsterView extends View {
    /** 描画エリア全体の幅（px) */
    private static final int DRAWING_WIDTH = 24;

    /** モンスターの描画領域　*/
    private static final int DRAWING_ONESIDE = 16;

    /** FPS */
    private static final int FPS = 2;

    /** paint */
    private Paint paint = null;

    /** モンスター描画情報 */
    private MonsterViewData monsterViewdata = null;;

    /** 1pxあたりの幅 */
    private float strokeWidth = 0;

    /** アニメーション管理 */
    private ValueAnimator animator = null;

    /** 描画位置（X座標） */
    private float px = 0;

    /** 描画位置（Y座標） */
    private float py = 0;

    /** 描画開始位置 */
    private float startPx = 0;

    /** コンストラクタ */
    public MonsterView(@NonNull Context context) { super(context);}

    /**
     * コンストラクタ
     * @param context　コンテキスト
     * @param attrs　状態
     */
    public MonsterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // 画面に合わせて描画領域を決定する
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float width = displayMetrics.widthPixels;
        float height = (float) (displayMetrics.heightPixels * 0.35);

        float sideLength = Math.min(width, height);

        strokeWidth = sideLength / DRAWING_WIDTH;
        startPx = (width - sideLength) / 2;
        float endPX = startPx + strokeWidth * (DRAWING_WIDTH - DRAWING_ONESIDE);
        py = height / 2;

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(strokeWidth);

        // アニメーションの設定
        animator = ValueAnimator.ofFloat(startPx, endPX);
        animator.setDuration(1000 / FPS);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(animation -> {
            px = (float) animation.getAnimatedValue();
            invalidate(); // 再描画をリクエスト
        });
    }

    /**
     * 描画関数
     * @param canvas canvas
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);

        if (monsterViewdata == null) { return; }

        // ２次元配列で設定されているモンスターの描画データを使用して描画する
        @SuppressLint("DrawAllocation") List<Float> psList = new ArrayList<>();

        @Nullable float[][] viewData = monsterViewdata.viewData;
        if (viewData == null) { return; }

        for (int tmpPy = 0; tmpPy < viewData.length; tmpPy++) {
            for (float tmpPx : viewData[tmpPy]) {
                psList.add(px + tmpPx * strokeWidth);
                psList.add(py + tmpPy * strokeWidth);
            }
        }

        @SuppressLint("DrawAllocation") float[] psArray = new float[psList.size()];
        for (int i = 0; i < psList.size(); i++) {
            psArray[i] = psList.get(i);
        }

        // 描画
        canvas.drawPoints(psArray, paint);
    }

    /**
     * 再描画をリクエストする
     */
    public void repaint() {

        if (monsterViewdata.isAnimation) {
            // 通常状態であれば左右に移動する
            animator.start();
        }
        else {
            // 通常状態でなければ移動は停止
            // 画面の中央に描画する
            animator.cancel();
            px = startPx + strokeWidth * ((float) (DRAWING_WIDTH - DRAWING_ONESIDE) / 2);
            postInvalidate();
        }
    }

    /**
     * モンスター情報のsetter
     * @param monsterViewData モンスター情報
     */
    public void setMonsterViewData(MonsterViewData monsterViewData) {
        // モンスター情報が更新されたら再描画
        this.monsterViewdata = monsterViewData;
        repaint();
    }
}
