package com.a1education.cegprakash.a1educationalcenter;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class StartMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Button button = findViewById(R.id.take_test_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent switchActivityIntent = new Intent(StartMenuActivity.this, MainActivity.class);
                startActivity(switchActivityIntent);
            }
        });


    }
}
