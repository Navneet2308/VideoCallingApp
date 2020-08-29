package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ProfileActivity extends AppCompatActivity {

    public String recevierUserid="",recevierUsername="",recevierUserimage="";
    private ImageView profile_back;
    private TextView name_profile;
    Button accept,cancel;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState="new";
    private DatabaseReference firendRequestRef,contacsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile_back=findViewById(R.id.background_profile_view);
        name_profile=findViewById(R.id.name_profile);
        accept=findViewById(R.id.add_firend);
        cancel=findViewById(R.id.descline_firend);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        firendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contacsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        recevierUserid=getIntent().getExtras().get("visit_user_id").toString();
        recevierUsername=getIntent().getExtras().get("visit_user_name").toString();
        recevierUserimage=getIntent().getExtras().get("visit_user_image").toString();


        Picasso.get().load(recevierUserimage).into(profile_back);
        name_profile.setText(recevierUsername);



        manageClickEvents();


    }

    private void manageClickEvents() {

        firendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(recevierUserid))
                        {
                            String requestType = dataSnapshot.child(recevierUserid)
                                    .child("request_type").getValue().toString();

                            if (requestType.equals("sent"))
                            {
                                currentState="request_sent";
                                accept.setText("Cancel Friend Request");

                            }

                            else if (requestType.equals("received"))
                            {
                                currentState="request_received";
                                accept.setText("Accept Friend Request");

                                cancel.setVisibility(View.VISIBLE);
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelFriendREquest();
                                    }
                                });

                            }
                        }

                        else
                        {
                            contacsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(recevierUserid))
                                            {
                                                currentState="friends";
                                                accept.setText("Delete Contact");
                                            }

                                            else
                                            {
                                                currentState="new";
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(senderUserId.equals(recevierUserid))
        {
            accept.setVisibility(View.GONE);

        }
        else
        {
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (currentState.equals("new"))
                    {
                        SentFriendRequest();
                    }
                     if(currentState.equals("request_sent"))
                    {

                        CancelFriendREquest();

                    }
                     if(currentState.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                     if(currentState.equals("request_sent"))
                    {
                        CancelFriendREquest();

                    }


                }
            });
        }
    }

    private void AcceptFriendRequest() {

        contacsRef.child(senderUserId).child(recevierUserid)
                .child("Contact").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {

                            contacsRef.child(recevierUserid).child(senderUserId)
                                    .child("Contact").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                firendRequestRef.child(senderUserId).child(recevierUserid)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    firendRequestRef.child(recevierUserid).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        currentState="friends";
                                                                                        accept.setText("Delete Contact");
                                                                                        cancel.setVisibility(View.VISIBLE);


                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });

                                            }
                                        }
                                    });

                        }
                    }
                });
    }

    private void CancelFriendREquest() {

        firendRequestRef.child(senderUserId).child(recevierUserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                   if (task.isSuccessful())
                   {
                       firendRequestRef.child(recevierUserid).child(senderUserId)
                               .removeValue()
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if (task.isSuccessful())
                                       {
                                           currentState="new";
                                           accept.setText("Add Friend");

                                       }

                                   }
                               });
                   }

                    }
                });
    }

    private void SentFriendRequest() {

        firendRequestRef.child(senderUserId).child(recevierUserid)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firendRequestRef.child(recevierUserid).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                currentState="request_sent";
                                                accept.setText("Cancel Friend Request");
                                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}
