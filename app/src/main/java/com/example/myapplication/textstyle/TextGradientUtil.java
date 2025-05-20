package com.example.myapplication.textstyle;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.widget.TextView;

public class TextGradientUtil {

    public static void applyGradient(TextView textView) {
        textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

        float width = textView.getPaint().measureText(textView.getText().toString());
        if (width == 0) {
            width = textView.getWidth(); // fallback nếu chưa đo được
        }

        Shader textShader = new LinearGradient(
                0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#FA457E"),
                        Color.parseColor("#7B49FF")
                },
                null, Shader.TileMode.CLAMP);

        textView.getPaint().setShader(textShader);
        textView.invalidate();
    }
}
