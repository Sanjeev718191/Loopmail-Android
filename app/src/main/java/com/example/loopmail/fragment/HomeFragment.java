package com.example.loopmail.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.loopmail.R;
import com.example.loopmail.TaskFormActivity;
import com.example.loopmail.TaskViewAdapter;
import com.example.loopmail.UserLicenseActivity;
import com.example.loopmail.model.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class HomeFragment extends Fragment {

    private RecyclerView taskRecyclerView;
    private TaskViewAdapter adapter;
    private List<Task> taskList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);

        taskList = new ArrayList<>();
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TaskViewAdapter(getContext(), taskList);
        taskRecyclerView.setAdapter(adapter);

        enableSwipeToDelete();

        // Set initial data if present in arguments
//        if (getArguments() != null) {
//            List<Task> initialList = (List<Task>) getArguments().getSerializable("taskList");
//            if (initialList != null) {
//                taskList.addAll(initialList);
//                adapter.notifyDataSetChanged();
//            }
//        }

        fetchTasksAgain();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchTasksAgain(); // refresh logic
        });

        LottieAnimationView premiumAnimation = view.findViewById(R.id.homeMainPremiumAnimation);

        premiumAnimation.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserLicenseActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTasksAgain();
    }

    private void enableSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task taskToDelete = taskList.get(position);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            deleteTask(taskToDelete.getTaskId(), position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            adapter.notifyItemChanged(position); // Reset swipe
                        });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray))
                        .addActionIcon(R.drawable.outline_auto_delete_24) // Make sure this icon exists in drawable
                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(taskRecyclerView);
    }

    private void deleteTask(String taskId, int position) {
        String url = getString(R.string.base_url) + "/api/task/delete/" + taskId;

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting task...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoopMailPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("auth_token", null);

        if (token == null) {
            Toast.makeText(getContext(), "Auth token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    progressDialog.dismiss();
                    taskList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    adapter.notifyItemChanged(position);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void fetchTasksAgain() {
        swipeRefreshLayout.setRefreshing(true);

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Refreshing Data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoopMailPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("auth_token", null);

        if (token == null) {
            Toast.makeText(getContext(), "No auth token found", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            return;
        }

        String url = getString(R.string.base_url) + "/api/task/getAll";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    swipeRefreshLayout.setRefreshing(false);
                    progressDialog.dismiss();

                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray tasksArray = response.optJSONArray("tasks");
                            List<Task> refreshedList = new ArrayList<>();

                            // System.out.println("All tasks -> " + tasksArray.get(tasksArray.length()-1).toString());

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
                                    refreshedList.add(task);
                                }
                            }

                            taskList.clear();
                            taskList.addAll(refreshedList);
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(getContext(), "Failed to fetch task data!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing task data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    swipeRefreshLayout.setRefreshing(false);
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Refresh failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", token);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private List<String> jsonArrayToList(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }
}