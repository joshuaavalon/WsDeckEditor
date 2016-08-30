package com.joshuaavalon.wsdeckeditor.model;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.joshuaavalon.wsdeckeditor.Handler;

import java.util.Hashtable;

public class QRCode {
    private QRCode() {
    }

    @NonNull
    public static Bitmap encode(@NonNull final String data,
                                @IntRange(from = 1) final int width,
                                @IntRange(from = 1) final int height) {
        final Hashtable<EncodeHintType, ErrorCorrectionLevel> encodeConfig = new Hashtable<>();
        encodeConfig.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
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

    //@RequiresPermission(Manifest.permission.CAMERA)
    public static void decode(@NonNull final AppCompatActivity activity,
                              @NonNull final Handler<String> callback) {
        final FragmentManager fragmentManager = activity.getSupportFragmentManager();
        final ResultFragment fragment = new ResultFragment();
        fragment.callback = callback;
        fragmentManager.beginTransaction().add(fragment, "QR_SCAN").commit();
        fragmentManager.executePendingTransactions();
        IntentIntegrator.forSupportFragment(fragment).initiateScan();
    }

    public static class ResultFragment extends Fragment {
        @Nullable
        private Handler<String> callback;

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (callback == null) return;
            final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null || result.getContents() == null)
                callback.handle(null);
            else
                callback.handle(result.getContents());
            super.onActivityResult(requestCode, resultCode, data);
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
