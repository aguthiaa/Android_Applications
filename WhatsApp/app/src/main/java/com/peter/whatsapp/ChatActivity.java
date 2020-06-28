package com.peter.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.peter.whatsapp.model.Messages;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView chatMessagesList;
    private EditText messageInput;
    private ImageButton sendMessageBtn, sendFileMessageBtn;
    private String receiverUserID, receiverUserName, receiverImage = "";
    private String messageSenderID;
    private String saveCurrentDate, saveCurrentTime;
    private String checker ="", myUrl ="";
    private static final int GalleryPick = 438;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private CircleImageView userProfileImage;
    private TextView rUsername, rLastSeen;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;

    private Uri fileURI;
    private StorageTask uploadTask;
    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUserID = getIntent().getExtras().get("UserID").toString();
        receiverUserName = getIntent().getExtras().get("UserName").toString();
        receiverImage = getIntent().getExtras().get("ProfileImage").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();


        mToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_app_bar, null);
        actionBar.setCustomView(actionBarView);

        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.chat_send_message_btn);
        sendFileMessageBtn = findViewById(R.id.chat_send_files_btn);
        userProfileImage = findViewById(R.id.chat_receiver_profile_image);
        rUsername = findViewById(R.id.chat_receiver_username);
        rLastSeen = findViewById(R.id.chat_receiver_last_seen);

        mDialog = new ProgressDialog(this);

        Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(userProfileImage);
        rUsername.setText(receiverUserName);
        rLastSeen.setText("Last Seen: Date, Time.");


        messagesAdapter = new MessagesAdapter(messageList);

        chatMessagesList = findViewById(R.id.chat_message_list);
        chatMessagesList.setLayoutManager(new LinearLayoutManager(this));
        chatMessagesList.setHasFixedSize(true);
        chatMessagesList.setAdapter(messagesAdapter);



