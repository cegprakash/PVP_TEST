package com.a1education.cegprakash.a1educationalcenter;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

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
import java.util.Collections;
import java.util.List;

public class GoogleSheetsHelper {

    final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    final String spreadsheetId = "1Z0ZkvwJtmA4F7vrRBsAULZ1RK5Ib159281aUC58zJ9k";
    final String range = "Sheet1!A:B";
    static final String APPLICATION_NAME = "A1 Education Center's Application";
    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    String TOKENS_DIRECTORY_PATH ;


    static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    AppCompatActivity activity;
    Resources resources;

    GoogleSheetsHelper(AppCompatActivity activity){
        this.activity = activity;
        resources = this.activity.getResources();
        TOKENS_DIRECTORY_PATH = this.activity.getFilesDir().getAbsolutePath();
    }


    private GoogleAuthorizationCodeFlow getFlow(final HttpTransport HTTP_TRANSPORT, Resources resources, String TOKENS_DIRECTORY_PATH) throws IOException {
        // Load client secrets.
        InputStream in = resources.openRawResource(R.raw.credentials);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("online")
                .build();

        return flow;
    }

    void submitSolution(List<List<Object>> data) throws IOException {
        GoogleAuthorizationCodeFlow flow = getFlow(HTTP_TRANSPORT, resources, TOKENS_DIRECTORY_PATH);

        AuthorizationCodeInstalledApp ab = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()) {

            protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) {
                String url = (authorizationUrl.build());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                activity.startActivity(browserIntent);
            }
        };

        Credential credential = ab.authorize("user");

        Sheets.Builder builder = new Sheets.Builder(HTTP_TRANSPORT, GoogleSheetsHelper.JSON_FACTORY, credential);
        builder.setApplicationName(GoogleSheetsHelper.APPLICATION_NAME);

        Sheets service = builder.build();
        ValueRange content = new ValueRange();
        content.setValues(data);
        service.spreadsheets().values().update(spreadsheetId, range, content).setValueInputOption("USER_ENTERED").execute();
    }
}