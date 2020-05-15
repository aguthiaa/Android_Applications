package com.peter.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity
        implements Session.SessionListener,
        PublisherKit.PublisherListener
{

    private static String API_KEY ="46741232";
    private static String SESSION_ID ="1_MX40Njc0MTIzMn5-MTU4OTU3NDM4MDM0Nn5XSk5zVlBWMnEvaTlnVmlFd0pNem1EYmh-fg";
    private static String TOKEN ="T1==cGFydG5lcl9pZD00Njc0MTIzMiZzaWc9ZTU2ZDJhODE1NTAwZDIwODllMTg0NDhkYzI5ZTQyOWZjOGQwNDVmMDpzZXNzaW9uX2lkPTFfTVg0ME5qYzBNVEl6TW41LU1UVTRPVFUzTkRNNE1ETTBObjVYU2s1elZsQldNbkV2YVRsblZtbEZkMHBOZW0xRVltaC1mZyZjcmVhdGVfdGltZT0xNTg5NTc0NDEwJm5vbmNlPTAuMzQ5NjIxNzE5OTkxNjM3OCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkyMTY2NDA5JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM =123;
    private static final int RC_VIDEO_APP_PERM =124;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String currentOnlineUser;

    private Session  mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private FrameLayout mSubscriberViewController, mPublisherViewController;
    private ImageView cancelVideoChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        initializeView();

        mAuth = FirebaseAuth.getInstance();
        currentOnlineUser = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        requestPermissions();


    }



    private void initializeView()
    {
        mSubscriberViewController = findViewById(R.id.subscriber_container);
        mPublisherViewController = findViewById(R.id.publisher_container);
        cancelVideoChatBtn = findViewById(R.id.cancel_video_chat_btn);

        cancelVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                usersRef.child(currentOnlineUser).child("Calling").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                        {
                            String receiversID = dataSnapshot.child("calling").getValue().toString();

                            usersRef.child(receiversID).child("Ringing").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                if (mPublisher != null)
                                                {
                                                    mPublisher.destroy();
                                                }
                                                if (mSubscriber != null)
                                                {
                                                    mSubscriber.destroy();
                                                }
                                                usersRef.child(currentOnlineUser).child("Calling").removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    if (mPublisher != null)
                                                                    {
                                                                        mPublisher.destroy();
                                                                    }
                                                                    if (mSubscriber != null)
                                                                    {
                                                                        mSubscriber.destroy();
                                                                    }
                                                                    Intent contactsIntent = new Intent(VideoChatActivity.this, RegisterActivity.class);
                                                                    contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    startActivity(contactsIntent);
                                                                    finish();
                                                                }

                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e)
                                                    {
                                                        String error = e.getMessage();
                                                        Toast.makeText(VideoChatActivity.this, error, Toast.LENGTH_LONG).show();

                                                    }
                                                });
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e)
                                {
                                    String error = e.getMessage();
                                    Toast.makeText(VideoChatActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                        else
                        {
                            if (mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if (mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }
                            Intent contactsIntent = new Intent(VideoChatActivity.this, RegisterActivity.class);
                            contactsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(contactsIntent);
                            finish();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms ={Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };

        if (EasyPermissions.hasPermissions(this, perms))
        {
            //1. Initialize and connect to the session

            mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);

        }
        else
        {
            EasyPermissions.requestPermissions(this, "To call, allow this App to access Camera and Record Audio", RC_VIDEO_APP_PERM,perms);
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session)
    {
        //2. Publishing a stream to the session.

        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView)

        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session)
    {
        Log.i(LOG_TAG,"Stream Disconnected");

    }

    @Override
    public void onStreamReceived(Session session, Stream stream)
    {
        //3. Subsrcibing to the streams that has been published.

        Log.i(LOG_TAG, "Stream received.");

        if (mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }

    }

    @Override
    public void onStreamDropped(Session session, Stream stream)
    {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }

    }

    @Override
    public void onError(Session session, OpentokError opentokError)
    {
        Log.i(LOG_TAG,"Stream Error");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
