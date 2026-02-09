package com.example.phonebook.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Locale;

public class AlphabetIndexView extends View {

    public interface OnLetterSelected {
        void onLetter(String letter);
    }

    private static final String[] LETTERS = {
            "#","A","B","C","D","E","F","G","H","I","J","K","L","M",
            "N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
            "А","Б","В","Г","Д","Е","Ё","Ж","З","И","Й","К","Л","М",
            "Н","О","П","Р","С","Т","У","Ф","Х","Ц","Ч","Ш","Щ","Ъ","Ы","Ь","Э","Ю","Я"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnLetterSelected listener;

    public AlphabetIndexView(Context context) { super(context); init(); }
    public AlphabetIndexView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public AlphabetIndexView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(12));
        paint.setColor(0xFF6B7280); // gray
    }

    public void setOnLetterSelected(OnLetterSelected l) {
        listener = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float h = getHeight();
        float w = getWidth();
        float cell = h / LETTERS.length;

        for (int i = 0; i < LETTERS.length; i++) {
            float x = w / 2f;
            float y = cell * i + cell/2f - (paint.descent() + paint.ascent())/2f;
            canvas.drawText(LETTERS[i], x, y, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            int idx = (int) Math.floor((y / getHeight()) * LETTERS.length);
            if (idx < 0) idx = 0;
            if (idx >= LETTERS.length) idx = LETTERS.length - 1;
            if (listener != null) listener.onLetter(LETTERS[idx]);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
