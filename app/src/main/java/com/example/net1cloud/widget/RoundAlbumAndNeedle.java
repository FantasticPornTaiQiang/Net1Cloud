package com.example.net1cloud.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.example.net1cloud.R;

public class RoundAlbumAndNeedle extends View {
    private Bitmap play_needle;
    private Bitmap play_disc;
    private Bitmap album_image_now;
    private Bitmap album_image_next;
    private Paint mBitPaint;
    private float needleScale;
    private float disc_x;
    private float disc_y;
    private float needle_x;
    private float needle_y;
    private float bd_x, bd_y;

    private Paint paint = new Paint();
    private Matrix avaterMatrix = new Matrix();
    private Matrix discMatrix = new Matrix();
    private Matrix matrix = new Matrix();
    private Paint linePaint = new Paint();

    private boolean isInited = false;

    public RoundAlbumAndNeedle(Context context) {
        super(context);
    }

    public RoundAlbumAndNeedle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundAlbumAndNeedle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        isInited = true;
        play_needle = ((BitmapDrawable) getResources().getDrawable(R.drawable.play_needle, null)).getBitmap();
        play_disc = ((BitmapDrawable) getResources().getDrawable(R.drawable.play_disc, null)).getBitmap();
        needleScale = play_needle.getWidth() / 276f;
        disc_x = getWidth() / 2f - play_disc.getWidth() / 2f;
        disc_y = (365 - 210) * needleScale;
        needle_x = getWidth() / 2f + 10;
        needle_y = -49 * needleScale;
        bd_x = disc_x + play_disc.getWidth() / 2f;
        bd_y = disc_y + play_disc.getHeight() / 2f;
        album_image_now = centerSquareScaleBitmap(album_image_now, (int) (needleScale * 550f));
        album_image_now = createCircleImage(album_image_now);
        mBitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitPaint.setFilterBitmap(true);
        mBitPaint.setDither(true);
        mBitPaint.setAntiAlias(true);
    }

    private float needleRotareEnd = -30;
    private float needleRotare = needleRotareEnd;
    private float discRotracre = 0;
    private boolean isPlay = false,
            isNext = false, isNextIn = false,
            isPrev = false, isPrevIn = false;

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        if (!isInited) init();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#1AFFFFFF"));
        canvas.drawCircle(bd_x, bd_y, play_disc.getWidth() / 2, paint);
        paint.setColor(Color.parseColor("#33000000"));
        canvas.drawCircle(bd_x, bd_y, play_disc.getWidth() / 2f - (10 * needleScale), paint);

        avaterMatrix.reset();
        avaterMatrix.postTranslate(
                disc_x + play_disc.getWidth() / 2f - album_image_now.getWidth() / 2f,
                disc_y + play_disc.getHeight() / 2f - album_image_now.getWidth() / 2f);
        avaterMatrix.postRotate(discRotracre, disc_x + play_disc.getWidth() / 2f, disc_y + play_disc.getHeight() / 2f);
        canvas.drawBitmap(album_image_now, avaterMatrix, mBitPaint);

        discMatrix.reset();
        discMatrix.postTranslate(disc_x, disc_y);
        discMatrix.postRotate(discRotracre, disc_x + play_disc.getWidth() / 2f, disc_y + play_disc.getHeight() / 2f);
        canvas.drawBitmap(play_disc, discMatrix, mBitPaint);

        if (isPrev || isPrevIn) {
            // prev
            float start = disc_x - getWidth() / 2f - play_disc.getWidth() / 2f;
            canvas.drawBitmap(album_image_next, start + play_disc.getWidth() / 2f - album_image_next.getWidth() / 2f,
                    disc_y + play_disc.getHeight() / 2f - album_image_next.getWidth() / 2f, mBitPaint);
            canvas.drawBitmap(play_disc, start, disc_y, mBitPaint);
        } else if (isNextIn || isNext) {
            // next
            float start1 = getWidth() / 2f + play_disc.getWidth() / 2f + disc_x;
            canvas.drawBitmap(album_image_next, start1 + play_disc.getWidth() / 2f - album_image_next.getWidth() / 2f,
                    disc_y + play_disc.getHeight() / 2f - album_image_next.getWidth() / 2f, mBitPaint);
            canvas.drawBitmap(play_disc, start1, disc_y, mBitPaint);
        }

        matrix.reset();
        matrix.postTranslate(needle_x, needle_y);
        matrix.postRotate(needleRotare, needle_x + 49 * needleScale, needle_y + 49 * needleScale);
        canvas.drawBitmap(play_needle, matrix, mBitPaint);

        linePaint.reset();
        linePaint.setAntiAlias(true);
        @SuppressLint("DrawAllocation") Shader shader = new RadialGradient(getWidth() / 2, 1, getWidth() / 2,
                new int[]{0xFFFFFFFF, 0x19FFFFFF}, null, Shader.TileMode.REPEAT);
        linePaint.setShader(shader);
        linePaint.setStrokeWidth(0.5f);
        canvas.drawLine(0, 0, getWidth(), 0, linePaint);

        if (isPlay) {
            discRotracre += 0.2f;
            if (discRotracre >= 360f) discRotracre = 0;
            float needleRotareStart = 0;
            if (needleRotare < needleRotareStart) needleRotare += 1;
            invalidate();
        } else if (isNextIn || isNext || isPrev || isPrevIn) {
            if (needleRotare > needleRotareEnd) needleRotare -= 1;
            if (isNext) {
                disc_x -= 40;
                if (disc_x <= -(play_disc.getWidth())) {
                    isNext = false;
                    isNextIn = true;
                    discRotracre = 0;
                }
            }
            if (isNextIn) {
                disc_x -= 40;
                if (disc_x <= getWidth() / 2f - play_disc.getWidth() / 2f) {
                    disc_x = getWidth() / 2f - play_disc.getWidth() / 2f;
                    isNextIn = false;
                    isPlay = true;
                    album_image_now = album_image_next;
                    invalidate();
                    return;
                }
            }
            if (isPrev) {
                disc_x += 40;
                if (disc_x >= getWidth()) {
                    isPrev = false;
                    isPrevIn = true;
                    discRotracre = 0;
                }
            }
            if (isPrevIn) {
                disc_x += 40;
                if (disc_x >= getWidth() / 2f - play_disc.getWidth() / 2f) {
                    disc_x = getWidth() / 2f - play_disc.getWidth() / 2f;
                    isPrevIn = false;
                    isPlay = true;
                    album_image_now = album_image_next;
                    invalidate();
                    return;
                }
            }
            invalidate();
        } else if (needleRotare > needleRotareEnd) {
            needleRotare -= 1;
            invalidate();
        }
    }

    public void play() {
        this.isPlay = true;
        invalidate();//请求重新draw()
    }

    public void next(Bitmap albumImage) {
        this.isPlay = false;
        this.isNext = true;
        this.isNextIn = false;
        album_image_next = centerSquareScaleBitmap(albumImage, (int) (needleScale * 550f));
        album_image_next = createCircleImage(album_image_next);
        invalidate();
    }

    public void prev(Bitmap albumImage) {
        this.isPlay = false;
        this.isPrev = true;
        this.isPrevIn = false;
        album_image_next = centerSquareScaleBitmap(albumImage, (int) (needleScale * 550f));
        album_image_next = createCircleImage(album_image_next);
        invalidate();
    }

    public void pause() {
        this.isPlay = false;
    }

    public void setAlbumImage(Bitmap albumImage) {
        album_image_now = albumImage;
    }

    /**
     * @param bitmap     原图
     * @param edgeLength 希望得到的正方形部分的边长
     * @return 缩放截取正中部分后的位图。
     */
    private Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }
        Bitmap result;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

//        if (widthOrg > edgeLength && heightOrg > edgeLength) {
        //压缩到一个最小长度是edgeLength的bitmap
        int longerEdge = edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg);
        int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
        int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
        Bitmap scaledBitmap;
        try {
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
        } catch (Exception e) {
            return null;
        }

        //从图中截取正中间的正方形部分。
        int xTopLeft = (scaledWidth - edgeLength) / 2;
        int yTopLeft = (scaledHeight - edgeLength) / 2;
        try {
            result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
            scaledBitmap.recycle();
        } catch (Exception e) {
            return null;
        }
//        }
        return result;
    }

    /**
     * 根据原图和变长绘制圆形图片
     *
     * @param source 原图
     * @return 要的图
     */
    private Bitmap createCircleImage(Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        int min = Math.min(source.getWidth(), source.getHeight());
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        //产生一个同样大小的画布
        Canvas canvas = new Canvas(target);
        //首先绘制圆形
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        //使用SRC_IN
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }
}