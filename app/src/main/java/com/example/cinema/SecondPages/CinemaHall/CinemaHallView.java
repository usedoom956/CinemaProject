package com.example.cinema.SecondPages.CinemaHall;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.cinema.SecondPages.CinemaHelpClasses.CinemaDataClass;

public class CinemaHallView extends View {

    private Paint seatPaint;
    private Paint screenPaint;
    private int numRows = 4;  // Количество рядов
    private int numSeatsPerRow = 5;  // Количество мест в ряде
    private int seatGap = 50;  // Промежуток между местами


    private int selectedRow = -1;
    private int selectedSeat = -1;

    private int screenHeight;

    private CinemaDataClass cinemaData;

    public void setSelectedSeat(int row, int seat) {
        selectedRow = row;
        selectedSeat = seat;
        invalidate(); // Перерисовываем представление
    }

    public void setCinemaData(CinemaDataClass cinemaData) {
        this.cinemaData = cinemaData;
        // Представление, чтобы отобразить бронированные места
        invalidate();
    }

    private OnSeatClickListener onSeatClickListener;

    public void setOnSeatClickListener(OnSeatClickListener listener) {
        this.onSeatClickListener = listener;
    }

    public CinemaHallView(Context context) {
        super(context);
        init();
    }

    public CinemaHallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CinemaHallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        seatPaint = new Paint();
        seatPaint.setColor(Color.WHITE);  // Цвет для рисования мест
        seatPaint.setStyle(Paint.Style.FILL);

        screenPaint = new Paint();
        screenPaint.setColor(Color.BLACK);  // Цвет для рисования экрана
        screenPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 1000;  // Например, 500 пикселей
        int desiredHeight = 1000;  // Например, 500 пикселей

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // Рассмотрим требования к ширине
        if (widthMode == MeasureSpec.EXACTLY) {
            // Ширина задана точно, используем её
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Ширина может быть не больше определенного размера
            width = Math.min(desiredWidth, widthSize);
        } else {
            // Ширина может быть любой
            width = desiredWidth;
        }

        // Рассмотрим требования к высоте
        if (heightMode == MeasureSpec.EXACTLY) {
            // Высота задана точно, используем её
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Высота может быть не больше определенного размера
            height = Math.min(desiredHeight, heightSize);
        } else {
            // Высота может быть любой
            height = desiredHeight;
        }

        // Установим финальные размеры представления
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Получим высоту экрана при изменении размеров
        screenHeight = h / 10;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cinemaData == null) {
            // Если данные о кинозале не загружены, просто нарисуйте пустой вид
            super.onDraw(canvas);
            return;
        }
        // Рисуем экран
        canvas.drawRect(0, 0, getWidth(), getHeight() / 10, screenPaint);

        // Рисуем места в зале
        int totalGapWidth = seatGap * (numSeatsPerRow - 1);
        int totalSeatWidth = getWidth() - totalGapWidth;
        int seatWidth = totalSeatWidth / numSeatsPerRow - 50;

        int totalGapHeight = seatGap * (numRows - 1);
        int totalSeatHeight = getHeight() - totalGapHeight - screenHeight;
        int seatHeight = totalSeatHeight / numRows - 50;

        for (int row = 0; row < numRows; row++) {
            for (int seat = 0; seat < numSeatsPerRow; seat++) {
                int left = seat * (seatWidth + seatGap) + seatGap / 2 + 125;  // Смещение для центрирования
                int top = row * (seatHeight + seatGap) + screenHeight + 100;  // Учтем высоту экрана
                int right = left + seatWidth;
                int bottom = top + seatHeight;

                int seatNumber = getSeatNumber(row, seat);

                if (cinemaData != null && cinemaData.getIsReserved() != null && seatNumber >= 0 && seatNumber < cinemaData.getIsReserved().size()) {
                    boolean isSeatReserved = cinemaData.getIsReserved().get(seatNumber);

                    if (isSeatReserved) {
                        // Место забронировано, рисуем его в черный цвет
                        seatPaint.setColor(Color.BLACK);
                    } else if (row == selectedRow && seat == selectedSeat) {
                        // Рисуем выбранное место другим цветом
                        seatPaint.setColor(Color.DKGRAY);
                    } else {
                        // Рисуем обычное место
                        seatPaint.setColor(Color.WHITE);
                    }
                }

                // Рисуем место
                canvas.drawRect(left, top, right, bottom, seatPaint);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Найдите выбранное место по координатам касания
                int row = (int) (y / ((float) getHeight() / numRows));
                int seat = (int) (x / ((float) getWidth() / numSeatsPerRow));

                // Установите выбранные значения
                selectedRow = row;
                selectedSeat = seat;

                // Вызовите слушателя, если установлен
                if (onSeatClickListener != null) {
                    onSeatClickListener.onSeatClick(row, seat);
                }

                invalidate(); // Перерисовываем представление
                return true;
        }

        return super.onTouchEvent(event);
    }


    private int getSeatNumber(int row, int seat) {
        // Возвращает уникальный номер места на основе его расположения
        return row * numSeatsPerRow + seat;
    }

}



