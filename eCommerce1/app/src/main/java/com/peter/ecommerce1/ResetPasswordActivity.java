package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.peter.ecommerce1.Model.Users;
import com.peter.ecommerce1.Prevalent.Prevalent;

import java.util.HashMap;

public class ResetPasswordActivity extends AppCompatActivity
{
    private String checkType="";

    private EditText resetPhoneNumber, question1,question2;
    private Button verifySecurityCheckQuestions;
    private TextView pageTitle, questionTitle;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        checkType = getIntent().getStringExtra("check");

        initViews();
    }


    private void initViews()
    {
        resetPhoneNumber = findViewById(R.id.reset_password_phone_number);
        question1 = findViewById(R.id.reset_security_question_1);
        question2 = findViewById(R.id.reset_security_question_2);
        verifySecurityCheckQuestions = findViewById(R.id.verify_questions);
        pageTitle = findViewById(R.id.page_title);
        questionTitle = findViewById(R.id.reset_text_headline);

        mDialog = new ProgressDialog(this);
    }


    @Override
    protected void onStart()
    {
        super.onStart();


        resetPhoneNumber.setVisibility(View.GONE);

        if (checkType.equals("settings"))
        {

            pageTitle.setText("Set Answers");
            questionTitle.setText("Set Answers for the following security questions");
            verifySecurityCheckQuestions.setText("Set Answers");

            displayPreviousAnswers();

            verifySecurityCheckQuestions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    verifyInputs();
                }
            });
        }
        else if (checkType.equals("login"))
        {
            resetPhoneNumber.setVisibility(View.VISIBLE);

            verifySecurityCheckQuestions.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    verifyUserInputs();

                }
            });
        }
    }



    private void verifyUserInputs()
    {
        mDialog.setMessage("Please wait");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String phone = resetPhoneNumber.getText().toString().trim();
        String ans1 = question1.getText().toString().toLowerCase();
        String ans2 = question2.getText().toString().toLowerCase();

        if (!phone.isEmpty())
        {
            if (!ans1.isEmpty())
            {
                if (!ans2.isEmpty())
                {

                    allowReset(phone,ans1,ans2);
                }
                else
                {
                    question2.setError("Phone Number is required");
                    question2.requestFocus();
                    mDialog.dismiss();
                }

            }
            else
            {
                question1.setError("Phone Number is required");
                question1.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            resetPhoneNumber.setError("Phone Number is required");
            resetPhoneNumber.requestFocus();
            mDialog.dismiss();
        }
    }


    private void allowReset(final String phone, final String ans1, final String ans2) {

//        if (!(phone).equals("") && ans1.equals("") && ans2.equals("")) {

            final DatabaseReference mRef;
            mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(phone);


            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        String number = dataSnapshot.child("phoneNumber").getValue().toString();


                        if (number.equals(phone) || number.equals(Prevalent.currentOnlineUser.getPhoneNumber())) {

                            if (dataSnapshot.hasChild("Security Questions")) {
                                String answer1 = dataSnapshot.child("Security Questions").child("answerOne").getValue().toString();
                                String answer2 = dataSnapshot.child("Security Questions").child("answerTwo").getValue().toString();

                                if (answer1.equals(ans1)) {
                                    if (answer2.equals(ans2)) {

                                        Toast.makeText(ResetPasswordActivity.this, "You're now ready to reset your password", Toast.LENGTH_SHORT).show();
                                        mDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPasswordActivity.this);

                                        builder.setTitle("Reset Password");

                                        final EditText newPassword = new EditText(ResetPasswordActivity.this);

                                        newPassword.setHint("Enter new password.");

                                        builder.setView(newPassword);

                                        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String newPass = newPassword.getText().toString().trim();

                                                if (!newPass.isEmpty()) {
                                                    mRef.child("password").setValue(newPass)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Toast.makeText(ResetPasswordActivity.this, "New Password set successfully", Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    } else {
                                                                        Toast.makeText(ResetPasswordActivity.this, "Error occurred", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            String error = e.getMessage();
                                                            Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_LONG).show();

                                                        }
                                                    });
                                                } else {
                                                    newPassword.setError("This field is required");
                                                    newPassword.requestFocus();
                                                }
                                            }
                                        });

                                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i)
                                            {
                                                dialogInterface.cancel();
                                            }
                                        });

                                        builder.show();
                                    }
                                    else {
                                        Toast.makeText(ResetPasswordActivity.this, "Your second answer is incorrect", Toast.LENGTH_LONG).show();
                                        mDialog.dismiss();
                                    }
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "Your first answer is incorrect", Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();
                                }
                            } else {
                                Toast.makeText(ResetPasswordActivity.this, "Security Questions Answers not set", Toast.LENGTH_LONG).show();
                                mDialog.dismiss();
                            }
                        } else {
                            Toast.makeText(ResetPasswordActivity.this, "Provide accurate information", Toast.LENGTH_LONG).show();
                            mDialog.dismiss();
                        }
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "User with this phone number " + phone + " does not exist", Toast.LENGTH_LONG).show();
                        mDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//        }
//        else
//        {
//            Toast.makeText(this, "Complete the form", Toast.LENGTH_SHORT).show();
//            mDialog.dismiss();
//        }
    }


    private void verifyInputs()
    {
        mDialog.setMessage("Please wait");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String answer1 = question1.getText().toString().toLowerCase();
        String answer2 = question2.getText().toString().toLowerCase();

        if (!answer1.isEmpty())
        {
            if (!answer2.isEmpty())
            {
                saveAnswers(answer1,answer2);
            }
            else
            {
                question2.setError("Please Provide an answer");
                question2.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            question1.setError("Please Provide an answer");
            question1.requestFocus();
            mDialog.dismiss();
        }
    }


    private void saveAnswers(String answer1, String answer2)
    {

        DatabaseReference usersRef;
        usersRef = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(Prevalent.currentOnlineUser.getPhoneNumber());

        HashMap<String, Object> questionsMap = new HashMap<>();
        questionsMap.put("answerOne",answer1);
        questionsMap.put("answerTwo",answer2);

        usersRef.child("Security Questions")
                .updateChildren(questionsMap)
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        String error = e.getMessage();
                        Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_LONG).show();
                        mDialog.dismiss();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(ResetPasswordActivity.this, "Answers stored successfully", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    mDialog.dismiss();
                }
            }
        });
    }


    private void displayPreviousAnswers()
    {
        DatabaseReference userRef;
        userRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(Prevalent.currentOnlineUser.getPhoneNumber()).child("Security Questions");

        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String answer1 = dataSnapshot.child("answerOne").getValue().toString();
                    String answer2 = dataSnapshot.child("answerTwo").getValue().toString();

                    question1.setText(answer1);
                    question2.setText(answer2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

}
