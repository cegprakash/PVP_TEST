package com.a1education.cegprakash.a1educationalcenter;

import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private static final int questionsCnt = 10;
    private static RadioGroup radioGroup[] = new RadioGroup[questionsCnt];
    private static int checkedId[] = new int[questionsCnt];
    GoogleSheetsHelper googleSheetsHelper;

    TableLayout tableLayout;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleSheetsHelper = new GoogleSheetsHelper(this);
        reset();
        setContentView(R.layout.activity_main);
        Context mContext = getApplicationContext();
        ScrollView scroll = new ScrollView(mContext);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        tableLayout = new TableLayout(mContext);
        tableLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        scroll.addView(tableLayout);

        addContentView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

    }

    protected int getPixels(float dps) {
        return (int) (dps * getResources().getDisplayMetrics().density);
    }

    private void allowNetworkOnMainThread() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    @Override
    protected void onStart() {
        super.onStart();
        allowNetworkOnMainThread();

        final Context mContext = getApplicationContext();

        tableLayout.removeAllViews();

        for (int i = 0; i < questionsCnt; i++) {

            TableRow row = new TableRow(mContext);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            row.setMinimumHeight(getPixels(20));
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            if(i%2==0){
                row.setBackgroundColor(Color.parseColor("#a0a0a0"));
            }

            TextView tv = new TextView(mContext);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setPadding(getPixels(30),0, 0,0);

            tv.setText("#" + Integer.toString(i + 1));
            tv.setTextColor(Color.BLACK);
            row.addView(tv);

            radioGroup[i] = new RadioGroup(mContext);
            radioGroup[i].setOrientation(LinearLayout.HORIZONTAL);
            radioGroup[i].setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            radioGroup[i].setMinimumWidth(row.getMeasuredWidth()-tv.getMeasuredWidth());
            radioGroup[i].setPadding(getPixels(50),0, 0,0);

            for (int j = 0; j < 4; j++) {

                RadioButton rb = new RadioButton(mContext);
                rb.setTextSize(getPixels(30));
                rb.setScaleX(1.2f);
                rb.setScaleY(1.2f);
                rb.setPadding(0,0,getPixels(30),0);
                rb.setId(j);
                radioGroup[i].addView(rb);
            }

            row.addView(radioGroup[i]);


            tableLayout.addView(row);

        }

        Button submitButton = new Button(mContext);
        submitButton.setText("Submit");
        tableLayout.addView(submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!isNetworkConnected()){
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("No connectivity");
                alert.setMessage("Check your internet connectivity and try again later.");
                alert.show();
                alert.setPositiveButton("Ok", null);
                return;
            }
            List<List<Object>> data = new ArrayList<>();
            for (int i = 0; i < questionsCnt; i++) {
                int selectedId = radioGroup[i].getCheckedRadioButtonId();
                List<Object> arr = new ArrayList<Object>();
                arr.add((Object)Integer.toString(selectedId));
                data.add(arr);
            }

            try{
                googleSheetsHelper.submitSolution(data);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Submitted");
                alert.setMessage("Your response has been submitted successfully");
                alert.setPositiveButton("Ok", null);
                alert.show();

            } catch(Exception e){
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Something went wrong. Check your internet connectivity and try again later.");
                alert.setMessage(e.toString());
                alert.show();
                alert.setPositiveButton("Ok", null);
                System.out.println(e);
            }
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        save();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void save(){
        for(int i=0;i<questionsCnt;i++) {
            checkedId[i] = radioGroup[i].getCheckedRadioButtonId();
        }
    }

    private void load(){
        for(int i=0;i<questionsCnt;i++){
            radioGroup[i].check(checkedId[i]);
        }
    }

    private void reset(){
        for(int i=0;i<questionsCnt;i++) {
            checkedId[i] = -1;
        }
    }


}
