package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    enum State {
        SIGNUP, LOGIN;
    }

    private State state;
    private Button btnSignUpLogIn, btnOneTimeLogIn;
    private RadioButton rdbDriver, rdbPassenger;
    private EditText edtUsername, edtPassword, edtDP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null) {
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }
        btnSignUpLogIn = findViewById(R.id.btnSignUpLogIn);
        rdbDriver = findViewById(R.id.rdbDriver);
        rdbPassenger = findViewById(R.id.rdbPassenger);
        btnOneTimeLogIn = findViewById(R.id.btnOneTimeLogin);
        btnOneTimeLogIn.setOnClickListener(this);

        state = State.SIGNUP;

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtDP = findViewById(R.id.edtDP);

        btnSignUpLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == State.SIGNUP) {
                    if (rdbDriver.isChecked() == false && rdbPassenger.isChecked() == false) {
                        FancyToast.makeText(MainActivity.this, "Are you driver or passenger?", Toast.LENGTH_SHORT, FancyToast.INFO, false).show();
                        return;
                    }

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUsername.getText().toString().trim());
                    appUser.setPassword(edtPassword.getText().toString().trim());
                    if (rdbDriver.isChecked()) {
                        appUser.put("as", "Driver");
                    } else if (rdbPassenger.isChecked()) {
                        appUser.put("as", "Passenger");
                    }

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                FancyToast.makeText(MainActivity.this, "Signed Up", Toast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });


                } else if (state == State.LOGIN) {
                    ParseUser.logInInBackground(edtUsername.getText().toString().trim(), edtPassword.getText().toString().trim(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null && e == null) {
                                FancyToast.makeText(MainActivity.this, "User Logged", Toast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.loginItem:

                if (state == State.SIGNUP) {
                    state = State.LOGIN;
                    item.setTitle("Sign Up");
                    btnSignUpLogIn.setText("Log In");
                } else if (state == State.LOGIN) {
                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogIn.setText("Sign Up");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        if (edtDP.getText().toString().trim().equals("Driver") || edtDP.getText().toString().trim().equals("Passenger")) {
            if (ParseUser.getCurrentUser() == null) {
                ParseAnonymousUtils.logIn(new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            FancyToast.makeText(MainActivity.this, "We have an anonymous user", Toast.LENGTH_SHORT, FancyToast.INFO, false).show();
                            user.put("as", edtDP.getText().toString().trim());
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    transitionToPassengerActivity();
                                    transitionToDriverRequestListActivity();
                                }
                            });
                        }
                    }
                });
            }
        }

    }

    private void transitionToPassengerActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }

        }
    }

    private void transitionToDriverRequestListActivity() {
        if (ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().get("as").equals("Driver")) {
                Intent intent = new Intent(MainActivity.this, DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }
}