//       messagesAdapter = new MessagesAdapter(messageList);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM-dd-yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());





        sendMessageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                verifyMessageInput();

            }
        });

        sendFileMessageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                selectFileTypeToSend();

            }
        });

    }



    private void selectFileTypeToSend()
    {
        CharSequence options[] = new CharSequence[]
                {
                        "Images",
                        "PDF Files",
                        "MS Word Files"
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Select File Type");

        builder.setItems(options, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

                if (i == 0)
                {
                    checker = "image";

                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent.createChooser(galleryIntent, "Select Image"),GalleryPick);
                }
                if (i == 1)
                {
                    checker = "pdf";

                    Intent pdfIntent = new Intent();
                    pdfIntent.setAction(Intent.ACTION_GET_CONTENT);
                    pdfIntent.setType("application/pdf");
                    startActivityForResult(pdfIntent.createChooser(pdfIntent, "Select PDF File"), GalleryPick);
                }
                if (i == 2)
                {
                    checker = "docx";

                    Intent docsIntent = new Intent();
                    docsIntent.setAction(Intent.ACTION_GET_CONTENT);
                    docsIntent.setType("application/msword");
                    startActivityForResult(docsIntent.createChooser(docsIntent, "Select MS Word File"), GalleryPick);

                }
                else
                {
                    dialogInterface.cancel();
                }
            }
        });

        builder.show();
    }



    private void verifyMessageInput()
    {
        String message = messageInput.getText().toString().trim();

        if (!message.isEmpty())
        {
            sendMessageNow(message);

        }
        else
        {
            messageInput.setError("Type message first");
            messageInput.requestFocus();
        }
    }

    private void sendMessageNow(String message)
    {
        String messageSenderRef = "Messages/" + messageSenderID + "/" + receiverUserID;
        String messagereceiverRef = "Messages/" + receiverUserID + "/" +messageSenderID;

        DatabaseReference userMessageKey = rootRef.child("Messages").child(messageSenderID).child(receiverUserID).push();
        String messagePushID = userMessageKey.getKey();

        Map messageMap = new HashMap();
        messageMap.put("message", message);
        messageMap.put("type", "text");
        messageMap.put("from", messageSenderID);
        messageMap.put("to", receiverUserID);
        messageMap.put("messageID", messagePushID);
        messageMap.put("date", saveCurrentDate);
        messageMap.put("time", saveCurrentTime);

        Map messageBodyDetailsMap = new HashMap();
        messageBodyDetailsMap.put(messageSenderRef+"/"+messagePushID, messageMap);
        messageBodyDetailsMap.put(messagereceiverRef + "/" + messagePushID, messageMap);


        rootRef.updateChildren(messageBodyDetailsMap)
                .addOnCompleteListener(new OnCompleteListener()
                {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this, "Messages sent successfully", Toast.LENGTH_SHORT).show();
                        }

                        messageInput.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                String error = e.getMessage();
                Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();

            }
        });



    }

    private void displayUserLastSeen()
    {
        rootRef.child("Users").child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child("User State").hasChild("state"))
                {
                    String stateType = dataSnapshot.child("User State").child("state").getValue().toString();
                    String date = dataSnapshot.child("User State").child("date").getValue().toString();
                    String time = dataSnapshot.child("User State").child("time").getValue().toString();

                    if (stateType.equals("online"))
                    {
                        rLastSeen.setText("online");
                    }
                    else if (stateType.equals("offline"))
                    {
                        rLastSeen.setText("Last Seen "+ date+" "+time);
                    }
                }
                else
                {
                    rLastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            mDialog.setMessage("Please wait...");
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();

            fileURI = data.getData();

            if (!checker.equals("image"))
            {
                StorageReference pdfFilesRef = FirebaseStorage.getInstance().getReference().child("PDF Files");

                final String messageSenderRef = "Messages/"+messageSenderID+"/"+receiverUserID;
                final String messageReceiverRef = "Messages/"+receiverUserID+"/"+messageSenderID;

                DatabaseReference userMessageKeyref = rootRef.child("Messages").child(messageSenderID).child(receiverUserID).push();

                final String messagePushKey = userMessageKeyref.getKey();

                final StorageReference pdfFilePath = pdfFilesRef.child(messagePushKey +"."+checker);

                UploadTask uploadTask2 = pdfFilePath.putFile(fileURI);

                uploadTask2.addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ChatActivity.this, "Error Message:\n"+e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(ChatActivity.this, "File sent successfully", Toast.LENGTH_SHORT).show();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
//                        System.out.println("Upload is " + progress + "% done");
                        mDialog.setMessage((int) progress+" % uploading...");
                    }
                });

                Task<Uri> uriTask = uploadTask2.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                    {

                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return pdfFilePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            String downloadURL = task.getResult().toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", downloadURL);
                            messageMap.put("name", fileURI.getLastPathSegment());
                            messageMap.put("type", checker);
                            messageMap.put("from", messageSenderID);
                            messageMap.put("to", receiverUserID);
                            messageMap.put("messageID", messagePushKey);
                            messageMap.put("date", saveCurrentDate);
                            messageMap.put("time", saveCurrentTime);

                            Map messageBodyDetailsMap = new HashMap();
                            messageBodyDetailsMap.put(messageSenderRef+"/"+messagePushKey, messageMap);
                            messageBodyDetailsMap.put(messageReceiverRef + "/" + messagePushKey, messageMap);


                            rootRef.updateChildren(messageBodyDetailsMap);

                            mDialog.dismiss();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(ChatActivity.this, "Error Message 2:\n"+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
//                .addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
//                        System.out.println("Upload is paused");
//                    }
//                });

            }
            else if (checker.equals("image"))
            {

                StorageReference imageFilesRef = FirebaseStorage.getInstance().getReference().child("Image Files");


                final String messageSenderRef = "Messages/"+messageSenderID+"/"+receiverUserID;
                final String messageReceiverRef = "Messages/"+receiverUserID+"/"+messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID).child(receiverUserID).push();
                final String messageImagePushKey = userMessageKeyRef.getKey();


                final StorageReference filepPath = imageFilesRef.child(messageImagePushKey +".jpg");

                uploadTask = filepPath.putFile(fileURI);
                uploadTask.continueWithTask(new Continuation()
                {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filepPath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task)
                    {
                        if (task.isSuccessful())
                        {
                            Uri downloadURL = task.getResult();
                            myUrl = downloadURL.toString();

                            Map messageMap = new HashMap();
                            messageMap.put("message", myUrl);
                            messageMap.put("name", fileURI.getLastPathSegment());
                            messageMap.put("type", checker);
                            messageMap.put("from", messageSenderID);
                            messageMap.put("to", receiverUserID);
                            messageMap.put("messageID", messageImagePushKey);
                            messageMap.put("date", saveCurrentDate);
                            messageMap.put("time", saveCurrentTime);

                            Map messageBodyDetailsMap = new HashMap();
                            messageBodyDetailsMap.put(messageSenderRef+"/"+messageImagePushKey, messageMap);
                            messageBodyDetailsMap.put(messageReceiverRef + "/" + messageImagePushKey, messageMap);


                            rootRef.updateChildren(messageBodyDetailsMap)
                                    .addOnCompleteListener(new OnCompleteListener()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(ChatActivity.this, "Image sent successfully", Toast.LENGTH_SHORT).show();
                                                mDialog.dismiss();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();
                                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                                    mDialog.dismiss();

                                }
                            });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {

                        String error = e.getMessage();
                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_LONG).show();
                        mDialog.dismiss();
                    }
                });

            }
            else
            {
                Toast.makeText(this, "Error: You didn't select any file", Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
            }



        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        displayUserLastSeen();

        rootRef.child("Messages").child(messageSenderID).child(receiverUserID)
                .addChildEventListener(new ChildEventListener()
                {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messageList.add(messages);

                        messagesAdapter.notifyDataSetChanged();

                        chatMessagesList.smoothScrollToPosition(chatMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }
}
