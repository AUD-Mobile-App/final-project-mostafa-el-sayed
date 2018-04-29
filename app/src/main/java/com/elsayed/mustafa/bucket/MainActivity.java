package com.elsayed.mustafa.bucket;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity
{
    // global variables
    public static FirebaseAuth firebaseAuth;
    public static DatabaseReference databaseReference;

    // GUI elements
    EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize global variables
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = (FirebaseDatabase.getInstance()).getReference();
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        // if user is remembered, redirect them to bucket list
        if(firebaseAuth.getCurrentUser() != null)
        {
            startBucketListActivity();
        }
    }

    // GUI functions
    public void registerClicked(View view)
    {
        // ensure fields are not empty
        if (emailEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input an email address", Toast.LENGTH_LONG).show();
            return;
        }
        if (passwordEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input a password", Toast.LENGTH_LONG).show();
            return;
        }

        // ensure fields are correct
        if (!emailEditText.getText().toString().contains("@") || !emailEditText.getText().toString().contains("."))
        {
            Toast.makeText(this, "Email address is in an incorrect format", Toast.LENGTH_LONG).show();
            return;
        }
        if (passwordEditText.getText().toString().length() < 6)
        {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_LONG).show();
            return;
        }

        // register
        try
        {
            // attempt to register
            firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                startBucketListActivity(); // redirect to bucket list activity
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "An error has occured. Perhaps an account already exists?", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch(Exception exc)
        {
            Toast.makeText(this, "An error has occured. Make sure Google Play Services are updated.", Toast.LENGTH_SHORT).show();
        }
    }
    public void loginClicked(View view)
    {
        // ensure fields are not empty
        if (emailEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input an email address", Toast.LENGTH_LONG).show();
            return;
        }
        if (passwordEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input a password", Toast.LENGTH_LONG).show();
            return;
        }


        try
        {
            // attempt to login
            firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        startBucketListActivity(); // start bucket list activity
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Account not found! Make sure the credentials are correct, or tap the register button to register a new account", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch(Exception exc)
        {
            Toast.makeText(this, "An error has occured. Make sure Google Play Services are updated.", Toast.LENGTH_SHORT).show();
        }
    }

    // Functions
    private  void startBucketListActivity()
    {
        startActivity(new Intent(this, BucketListActivity.class));
        finish();
    }
}





