package com.solusi247.reportmanagement.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.android.md5simply.MD5;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;
import com.solusi247.reportmanagement.model.UserData;
import com.solusi247.reportmanagement.util.SessionManager;
import com.solusi247.reportmanagement.util.SharedPreference;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static final int REQUEST_SIGNUP = 0;
    SessionManager sessionManager;

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    private SharedPreference sharedPreference;
    Activity context = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(getApplicationContext());

        sharedPreference = new SharedPreference();
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        loginButton = (Button) findViewById(R.id.btn_login);
        signupLink = (TextView) findViewById(R.id.link_signup);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });

    }


    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
           return;
        }

//        loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        // TODO: Implement your own authentication logic here.
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(emailText.getWindowToken(), 0);

        //check an existing email
        Rest rest = new Rest();
        rest.getRestApi().findUser(email, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                progressDialog.dismiss();
                if (response.getStatus()==200) {
                    String email = emailText.getText().toString().trim();
                    String password = MD5.encrypt(passwordText.getText().toString().trim());
                    Log.e("Insert Report", "encrypted password : " + email + " : " + password);
                    Rest rest = new Rest();
                    rest.getRestApi().getUser(email, password, new Callback<UserData>() {
                        @Override
                        public void success(UserData userData, Response response) {
                            if (response.getStatus()==200) {
                                String user_id;
                                String name;
                                String email;
                                user_id = String.valueOf(userData.getUser_id());
                                name = userData.getName();
                                email = userData.getEmail();

                                sessionManager.createUserLoginSession(user_id, name, email);

                                Intent intent = new Intent(LoginActivity.this, ViewReportActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                startActivity(intent);
                                finish();
                            } else if (response.getStatus()==204){
                                passwordText.setError("Wrong password");
                                Toast.makeText(LoginActivity.this, "Email and password doesn't matched", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Toast.makeText(LoginActivity.this, "Please check your connectivity", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (response.getStatus()==204) {
                    Toast.makeText(LoginActivity.this, "Email not registered", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Please check your connectivity", Toast.LENGTH_SHORT).show();
            }
        });


        // Save the text in SharedPreference
//        sharedPreference.save(context, email);
//        Toast.makeText(context,
//                "Sudah di simpan di SharedPreferences.",
//                Toast.LENGTH_LONG).show();

//        String emailLogin = emailText.getText().toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("emailLogin", emailLogin);
//        getSharedPreferences("emailLogin", );

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onLoginSuccess or onLoginFailed
//                        onLoginSuccess();
//                        // onLoginFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
//
//        Intent intent = new Intent(getApplicationContext(), ViewReportActivity.class);
//        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SIGNUP) {
//            if (resultCode == RESULT_OK) {
//
//                // TODO: Implement successful signup logic here
//
//                this.finish();
//            }
//        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        int counter = 3;

//        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 ) {
            passwordText.setError("minimum 4 characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
