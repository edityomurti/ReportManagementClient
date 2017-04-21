package com.solusi247.reportmanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.android.md5simply.MD5;
import com.solusi247.reportmanagement.R;
import com.solusi247.reportmanagement.adapter.rest.Rest;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    EditText nameText;
    EditText emailText;
    EditText passwordText;
    EditText repasswordText;
    Button signupButton;
    TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        nameText = (EditText) findViewById(R.id.input_name);
        emailText = (EditText) findViewById(R.id.input_email);
        passwordText = (EditText) findViewById(R.id.input_password);
        repasswordText = (EditText) findViewById(R.id.reinput_password);
        signupButton = (Button) findViewById(R.id.btn_signup);
        loginLink = (TextView) findViewById(R.id.link_login);


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });
    }


    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String email = emailText.getText().toString().trim();

        // TODO: Implement your own signup logic here.
        // checking an existing email
        Rest rest = new Rest();
        rest.getRestApi().findUser(email, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                if (response.getStatus() == 204){
                    String name = nameText.getText().toString().trim();
                    String email = emailText.getText().toString().trim();
                    String password = MD5.encrypt(passwordText.getText().toString().trim());
                    Rest rest = new Rest();
                    rest.getRestApi().createUser(name, email, password, new Callback<Response>() {
                        @Override
                        public void success(Response response, Response response2) {
                            progressDialog.dismiss();
                            onSignupSuccess();
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.i(TAG, "error creating user" + error);
                            Toast.makeText(SignupActivity.this, "Please check your connectivity", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (response.getStatus() == 200) {
                    progressDialog.dismiss();
                    Toast.makeText(SignupActivity.this, "Email already exist", Toast.LENGTH_SHORT).show();
                    onSignupFailed();
                    return;
                } else {
                    Toast.makeText(SignupActivity.this, "Please check your connectivity", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void failure(RetrofitError error) {
                Log.i(TAG, "error find account" + error);
                Toast.makeText(SignupActivity.this, "Please check your connectivity", Toast.LENGTH_SHORT).show();
            }
        });


//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        // On complete call either onSignupSuccess or onSignupFailed
//                        // depending on success
//                        onSignupSuccess();
//                        // onSignupFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 3000);
    }

    public void onSignupSuccess() {
        Toast.makeText(SignupActivity.this, "Account created successfully, please now login", Toast.LENGTH_SHORT).show();
        signupButton.setEnabled(true);
//        setResult(RESULT_OK, null);
//        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

    }

    public void onSignupFailed() {
//        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_LONG).show();
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();
        String repassword = repasswordText.getText().toString().trim();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("at least 3 characters");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (repassword.isEmpty() || !repassword.equals(password)) {
            repasswordText.setError("password not matched");
            valid = false;
        } else {
            repasswordText.setError(null);
        }

        return valid;
    }
}