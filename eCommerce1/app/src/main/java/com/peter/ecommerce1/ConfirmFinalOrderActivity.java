package com.peter.ecommerce1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.peter.ecommerce1.Prevalent.Prevalent;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ConfirmFinalOrderActivity extends AppCompatActivity
{
    private EditText sName,sPhoneNumber,sEmail, sAddress,sCity;
    private Button confirmOrder;

    private String totalAmount = "";
    private String saveCurrentDate, saveCurrentTime;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_final_order);

        initViews();

        totalAmount = getIntent().getStringExtra("Total Price");
    }

    private void initViews()
    {
        sName = (EditText) findViewById(R.id.shipment_name);
        sPhoneNumber = (EditText) findViewById(R.id.shipment_phone_number);
        sEmail = (EditText) findViewById(R.id.shipment_email);
        sAddress = (EditText) findViewById(R.id.shipment_home_address);
        sCity = (EditText) findViewById(R.id.shipment_city);
        confirmOrder = (Button) findViewById(R.id.confirm_final_order);

        mDialog = new ProgressDialog(this);

        confirmOrder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                verifyUserInputs();
            }
        });
    }


    private void verifyUserInputs()
    {
        mDialog.setTitle("Confirm Final Order");
        mDialog.setMessage("Please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        String name = sName.getText().toString().trim();
        String number = sPhoneNumber.getText().toString().trim();
        String email = sEmail.getText().toString().trim();
        String address = sAddress.getText().toString().trim();
        String city = sCity.getText().toString().trim();

        if (!name.isEmpty())
        {
            if (!number.isEmpty())
            {
                if (!email.isEmpty())
                {
                    if (!address.isEmpty())
                    {
                        if (!city.isEmpty())
                        {
                            confirmOrderNow(name,number,email,address,city);
                        }
                        else
                        {
                            sCity.setError("City is required");
                            sCity.requestFocus();
                            mDialog.dismiss();
                        }

                    }
                    else
                    {
                        sAddress.setError("Address is required");
                        sAddress.requestFocus();
                        mDialog.dismiss();
                    }

                }
                else
                {
                    sEmail.setError("Email is required");
                    sEmail.requestFocus();
                    mDialog.dismiss();
                }

            }
            else
            {
                sPhoneNumber.setError("Phone Number is required");
                sPhoneNumber.requestFocus();
                mDialog.dismiss();
            }
        }
        else
        {
            sName.setError("Full Name is required");
            sName.requestFocus();
            mDialog.dismiss();
        }
    }



    private void confirmOrderNow(String name, String number, String email, String address, String city)
    {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM-dd-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());


        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());

        final DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhoneNumber());

        HashMap<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("totalAmount",totalAmount);
        ordersMap.put("date",saveCurrentDate);
        ordersMap.put("time",saveCurrentTime);
        ordersMap.put("fullName",name);
        ordersMap.put("phoneNumber",number);
        ordersMap.put("email",email);
        ordersMap.put("address",address);
        ordersMap.put("city",city);
        ordersMap.put("state","not shipped");

        ordersRef.updateChildren(ordersMap)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Cart List")
                                    .child("User View")
                                    .child(Prevalent.currentOnlineUser.getPhoneNumber())
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {

                                                                if (task.isSuccessful())
                                                                {
                                                                    Toast.makeText(ConfirmFinalOrderActivity.this, "Your Order Has Been Placed Successfully", Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(ConfirmFinalOrderActivity.this, HomeActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                    startActivity(intent);
                                                                    finish();
                                                                    mDialog.dismiss();
                                                                }
                                                            }

                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();

                                    Toast.makeText(ConfirmFinalOrderActivity.this, error, Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();
                                }
                            });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e) {

                String error = e.getMessage();

                Toast.makeText(ConfirmFinalOrderActivity.this, error, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        });

    }


}
