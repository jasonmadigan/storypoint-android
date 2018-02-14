package me.dmkube.storypointme;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class PointingActivity extends AppCompatActivity {

    private PointingApplication pointingApplication;


    // TODO: onDestroy/onBack impl that cleans up the pointing application

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointing);

        SharedPreferences settings = getSharedPreferences("appPrefs", 0);
        String sessionId = settings.getString("sessionID", null);
        String name = settings.getString("name", null);
        if (sessionId == null || sessionId.isEmpty()) {
            Log.d("app", String.format("finishing activity sessionId=%s", sessionId));
            finish();
        }
        if (name == null || name.isEmpty()) {
            Log.d("app", String.format("finishing activity name=%s", name));
            finish();
        }

        Pointer me = new Pointer(name);
        pointingApplication = new PointingApplication(sessionId, me);

        // TODO: read story description from server and set it here
        setStoryDescription(me.getName(), pointingApplication.getSessionId(), pointingApplication.calculateAverageScores());

        final ListView pointersListView = findViewById(R.id.pointersListView);
        ArrayAdapter<Pointer> pointersListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pointingApplication.getPointers());

        pointingApplication.setEventListener(new PointingEventListener(){
            @Override
            public void onPointers(JSONObject event) {
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onScore(JSONObject event) {
                runOnUiThread(() -> {
                    pointersListAdapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onShow(JSONObject event) {
                // TODO: make scores visible & show average

            }
            @Override
            public void onClear(JSONObject event) {
                // TODO: clear all user scores

            }

            @Override
            public void onExit(String reason) {
                Log.d("app", String.format("PointingApplication exited with reason (%s)", reason));
                // TODO: any cleanup needed (e.g. close websocket if its still open)?
                finish();
            }
        });

        pointersListView.setAdapter(pointersListAdapter);
        pointingApplication.start();
    }

    private void setStoryDescription(String name, String sessionId, String average) {
        TextView storyDescriptionView = findViewById(R.id.storyDescriptionView);
        storyDescriptionView.setText(String.format("Name: %s\nSession ID: %s\nAverage : %s", name, sessionId, average));

    }
}
