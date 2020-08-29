package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.opentok.android.OpentokError;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Publisher;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener,
        PublisherKit.PublisherListener
{

    private static String API_Key="46859614";
    private static  String SESSION_ID="2_MX40Njg1OTYxNH5-MTU5NTg2NzQzMzMzOX5KZGtIT1pCZnE5b243cGJYd2QwR1VQd0h-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00Njg1OTYxNCZzaWc9M2U5MmU2OWIxNGYwNmY3Y2ExODJiZTRkMjY2NTZiOTZkMjY3ZmZlYTpzZXNzaW9uX2lkPTJfTVg0ME5qZzFPVFl4Tkg1LU1UVTVOVGcyTnpRek16TXpPWDVLWkd0SVQxcENabkU1YjI0M2NHSllkMlF3UjFWUWQwaC1mZyZjcmVhdGVfdGltZT0xNTk1ODY3NDg1Jm5vbmNlPTAuNzM5NjgyMzAzNzk5ODkzMiZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTk1ODg5MDg0JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM=124;

    private ImageView close_videochat_btn;
    private DatabaseReference userRef;
    private String userID="";
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubsciberViewController;
    private Session mSession;
    private Publisher mpublisher;
    private Subscriber mSubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("user");



        close_videochat_btn=findViewById(R.id.close_video_chat_btn);

        close_videochat_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            if (mpublisher !=null)
                            {
                                mpublisher.destroy();
                            }
                            if (mSubscriber !=null)
                            {
                                mSubscriber.destroy();
                            }
                            userRef.child(userID).child("Ringing").removeValue();
                            startActivity(new Intent(VideoChatActivity.this,Registration.class));
                            finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            userRef.child(userID).child("Calling").removeValue();

                            if (mpublisher !=null)
                            {
                                mpublisher.destroy();
                            }
                            if (mSubscriber !=null)
                            {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,Registration.class));
                            finish();
                        }

                        else
                        {
                            if (mpublisher !=null)
                            {
                                mpublisher.destroy();
                            }
                            if (mSubscriber !=null)
                            {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,Registration.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermission()
    {
        String[] perms={Manifest.permission.INTERNET,Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA};

        if (EasyPermissions.hasPermissions(this,perms))
        {
            mPublisherViewController=findViewById(R.id.publisher_container);
            mSubsciberViewController=findViewById(R.id.subscriber_container);

            mSession=new Session.Builder(this,API_Key,SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(this,"Hey this app need a Mic and Camera Permisson Please allow.",RC_VIDEO_APP_PERM
            ,perms);


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
//2.Publishing a stream to the session
    @Override
    public void onConnected(Session session) {

        Log.i(LOG_TAG,"Session Connected");



        mpublisher=new Publisher.Builder(this).build();
        mpublisher.setPublisherListener(VideoChatActivity.this);
        mPublisherViewController.addView(mpublisher.getView());

        if (mpublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView)mpublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mpublisher);

    }

    @Override
    public void onDisconnected(Session session) {

        Log.i(LOG_TAG,"Stream Disconnected");

    }

    //3. Subscribing to the streams
    @Override
    public void onStreamReceived(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Received");

        if (mSubscriber==null)
        {
            mSubscriber=new Subscriber.Builder(this,stream).build();
            mSession.subscribe(mSubscriber);
            mSubsciberViewController.addView(mSubscriber.getView());

        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

        Log.i(LOG_TAG,"Stream Dropped");

        if (mSubscriber!=null)
        {

            mSubscriber=null;
            mSubsciberViewController.removeAllViews();




        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

        Log.i(LOG_TAG,"Stream Received");

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}


