package com.joshuaavalon.wsdeckeditor.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.WsApplication;

import java.util.Hashtable;

public class QRCode {
    @NonNull
    public static Bitmap encode(@NonNull final String data,
                                @IntRange(from = 1) final int width,
                                @IntRange(from = 1) final int height) {
        final Hashtable<EncodeHintType, ErrorCorrectionLevel> encodeConfig = new Hashtable<>();
        encodeConfig.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        final QRCodeWriter writer = new QRCodeWriter();
        try {
            final BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, encodeConfig);
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int widthIndex = 0; widthIndex < width; widthIndex++) {
                for (int heightIndex = 0; heightIndex < height; heightIndex++) {
                    if (bitMatrix.get(widthIndex, heightIndex))
                        bitmap.setPixel(widthIndex, heightIndex, Color.BLACK);
                    else
                        bitmap.setPixel(widthIndex, heightIndex, Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            throw new RuntimeException("Encode failed.", e);
        }
    }

    @NonNull
    public static Bitmap encodeWithLogo(@NonNull final String data,
                                        @IntRange(from = 1) final int width,
                                        @IntRange(from = 1) final int height) {
        final Bitmap qrBitmap = encode(data, width, height);
        final Bitmap logoBitmap = BitmapFactory.decodeResource(
                WsApplication.getContext().getResources(),
                R.mipmap.ic_launcher);
        final Bitmap resultBitmap = Bitmap.createBitmap(qrBitmap.getWidth(),
                qrBitmap.getHeight(),
                qrBitmap.getConfig());
        final Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(qrBitmap, new Matrix(), null);
        final int deltaHeight = qrBitmap.getHeight() - logoBitmap.getHeight();
        final int deltaWidth = qrBitmap.getWidth() - logoBitmap.getWidth();
        canvas.drawBitmap(logoBitmap, deltaWidth / 2, deltaHeight / 2, null);
        return resultBitmap;
    }
}
