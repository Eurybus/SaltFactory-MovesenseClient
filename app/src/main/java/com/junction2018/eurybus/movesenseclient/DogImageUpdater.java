package com.junction2018.eurybus.movesenseclient;

import android.content.Context;
import android.widget.ImageView;

public class DogImageUpdater {
    private ImageView imageView;
    private Context context;
    private int currentState;

    public DogImageUpdater(ImageView imageView, Context context) {
        this.imageView = imageView;
        this.context = context;
    }

    public void ChangeState(int state) {
        if (state == currentState)
            return;
        else
            currentState = state;
        switch (state) {
            case 0:
                imageView.setImageResource(R.drawable.dog0);
                break;

            case 1:
                imageView.setImageResource(R.drawable.dog2);
                break;

            case 2:
                imageView.setImageResource(R.drawable.dog3);
                break;

            case 3:
                imageView.setImageResource(R.drawable.dog4);
                break;

            case 4:
                imageView.setImageResource(R.drawable.dog5);
                break;
            case 5:
                imageView.setImageResource(R.drawable.dog5);
                break;
        }
    }

    public void IncrementState() {
        currentState += 1;
        ChangeState(currentState);
    }
    public void DecrementState() {
        currentState -= 1;
        ChangeState(currentState);
    }


}
