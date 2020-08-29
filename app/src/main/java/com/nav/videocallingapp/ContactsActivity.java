package com.nav.videocallingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    RecyclerView myContactslist;
    ImageView findPeopleBtn;

    private DatabaseReference contacsRef,userRef;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private String username="",userimage="";
    private String calledby="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
       navView= findViewById(R.id.nav_view);
        myContactslist=findViewById(R.id.contact_list);
       findPeopleBtn=findViewById(R.id.find_people_btn);


        myContactslist.setHasFixedSize(true);

        mAuth= FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        contacsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef= FirebaseDatabase.getInstance().getReference().child("user");


        myContactslist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findpeopleintent = new Intent(ContactsActivity.this, PeopleActivity.class);
                startActivity(findpeopleintent);
            }
        });

    }

    private  BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
           switch (menuItem.getItemId()) {

               case R.id.navigation_home:
                   Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                   startActivity(mainIntent);
                   break;


               case R.id.navigation_dashboard:
                   Intent dashobardIntent = new Intent(ContactsActivity.this, SettingActivity.class);
                   startActivity(dashobardIntent);
                   break;

               case R.id.navigation_notifications:
                   Intent notificationIntent = new Intent(ContactsActivity.this, NotificationActivity.class);
                   startActivity(notificationIntent);
                   break;

               case R.id.logout:
                   FirebaseAuth.getInstance().signOut();
                   Intent logoutIntent = new Intent(ContactsActivity.this, Registration.class);
                   startActivity(logoutIntent);
                   finish();
                   break;


           }
            return true;
            }
        };


    @Override
    protected void onStart() {
        super.onStart();

        checkForRecevingCall();
        validateUser();


        FirebaseRecyclerOptions<Contacts> option
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contacsRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder>firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(option) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts) {
                        final String listUserId=getRef(i).getKey();

                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists())
                                {
                                    username=dataSnapshot.child("name").getValue().toString();
                                    userimage=dataSnapshot.child("image").getValue().toString();


                                    holder.usernametext.setText(username);
                                    Picasso.get().load(userimage).into(holder.profileimage);


                                }

                                holder.callBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent callingIntent =new Intent(ContactsActivity.this,CallingActivity.class);
                                       callingIntent.putExtra("visit_user_id",listUserId);
                                        startActivity(callingIntent);
                                        finish();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                        ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        myContactslist.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }




    public static class  ContactsViewHolder extends RecyclerView.ViewHolder
    {

        TextView usernametext;
        Button callBtn;
        ImageView profileimage;



        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            usernametext=itemView.findViewById(R.id.name_contact);
            callBtn=itemView.findViewById(R.id.call_btn);
            profileimage=itemView.findViewById(R.id.image_contact);





        }
    }
    private void validateUser() {

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        reference.child("user").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists())
                        {
                            Intent settingIntent =new Intent(ContactsActivity.this,SettingActivity.class);
                            startActivity(settingIntent);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkForRecevingCall() {

        userRef.child(currentUserId)
                .child("Ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild("ringing"))
                        {
                            calledby=dataSnapshot.child("ringing").getValue().toString();
                            Intent callingIntent =new Intent(ContactsActivity.this,CallingActivity.class);
                            callingIntent.putExtra("visit_user_id",calledby);
                            startActivity(callingIntent);
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
