package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage,cancelCallBtn,makeCallBtn;
    private  String receiverUserId="",receiverUserImage="",receiverUserName="";
    private  String senderUserId="",senderUserImage="",senderUserName="",checker="",callingId="",ringingId="";
    private DatabaseReference userRef;

    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        nameContact=findViewById(R.id.name_calling);
        profileImage=findViewById(R.id.profile_image_calling);
        makeCallBtn=findViewById(R.id.make_call);
        cancelCallBtn=findViewById(R.id.cancel_call);

        senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        userRef= FirebaseDatabase.getInstance().getReference().child("user");


        mediaPlayer=MediaPlayer.create(this,R.raw.ringing);

        getAndSetUserIdInfo();

    cancelCallBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mediaPlayer.stop();
            checker="clicked";

            cancelCallingUser();
        }
    });

    makeCallBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.stop();
            final HashMap<String,Object>callingPickupMap=new HashMap<>();
            callingPickupMap.put("picked","picked");

            userRef.child(senderUserId).child("Ringing")
                    .updateChildren(callingPickupMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful())
                            {
                                Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                                startActivity(intent);
                            }

                        }
                    });
        }
    });

    }



    private void getAndSetUserIdInfo() {

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUserId).exists())
                {
                    receiverUserImage=dataSnapshot.child(receiverUserId).child("image").getValue().toString();

                    receiverUserName=dataSnapshot.child(receiverUserId).child("name").getValue().toString();


                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);



                }

                if (!dataSnapshot.child(receiverUserId).exists())
                {
                    Toast.makeText(CallingActivity.this, "not exts", Toast.LENGTH_SHORT).show();



                }

                if (dataSnapshot.child(senderUserId).exists())
                {
                    senderUserImage=dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName=dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();

        userRef.child(receiverUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing") )
                        {


                            final HashMap<String,Object> callingInfo = new HashMap<>();

                            callingInfo.put("calling",receiverUserId);


                            userRef.child(senderUserId).child("Calling")
                                    .updateChildren(callingInfo)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                final HashMap<String,Object> ringingInfo = new HashMap<>();

                                                ringingInfo.put("ringing",senderUserId);

                                                userRef.child(receiverUserId)
                                                        .child("Ringing")
                                                        .updateChildren(ringingInfo);

                                            }
                                        }
                                    });


                        }


                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))

                                {
                                    makeCallBtn.setVisibility(View.VISIBLE);
                                }

                                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked"));

                                mediaPlayer.stop();
                                Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void cancelCallingUser()
    {
        //from sender side
    userRef.child(senderUserId)
            .child("Calling")
    .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
            {
                callingId=dataSnapshot.child("calling").getValue().toString();

                userRef.child(callingId)
                        .child("Ringing")
                        .removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    userRef.child(senderUserId)
                                            .child("Calling")
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {

                                                    startActivity(new Intent(CallingActivity.this,Registration.class));
                                                     finish();

                                                }
                                            });

                                }
                            }
                        });
            }


            else
            {

                startActivity(new Intent(CallingActivity.this,Registration.class));
                finish();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    //from receiver side

        userRef.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                        {
                            ringingId=dataSnapshot.child("ringing").getValue().toString();

                            userRef.child(ringingId)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                userRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {

                                                                startActivity(new Intent(CallingActivity.this,Registration.class));
                                                                finish();

                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }


                        else
                        {

                            startActivity(new Intent(CallingActivity.this,Registration.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



    }
}

