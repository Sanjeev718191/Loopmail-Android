package com.example.loopmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.loopmail.model.Task;
import com.example.loopmail.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        VideoView videoView = findViewById(R.id.videoView);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.loopmail_welcome);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> mp.setLooping(false)); // Ensure it plays once

        videoView.start();

        SharedPreferences sharedPreferences = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            String token = sharedPreferences.getString("auth_token", null);
            if(token != null && isInternetAvailable()) {
                getUserData(token);
            } else {
                startActivity(new Intent(WelcomeActivity.this, UserLoginSignupActivity.class));
                finish();
            }
        }, 1500);
    }

    private boolean getUserData(String token) {
        String url = getString(R.string.base_url) + "/api/auth";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject userJson = response.getJSONObject("user");
                            List<Task> taskList = new ArrayList<>();

                            // Save user info
                            User user = new User(
                                    userJson.getString("_id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("avatar"),
                                    taskList
                            );

                            saveUserData(token, user);

                            // Proceed to main activity
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            SharedPreferences sharedPreferences = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("auth_token", null);

                            startActivity(new Intent(WelcomeActivity.this, UserLoginSignupActivity.class));
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(WelcomeActivity.this, UserLoginSignupActivity.class));
                    finish();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        // Add request to Volley queue
        Volley.newRequestQueue(this).add(request);
        return false;
    }

    private void saveUserData(String token, User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("auth_token", token);
        editor.putString("user_id", user.getId());
        editor.putString("username", user.getUsername());
        editor.putString("email", user.getEmail());
        editor.putString("avatar", user.getAvatar());

        editor.apply();
    }
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}