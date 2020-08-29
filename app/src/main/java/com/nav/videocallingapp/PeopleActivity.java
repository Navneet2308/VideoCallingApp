package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class PeopleActivity extends AppCompatActivity {

   private RecyclerView findFriendList;
   private EditText searchET;
   private  String str="";
   private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        findFriendList=findViewById(R.id.find_firend_list);
        searchET=findViewById(R.id.search_user_text);

        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        usersRef= FirebaseDatabase.getInstance().getReference().child("user");

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                if (searchET.getText().toString().equals(""))
                {
                    Toast.makeText(PeopleActivity.this, "Please write name to search", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    str=charSequence.toString();
                    onStart();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });




    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=null;

        if (str.equals(""))
        {
            options= new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(usersRef,Contacts.class)
                    .build();
        }
        else {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(usersRef.orderByChild("name").startAt(str).endAt(str +"\uf8ff"),
                            Contacts.class)
                    .build();
            FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>firebaseRecyclerAdapter
                    =new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull final Contacts model) {

                    holder.usernametext.setText(model.getName());
                    Picasso.get().load(model.getImage()).into(holder.profileimage);

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String visit_user_id=getRef(position).getKey();


                            Intent intent=new Intent(PeopleActivity.this,ProfileActivity.class);
                            intent.putExtra("visit_user_id",visit_user_id);
                            intent.putExtra("visit_user_image",model.getImage());
                            intent.putExtra("visit_user_name",model.getName());
                            startActivity(intent);
                        }
                    });

                }

                @NonNull
                @Override
                public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
                    View view = LayoutInflater.from(p.getContext()).inflate(R.layout.find_friend_design, p, false);
                    FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                    return viewHolder;

                }
            };

            findFriendList.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
        }
    }

    public static class  FindFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView usernametext;
        Button videocallbtn;
        ImageView profileimage;
        RelativeLayout cardview;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            usernametext=itemView.findViewById(R.id.name_notification);
          //  videocallbtn=itemView.findViewById(R.id.call_btn);
            profileimage=itemView.findViewById(R.id.image_notification);
            cardview=itemView.findViewById(R.id.cardview1);

         //   videocallbtn.setVisibility(View.GONE);

        }
    }

}


