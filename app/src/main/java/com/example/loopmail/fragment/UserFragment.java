package com.example.loopmail.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.loopmail.R;
import com.example.loopmail.UserLicenseActivity;
import com.example.loopmail.WelcomeActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFragment extends Fragment {

    private CircleImageView avatarImageView;
    private TextView usernameTextView, userMailTextView, infoUsername, infoEmail, infoLicense;
    private AppCompatButton userLogoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        userMailTextView = view.findViewById(R.id.userMailTextView);
        infoUsername = view.findViewById(R.id.infoUsername);
        infoEmail = view.findViewById(R.id.infoEmail);
        infoLicense = view.findViewById(R.id.infoLicense);
        userLogoutButton = view.findViewById(R.id.userLogoutButton);

        loadUserData();

        userLogoutButton.setOnClickListener(v -> logout());

        LottieAnimationView premiumAnimation = view.findViewById(R.id.topRightLottie);

        premiumAnimation.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), UserLicenseActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoopMailPrefs", Context.MODE_PRIVATE);

        String username = sharedPreferences.getString("username", "N/A");
        String email = sharedPreferences.getString("email", "N/A");
        String avatarUrl = sharedPreferences.getString("avatar", "");

        usernameTextView.setText(username);
        userMailTextView.setText(email);
        infoUsername.setText(username);
        infoEmail.setText(email);
        infoLicense.setText("Premium User");

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.people_icon)
                .into(avatarImageView);
    }

    private void logout() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoopMailPrefs", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears the back stack
        startActivity(intent);
        requireActivity().finish();
    }
}