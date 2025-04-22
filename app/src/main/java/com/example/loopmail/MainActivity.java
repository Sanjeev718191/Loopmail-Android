package com.example.loopmail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.loopmail.databinding.ActivityMainBinding;
import com.example.loopmail.fragment.HomeFragment;
import com.example.loopmail.fragment.UserFragment;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Bundle taskBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences("LoopMailPrefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("auth_token", null);

        replaceFragment(new HomeFragment());

        binding.mainBottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.tasks) {
                HomeFragment homeFragment = new HomeFragment();
//                if (taskBundle != null) {
//                    homeFragment.setArguments(taskBundle);
//                }
                replaceFragment(homeFragment);
            } else {
                replaceFragment(new UserFragment());
            }
            return true;
        });

        binding.mailFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TaskFormActivity.class);
                startActivity(intent);
            }
        });

//        if (token != null) {
//            fetchTasks(token);
//        }

    }

//    private void fetchTasks(String token) {
//
//        String url = getString(R.string.base_url) + "/api/task/getAll";
//
//        ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Loading...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
//                response -> {
//                    try {
//                        progressDialog.dismiss();
//                        boolean success = response.getBoolean("success");
//                        if (success) {
//                            JSONArray tasksArray = response.optJSONArray("tasks");
//                            List<Task> taskList = new ArrayList<>();
//
//                            if (tasksArray != null) {
//                                for (int i = 0; i < tasksArray.length(); i++) {
//                                    JSONObject taskJson = tasksArray.getJSONObject(i);
//                                    Task task = new Task(
//                                            taskJson.getString("_id"),
//                                            taskJson.getString("email_id"),
//                                            taskJson.getString("email_pass"),
//                                            jsonArrayToList(taskJson.getJSONArray("recipients")),
//                                            taskJson.getString("mail_body"),
//                                            taskJson.getString("mail_subject"),
//                                            taskJson.getString("user"),
//                                            taskJson.getString("created_at"),
//                                            taskJson.getString("sender_name"),
//                                            taskJson.getString("task_name")
//                                    );
//                                    taskList.add(task);
//                                }
//                            }
//
//                            //Toast.makeText(this, "task data: " + tasksArray.toString(), Toast.LENGTH_LONG).show();
//                            //System.out.println("Task data -> " + tasksArray.toString());
//                            //Task data -> [{"_id":"67c083d74556441220222b5b","email_id":"sanjeev19203@gmail.com","email_pass":"zjvjehdewjulozvc","recipients":["tempmovie08@gmail.com","sanjeev718191@gmail.com"],"mail_body":"Hi user\/nI am Sanjeev.","mail_subject":"Sample mail Header","user":"67b2ef3f9166f6388bb77d12","created_at":"2025-02-27T15:25:11.981Z","__v":0},{"_id":"67e82ce4caa56fa1456ced58","email_id":"sanjeev192034@gmail.com","email_pass":"zjvjehdewjulozvc","recipients":["tempmovie08@gmail.com","sanjeev718191@gmail.com"],"mail_body":"Hi user\/nI am Sanjeev.","mail_subject":"Sample mail Header","user":"67b2ef3f9166f6388bb77d12","created_at":"2025-03-29T17:24:52.776Z","__v":0}]
//
//                            taskBundle = new Bundle();
//                            taskBundle.putSerializable("taskList", new ArrayList<>(taskList));
//
//                            // Replace fragment with data on successful fetch
//                            HomeFragment homeFragment = new HomeFragment();
//                            homeFragment.setArguments(taskBundle);
//                            replaceFragment(homeFragment);
//
//
//                        } else {
//                            Toast.makeText(this, "Failed to fetch task data!", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        progressDialog.dismiss();
//                        e.printStackTrace();
//                        Toast.makeText(this, "Error parsing task data", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    progressDialog.dismiss();
//                    Toast.makeText(this, "Failed to fetch task data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//                }) {
//            @Override
//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", token);
//                return headers;
//            }
//        };
//
//        // Add request to Volley queue
//        Volley.newRequestQueue(this).add(request);
//    }

//    private List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
//        List<String> list = new ArrayList<>();
//        for (int i = 0; i < jsonArray.length(); i++) {
//            list.add(jsonArray.getString(i));
//        }
//        return list;
//    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.mainFrameLayout, fragment);
        fragmentTransaction.commit();
    }
}