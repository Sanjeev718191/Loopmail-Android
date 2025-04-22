package com.example.loopmail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.loopmail.databinding.ActivityMainBinding;
import com.example.loopmail.databinding.ActivityTaskFormBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TaskFormActivity extends AppCompatActivity {

    ActivityTaskFormBinding binding;
    String taskId;
    String userId;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskId = getIntent().getStringExtra("taskId");
        userId = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE).getString("user_id", null);
        token = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE).getString("auth_token", null);

        if (taskId != null) {
            fetchTaskById(taskId);
        } else {
            showSaveButton();
        }

        binding.saveButton.setOnClickListener(v -> saveTask());

        binding.updateButton.setOnClickListener(v -> updateTask());

        binding.sendMailButton.setOnClickListener(v -> sendMail());

    }

    private void fetchTaskById(String id) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Fetching task...");
        dialog.setCancelable(false);
        dialog.show();

        if (token == null) {
            Toast.makeText(this, "No auth token", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        String url = getString(R.string.base_url) + "/api/task/get/" + id;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONObject taskJson = response.getJSONObject("task");
//                            Toast.makeText(this, "Task data " + taskJson.toString(), Toast.LENGTH_SHORT).show();

                            binding.inputTaskEmailText.setText(taskJson.getString("email_id"));
                            binding.inputTaskPasswordText.setText(taskJson.getString("email_pass"));
                            binding.inputSubjectText.setText(taskJson.getString("mail_subject"));
                            binding.inputBodyText.setText(taskJson.getString("mail_body"));
                            binding.inputTaskNameText.setText(taskJson.getString("task_name"));
                            binding.inputUserNameText.setText(taskJson.getString("sender_name"));

                            JSONArray recipientsArray = taskJson.getJSONArray("recipients");
                            String recipientsString = getCommaSeparatedEmails(recipientsArray);
                            binding.inputRecipientsText.setText(recipientsString);

                            showUpdateLayout();
                        } else {
                            Toast.makeText(this, "Failed to fetch task", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Request failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.Map<String, String> headers = new java.util.HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String getCommaSeparatedEmails(JSONArray recipientsArray) {
        StringBuilder emails = new StringBuilder();
        for (int i = 0; i < recipientsArray.length(); i++) {
            try {
                emails.append(recipientsArray.getString(i));
                if (i != recipientsArray.length() - 1) {
                    emails.append(", ");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return emails.toString();
    }

    private void showSaveButton() {
        binding.saveButton.setVisibility(View.VISIBLE);
        binding.updateSendLayout.setVisibility(View.GONE);
    }

    private void showUpdateLayout() {
        binding.saveButton.setVisibility(View.GONE);
        binding.updateSendLayout.setVisibility(View.VISIBLE);
    }

    private void saveTask() {

        if (!validateTaskFields()) return;

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Creating task...");
        dialog.setCancelable(false);
        dialog.show();

        if (token == null) {
            Toast.makeText(this, "No auth token", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        String taskName = binding.inputTaskNameText.getText().toString();
        String email = binding.inputTaskEmailText.getText().toString();
        String password = binding.inputTaskPasswordText.getText().toString();
        String senderName = binding.inputUserNameText.getText().toString();
        String subject = binding.inputSubjectText.getText().toString();
        String body = binding.inputBodyText.getText().toString();
        String recipientsRaw = binding.inputRecipientsText.getText().toString();

        JSONArray recipients = new JSONArray();
        for (String recipient : recipientsRaw.split(",")) {
            recipients.put(recipient.trim());
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user", this.userId);
            requestBody.put("email_id", email);
            requestBody.put("email_pass", password);
            requestBody.put("mail_subject", subject);
            requestBody.put("mail_body", body);
            requestBody.put("recipients", recipients);
            requestBody.put("sender_name", senderName);
            requestBody.put("task_name", taskName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.base_url) + "/api/task/create";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Task saved!", Toast.LENGTH_SHORT).show();
                    showUpdateLayout();
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to save task", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void updateTask() {

        if (!validateTaskFields()) return;

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Updating task...");
        dialog.setCancelable(false);
        dialog.show();

        if (token == null) {
            Toast.makeText(this, "No auth token", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }
        String taskName = binding.inputTaskNameText.getText().toString();
        String email = binding.inputTaskEmailText.getText().toString();
        String password = binding.inputTaskPasswordText.getText().toString();
        String senderName = binding.inputUserNameText.getText().toString();
        String subject = binding.inputSubjectText.getText().toString();
        String body = binding.inputBodyText.getText().toString();
        String recipientsRaw = binding.inputRecipientsText.getText().toString();

        JSONArray recipients = new JSONArray();
        for (String recipient : recipientsRaw.split(",")) {
            recipients.put(recipient.trim());
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user", this.userId);
            requestBody.put("email_id", email);
            requestBody.put("email_pass", password);
            requestBody.put("mail_subject", subject);
            requestBody.put("mail_body", body);
            requestBody.put("recipients", recipients);
            requestBody.put("sender_name", senderName);
            requestBody.put("task_name", taskName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.base_url) + "/api/task/update/" + taskId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                requestBody,
                response -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to update + " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void sendMail() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Sending Mails...");
        dialog.setCancelable(false);
        dialog.show();

        if (token == null) {
            Toast.makeText(this, "No auth token", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("taskId", taskId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.base_url) + "/api/sendmail";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Mail sent!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Failed to send mail", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private boolean validateTaskFields() {
        boolean isValid = true;

        if (binding.inputTaskNameText.getText().toString().trim().isEmpty()) {
            binding.inputTaskName.setError("Please enter task name");
            isValid = false;
        } else {
            binding.inputTaskName.setError(null);
        }

        if (binding.inputTaskEmailText.getText().toString().trim().isEmpty()) {
            binding.inputTaskEmail.setError("Please enter email");
            isValid = false;
        } else {
            binding.inputTaskEmail.setError(null);
        }

        if (binding.inputTaskPasswordText.getText().toString().trim().isEmpty()) {
            binding.inputTaskPassword.setError("Please enter password");
            isValid = false;
        } else {
            binding.inputTaskPassword.setError(null);
        }

        if (binding.inputUserNameText.getText().toString().trim().isEmpty()) {
            binding.inputUserName.setError("Please enter sender name");
            isValid = false;
        } else {
            binding.inputUserName.setError(null);
        }

        if (binding.inputSubjectText.getText().toString().trim().isEmpty()) {
            binding.inputSubject.setError("Please enter subject");
            isValid = false;
        } else {
            binding.inputSubject.setError(null);
        }

        if (binding.inputBodyText.getText().toString().trim().isEmpty()) {
            binding.inputBody.setError("Please enter mail body");
            isValid = false;
        } else {
            binding.inputBody.setError(null);
        }

        if (binding.inputRecipientsText.getText().toString().trim().isEmpty()) {
            binding.inputRecipients.setError("Please enter at least one recipient");
            isValid = false;
        } else {
            binding.inputRecipients.setError(null);
        }

        return isValid;
    }
}