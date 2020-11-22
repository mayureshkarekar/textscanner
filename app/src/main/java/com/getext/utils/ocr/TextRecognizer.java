package com.getext.utils.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;

public class TextRecognizer {
    private static volatile com.google.android.gms.vision.text.TextRecognizer mTextRecognizer;

    private static com.google.android.gms.vision.text.TextRecognizer getTextRecognizer(@NonNull final Context context) {
        synchronized (TextRecognizer.class) {
            if (mTextRecognizer == null) {
                mTextRecognizer = new com.google.android.gms.vision.text.TextRecognizer.Builder(context.getApplicationContext()).build();
            }
        }

        return mTextRecognizer;
    }

    public static String recognizeText(@NonNull final Context context, @NonNull Bitmap imageBitmap) throws TextRecognitionException {
        com.google.android.gms.vision.text.TextRecognizer textRecognizer = getTextRecognizer(context);

        if (textRecognizer.isOperational()) {
            Frame frame = new Frame.Builder().setBitmap(imageBitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder recognizedText = new StringBuilder();

            for (int i = 0; i < items.size(); i++) {
                TextBlock item = items.valueAt(i);
                recognizedText.append(item.getValue()).append("\n");
            }

            return recognizedText.toString().trim();
        }

        throw new TextRecognitionException("TextRecognizer not operational.");
    }
}