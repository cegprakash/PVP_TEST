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
import android.view.View;
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

class GoogleSheets {
    public static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    public static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @param resources
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    public static GoogleAuthorizationCodeFlow getFlow(final HttpTransport HTTP_TRANSPORT, Resources resources, String TOKENS_DIRECTORY_PATH) throws IOException {
        // Load client secrets.
        InputStream in = resources.openRawResource(R.raw.credentials);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();


        return flow;
        //return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


}


public class MainActivity extends AppCompatActivity {

    private static String clientId = "624249149118-7374n1noekncojpsssiofmoptuomh3ut.apps.googleusercontent.com";
    private static String clientSecret = "osaVcPyR8w51Uqbsp1lcaIr-";
    private static RadioGroup radioGroup[] = new RadioGroup[180];

    private static int checkedId[] = new int[180];
    TableLayout tableLayout;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    protected void onCreate(Bundle savedInstanceState) {
        reset();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context mContext = getApplicationContext();
        ScrollView scroll = new ScrollView(mContext);
        scroll.setBackgroundColor(Color.TRANSPARENT);
        tableLayout = new TableLayout(mContext);
        scroll.addView(tableLayout);

        addContentView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));


    }

    @Override
    protected void onStart() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        final String TOKENS_DIRECTORY_PATH = getFilesDir().getAbsolutePath();
        ;

        super.onStart();
        final Context mContext = getApplicationContext();

        tableLayout.removeAllViews();

        for (int i = 0; i < 180; i++) {

            TableRow row = new TableRow(mContext);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView tv = new TextView(mContext);
            tv.setText("Question : " + Integer.toString(i + 1));
            tv.setTextColor(Color.BLACK);
            row.addView(tv);

            radioGroup[i] = new RadioGroup(mContext);
            radioGroup[i].setOrientation(LinearLayout.HORIZONTAL);

            for (int j = 0; j < 4; j++) {

                RadioButton rb = new RadioButton(mContext);
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
                for (int i = 0; i < 180; i++) {
                    int selectedId = radioGroup[i].getCheckedRadioButtonId();
                    List<Object> arr = new ArrayList<Object>();
                    arr.add((Object)Integer.toString(selectedId));
                    data.add(arr);
                }

                try{

                    System.out.println("here1");
                    final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
                    final String spreadsheetId = "1Z0ZkvwJtmA4F7vrRBsAULZ1RK5Ib159281aUC58zJ9k";
                    final String range = "Sheet1!A:B";
                    System.out.println("here2");

                    GoogleAuthorizationCodeFlow flow = GoogleSheets.getFlow(HTTP_TRANSPORT, getResources(), TOKENS_DIRECTORY_PATH);

                    AuthorizationCodeInstalledApp ab = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()){
                        public Intent browserIntent;
                        protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                            String url = (authorizationUrl.build());
                            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                            startActivity(browserIntent);
                        }


                    };

                    Credential credential = ab.authorize("user");


                    System.out.println("here21");
                    Sheets.Builder builder = new Sheets.Builder(HTTP_TRANSPORT, GoogleSheets.JSON_FACTORY, credential);
                    System.out.println("here22");
                    builder.setApplicationName(GoogleSheets.APPLICATION_NAME);
                    System.out.println("here3");

                    Sheets service = builder.build();
                    System.out.println("here4");
                    ValueRange content = new ValueRange();
                    content.setValues(data);
                    System.out.println("here5");
                    service.spreadsheets().values().update(spreadsheetId, range, content).setValueInputOption("USER_ENTERED").execute();
                    System.out.println("here6");
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
        for(int i=0;i<180;i++) {
            checkedId[i] = radioGroup[i].getCheckedRadioButtonId();
        }
    }

    private void load(){
        for(int i=0;i<180;i++){
            radioGroup[i].check(checkedId[i]);
        }
    }

    private void reset(){
        for(int i=0;i<180;i++) {
            checkedId[i] = -1;
        }
    }


}
