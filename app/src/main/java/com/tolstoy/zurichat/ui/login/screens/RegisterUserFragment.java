package com.tolstoy.zurichat.ui.login.screens;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.tolstoy.zurichat.R;
import com.tolstoy.zurichat.data.remoteSource.RetrofitClient;
import com.tolstoy.zurichat.data.remoteSource.RetrofitService;
import com.tolstoy.zurichat.di.RetrofitModule;
import com.tolstoy.zurichat.models.RegisterUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RegisterUserFragment extends Fragment {

    private TextView textView_login;
    private NavController navController;
    private Button btnRegister;
    private TextInputLayout email, password, password2;
    private ProgressDialog progressDialog;
    private RetrofitService retrofitService;
    private CheckBox checkBox;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView_login = view.findViewById(R.id.textView_signin);
        navController = Navigation.findNavController(view);
        bundle = new Bundle();
        checkBox = view.findViewById(R.id.checkBox);

        retrofitService = RetrofitClient.getClient("https://api.zuri.chat/").create(RetrofitService.class);

        email = view.findViewById(R.id.user_email);
        password = view.findViewById(R.id.user_password);
        password2 = view.findViewById(R.id.confirm_user_password);
        progressDialog = new ProgressDialog(getContext());

        btnRegister = view.findViewById(R.id.button_signUp);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = email.getEditText().getText().toString();
                String userPassword = password.getEditText().getText().toString();

                if (userEmail.isEmpty()){
                    email.setError("Email is required");
                }else if(userPassword.isEmpty()){
                    email.setError(null);
                    password.setError("Password cannot be empty");
                }else if(!isValidPassword(userPassword.trim())){
                    password.setError("Invalid password pattern");
                }else if(!password.getEditText().getText().toString().equals(password2.getEditText().getText().toString())){
                    password2.setError("Password does not match");
                }else if(!checkBox.isChecked()){
                    checkBox.setError("Terms and conditions must be accepted!");
                } else{

                    password.setError(null);
                    password2.setError(null);
                    bundle.putString("email", userEmail);
                    registerUser("", "", "", userEmail, userPassword, "");
                }

            }
        });

        textView_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_registerUserFragment_to_loginFragment, bundle);
            }
        });
    }

    public void registerUser(String first_name, String last_name, String display_name, String email, String password, String phone) {
        progressDialog.show();
        final RegisterUser registerUser = new RegisterUser(first_name, last_name, display_name, email, password, phone);
        Call<RegisterUser> call = retrofitService.register(registerUser);
        call.enqueue(new Callback<RegisterUser>() {
            @Override
            public void onResponse(Call<RegisterUser> call, Response<RegisterUser> response) {
                if (response.code() == 400){
                    Toast.makeText(getContext(), "User Already Exists!", Toast.LENGTH_LONG).show();

                }else {
                    if (response.body().getMessage().matches("user created")){
                        Toast.makeText(getContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                        navController.navigate(R.id.action_registerUserFragment_to_emailVerificationFragment, bundle);
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<RegisterUser> call, Throwable t) {

            }
        });

    }

    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        //return matcher.matches();
        return true;
    }
}