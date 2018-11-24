package com.junction2018.eurybus.movesenseclient;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onToDeviceScreenClicked(findViewById(R.id.btnSendMessage));
    }


    public void onSendMessageClicked(View view) {

    }

    public void onToDeviceScreenClicked(View view) {
        Intent intent = new Intent(this, DeviceConnect.class);
        this.startActivity(intent);
    }
}
