package com.ip.fastgate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainPageActivity extends AppCompatActivity {

    private static final String TAG = "MainPageActivityTag";
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);
        context = this;
        Button btnAccesPoarta = (Button) findViewById(R.id.btnAcces);
        Button btnUserData = (Button) findViewById(R.id.btnUserData);
        Button btnRaport = (Button) findViewById(R.id.btnRaport);

        MyOnClickListener onClickListener = new MyOnClickListener();

        btnAccesPoarta.setOnClickListener(onClickListener);
        btnUserData.setOnClickListener(onClickListener);
        btnRaport.setOnClickListener(onClickListener);
    }

    private static void doOnClickAction(int id) {
        Intent intent = null;
        if (id == R.id.btnRaport) {
            intent = new Intent(context, UserReportActivity.class);
        } else if (id == R.id.btnUserData) {
            intent = new Intent(context, UserProfileActivity.class);
        } else if (id == R.id.btnAcces) {
            intent = new Intent(context, com.ip.fastgate.AccessActivity.class);
        }
        try {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            MainPageActivity.doOnClickAction(v.getId());
        }
    }
}
