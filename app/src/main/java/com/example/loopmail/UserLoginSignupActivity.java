package com.example.loopmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.loopmail.databinding.ActivityUserLoginSignupBinding;
import com.example.loopmail.model.Task;
import com.example.loopmail.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserLoginSignupActivity extends AppCompatActivity {


    private ActivityUserLoginSignupBinding binding;
    private String loginType;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserLoginSignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSignInLayout();

        // Set click listeners
        binding.signInMailBtn.setOnClickListener(v -> setSignInLayout());
        binding.signupMailBtn.setOnClickListener(v -> setSignUpLayout());
        binding.forgetPassButton.setOnClickListener(v -> setForgetPassLayout());
        binding.loginSignupForgetPassButton.setOnClickListener(v -> handleLoginSignupAction());
    }

    private void setSignInLayout() {
        loginType = "in";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputUserName.setVisibility(View.GONE);
        binding.inputPassword.setVisibility(View.VISIBLE);
        binding.inputPassword.setHint("Password");
        binding.inputPasswordConfirm.setVisibility(View.GONE);
        binding.loginSignupForgetPassButton.setText("Sign In");

        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.GONE);
        binding.signupMailBtn.setVisibility(View.VISIBLE);
        binding.forgetPassButton.setVisibility(View.VISIBLE);

        binding.inputOTP.setVisibility(View.GONE);
        binding.verifyOTPText.setVisibility(View.GONE);
    }

    private void setSignUpLayout() {
        loginType = "up";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputUserName.setVisibility(View.VISIBLE);
        binding.inputPassword.setVisibility(View.VISIBLE);
        binding.inputPassword.setHint("Password");
        binding.inputPasswordConfirm.setVisibility(View.VISIBLE);
        binding.loginSignupForgetPassButton.setText("Sign Up");

        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.VISIBLE);
        binding.signupMailBtn.setVisibility(View.GONE);
        binding.forgetPassButton.setVisibility(View.VISIBLE);

        binding.inputOTP.setVisibility(View.GONE);
        binding.verifyOTPText.setVisibility(View.GONE);
    }

    private void setForgetPassLayout() {
        loginType = "forget";
        binding.inputEmail.setVisibility(View.VISIBLE);
        binding.inputPassword.setVisibility(View.GONE);
        binding.inputPasswordConfirm.setVisibility(View.GONE);
        binding.loginSignupForgetPassButton.setText("Reset Password");

        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.VISIBLE);
        binding.signupMailBtn.setVisibility(View.VISIBLE);
        binding.forgetPassButton.setVisibility(View.GONE);

        binding.inputOTP.setVisibility(View.GONE);
        binding.verifyOTPText.setVisibility(View.GONE);
    }

    private void setOTPLayout() {
        loginType = "up_otp";
        binding.inputEmail.setVisibility(View.GONE);
        binding.inputPassword.setVisibility(View.GONE);
        binding.inputPasswordConfirm.setVisibility(View.GONE);
        binding.inputUserName.setVisibility(View.GONE);
        binding.loginSignupForgetPassButton.setText("Verify ->");

        binding.signInUpInput.setVisibility(View.VISIBLE);
        binding.signInMailBtn.setVisibility(View.VISIBLE);
        binding.signupMailBtn.setVisibility(View.VISIBLE);
        binding.forgetPassButton.setVisibility(View.VISIBLE);

        binding.inputOTP.setVisibility(View.VISIBLE);
        binding.verifyOTPText.setVisibility(View.VISIBLE);
    }

    private void handleLoginSignupAction() {
        if (loginType == null) {
            Toast.makeText(this, "Please retry!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loginType.equals("in")) {
            if (validateSignIn()) {
                email = binding.inputEmailText.getText().toString();
                signInUser(binding.inputEmailText.getText().toString(), binding.inputPasswordText.getText().toString());
            }
        } else if (loginType.equals("up")) {
            if (validateSignUp()) {
                email = binding.inputEmailText.getText().toString();
                signUpUser(binding.inputEmailText.getText().toString(), binding.inputUserNameText.getText().toString(), binding.inputPasswordText.getText().toString());
            }
        } else if (loginType.equals("forget")) {
            if(validateForgetPassword()) {
                resetUserPassword(binding.inputEmailText.getText().toString(), binding.inputPasswordText.getText().toString());
            }
        } else if (loginType.equals("up_otp")) {
            if(validateOPTInput()) {
                verifyOTPSignup(email, binding.inputOTPText.getText().toString());
            }
        }
    }

    private void verifyOTPSignup(String email, String otp) {
        String url = getString(R.string.base_url) + "/api/auth/verify-otp";
        JSONObject requestBody = new JSONObject();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            requestBody.put("email", email);
            requestBody.put("otp", otp);
        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        progressDialog.dismiss();
//                        Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show();
                        boolean success = response.getBoolean("success");
                        if (success) {
                            String token = response.getString("token");

                            getUserData(token);

                            Toast.makeText(this, response.getString("msg"), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(this, "OTP Verification failed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "OTP Verification failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        // Add request to Volley queue
        Volley.newRequestQueue(this).add(request);
    }

    private void getUserData(String token) {
        String url = getString(R.string.base_url) + "/api/auth";

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        progressDialog.dismiss();
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject userJson = response.getJSONObject("user");
                            JSONArray tasksArray = userJson.optJSONArray("tasks");
                            List<Task> taskList = new ArrayList<>();

                            if (tasksArray != null) {
                                for (int i = 0; i < tasksArray.length(); i++) {
                                    JSONObject taskJson = tasksArray.getJSONObject(i);
                                    Task task = new Task(
                                            taskJson.getString("_id"),
                                            taskJson.getString("email_id"),
                                            taskJson.getString("email_pass"),
                                            jsonArrayToList(taskJson.getJSONArray("recipients")),
                                            taskJson.getString("mail_body"),
                                            taskJson.getString("mail_subject"),
                                            taskJson.getString("user"),
                                            taskJson.getString("created_at"),
                                            taskJson.getString("sender_name"),
                                            taskJson.getString("task_name")
                                    );
                                    taskList.add(task);
                                }
                            }
                            // Save user info
                            User user = new User(
                                    userJson.getString("_id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("avatar"),
                                    taskList
                            );

                            saveUserData(token, user);

                            binding.welcomeContent.setVisibility(View.GONE);
                            binding.premiumUnlockedView.setVisibility(View.VISIBLE);

                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Intent intent = new Intent(UserLoginSignupActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }, 2000);

                            // Proceed to main activity
                            // Intent intent = new Intent(this, MainActivity.class);
                            // startActivity(intent);
                            // finish();


                        } else {
                            Toast.makeText(this, "Failed to fetch user data!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        System.out.println("Ottp --> " + e.getMessage());
                        Toast.makeText(this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    private void resetUserPassword(String string, String string1) {

    }

    private void signUpUser(String email, String userName, String password) {
        String url = getString(R.string.base_url) + "/api/auth/register";
        JSONObject requestBody = new JSONObject();

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            requestBody.put("username", userName);
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        progressDialog.dismiss();
                        boolean success = response.getBoolean("success");
                        if (success) {
                            Toast.makeText(this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                            setOTPLayout();
                        } else {
                            Toast.makeText(this, "Signup failed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Signup failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        // Add request to Volley queue
        Volley.newRequestQueue(this).add(request);
    }

    private void signInUser(String email, String password) {
        String url = getString(R.string.base_url) + "/api/auth/login";

        if (!isInternetAvailable()) {
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("password", password);
        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    progressDialog.dismiss();

                    try {
                        if (response.getBoolean("success")) {
                            String token = response.getString("token");
                            JSONObject userJson = response.getJSONObject("user");

                            JSONArray tasksArray = userJson.optJSONArray("tasks");
                            List<Task> taskList = new ArrayList<>();

                            if (tasksArray != null) {
                                for (int i = 0; i < tasksArray.length(); i++) {
                                    JSONObject taskJson = tasksArray.getJSONObject(i);
                                    Task task = new Task(
                                            taskJson.getString("_id"),
                                            taskJson.getString("email_id"),
                                            taskJson.getString("email_pass"),
                                            jsonArrayToList(taskJson.getJSONArray("recipients")),
                                            taskJson.getString("mail_body"),
                                            taskJson.getString("mail_subject"),
                                            taskJson.getString("user"),
                                            taskJson.getString("created_at"),
                                            taskJson.getString("sender_name"),
                                            taskJson.getString("task_name")
                                    );
                                    taskList.add(task);
                                }
                            }

                            // Save user info
                            User user = new User(
                                    userJson.getString("id"),
                                    userJson.getString("username"),
                                    userJson.getString("email"),
                                    userJson.getString("avatar"),
                                    taskList
                            );

                            saveUserData(token, user);

                            // Redirect to MainActivity
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            finish();

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, response.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Login failed! Please check credentials.", Toast.LENGTH_SHORT).show();
                    Log.e("VolleyError", "Error: " + error.toString());
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
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

    private boolean validateSignIn() {
        if (binding.inputEmailText.getText().toString().isEmpty()) {
            binding.inputEmail.setError("Please enter Email");
            return false;
        } else {
            binding.inputEmail.setError("");
        }

        if (binding.inputPasswordText.getText().toString().isEmpty()) {
            binding.inputPasswordText.setError("Please enter Password");
            return false;
        } else {
            binding.inputPasswordText.setError("");
            Toast.makeText(this, "Signing In...", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean validateSignUp() {
        if (binding.inputEmailText.getText().toString().isEmpty()) {
            binding.inputEmail.setError("Please enter Email");
            return false;
        } else {
            binding.inputEmail.setError("");
        }

        if (binding.inputUserNameText.getText().toString().isEmpty()) {
            binding.inputUserName.setError("Please enter User Name");
            return false;
        } else {
            binding.inputUserName.setError("");
        }

        if (binding.inputPasswordText.getText().toString().isEmpty()) {
            binding.inputPasswordText.setError("Please enter Password");
            return false;
        } else {
            binding.inputPasswordText.setError("");
        }

        if (binding.inputPasswordConfirmText.getText().toString().isEmpty()) {
            binding.inputPasswordConfirmText.setError("Please enter Confirm Password");
            return false;
        } else {
            binding.inputPasswordConfirmText.setError("");
        }

        if (!binding.inputPasswordText.getText().toString().equals(binding.inputPasswordConfirmText.getText().toString())) {
            binding.inputPasswordConfirm.setError("Password mismatch");
            return false;
        } else {
            // Proceed with SignUp() function
            binding.inputPasswordConfirm.setError("");
            Toast.makeText(this, "Signing Up...", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean validateForgetPassword() {
        if (binding.inputEmailText.getText().toString().isEmpty()) {
            binding.inputEmail.setError("Enter your registered email ID");
            return false;
        } else {
            // Proceed with resetPassword() function
            binding.inputEmail.setError("");
            Toast.makeText(this, "Resetting Password...", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean validateOPTInput() {
        if (binding.inputOTPText.getText().toString().isEmpty()) {
            binding.inputEmail.setError("Enter OTP to verify");
            return false;
        } if (binding.inputOTPText.getText().toString().length() < 6 || binding.inputOTPText.getText().toString().length() > 6) {
            binding.inputEmail.setError("Enter 6 digit OTP to verify");
            return false;
        } else {
            binding.inputEmail.setError("");
            Toast.makeText(this, "Verifying Email...", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}