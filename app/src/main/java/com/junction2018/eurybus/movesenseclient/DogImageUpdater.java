package com.junction2018.eurybus.movesenseclient;

import android.content.Context;
import android.media.Image;
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
        switch (state) {
            case 0:
                imageView.setImageResource(R.drawable.movesense);
                break;

            case 1:
                imageView.setImageResource(R.drawable.movesense);
                break;

            case 2:
                imageView.setImageResource(R.drawable.movesense);
                break;

            case 3:
                imageView.setImageResource(R.drawable.movesense);
                break;

            case 4:
                imageView.setImageResource(R.drawable.movesense);
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
