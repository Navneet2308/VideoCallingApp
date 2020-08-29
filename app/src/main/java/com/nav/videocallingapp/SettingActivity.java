package com.nav.videocallingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

   private EditText userNameET,userBioET;
   private Button savebtn;
   private ImageView profileImageView;
   private  static int gallerypick=1;
   private Uri ImageUri;
   private  StorageReference userProfileImgRef;
   private String downloadurl;
   private DatabaseReference userREf;

   private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        userNameET=findViewById(R.id.username_setting);
        userBioET=findViewById(R.id.edit_status);
        savebtn=findViewById(R.id.save_setting_btn);
        profileImageView=findViewById(R.id.setting_profile_image);

        userREf= FirebaseDatabase.getInstance().getReference().child("user");
        userProfileImgRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        progressDialog=new ProgressDialog(this);

        retriveUserdata();

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent= new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallerypick);
            }
        });


        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savesuerdata();
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallerypick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            profileImageView.setImageURI(ImageUri);

        }
    }

    private void savesuerdata() {
        final  String getUserName =userNameET.getText().toString();
        final  String getbio=userBioET.getText().toString();

        if (ImageUri==null)
        {
            userREf.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image"))
                    {

                        saveInfoOnly();

                    }
                    else
                    {
                        saveInfoOnly();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        else if(getUserName.equals(""))
        {
            Toast.makeText(this, "Please enter user name", Toast.LENGTH_SHORT).show();
        }

        else if(getbio.equals(""))
        {
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog.setTitle("Account Setting");
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            final StorageReference filePath =userProfileImgRef
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());


            final UploadTask uploadTask=filePath.putFile(ImageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                    }

                    downloadurl=filePath.getDownloadUrl().toString();

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {

                        downloadurl=task.getResult().toString();

                        HashMap<String,Object>profileMap =new HashMap<>();
                        profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name",getUserName);
                        profileMap.put("status",getbio);
                        profileMap.put("image",downloadurl);


                        userREf.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                           if (task.isSuccessful()) {

                               progressDialog.dismiss();
                               Intent intent=new Intent(SettingActivity.this,ContactsActivity.class);
                               startActivity(intent);
                               finish();

                               Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                           }

                           else
                           {
                               progressDialog.dismiss();
                               Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                           }
                            }
                        });

                    }
                }
            });
        }
    }

    private void saveInfoOnly() {

        final  String getUserName =userNameET.getText().toString();
        final  String getbio=userBioET.getText().toString();

       if(getUserName.equals(""))
        {
            Toast.makeText(this, "Please enter user name", Toast.LENGTH_SHORT).show();
        }

        else if(getbio.equals(""))
        {
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }

        else

       {

           progressDialog.setTitle("Account Setting");
           progressDialog.setMessage("Please wait...");
           progressDialog.show();

        HashMap<String,Object>profileMap =new HashMap<>();
        profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
        profileMap.put("name",getUserName);
        profileMap.put("status",getbio);

        userREf.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    progressDialog.dismiss();
                    Intent intent=new Intent(SettingActivity.this,ContactsActivity.class);
                    startActivity(intent);
                    finish();

                    Toast.makeText(SettingActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                }

                else
                {
                    progressDialog.dismiss();
                    Toast.makeText(SettingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }}

    private  void retriveUserdata()
    {
        userREf.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                 String imageDb=dataSnapshot.child("image").getValue().toString();
                    String nameDb=dataSnapshot.child("name").getValue().toString();
                    String bioDb=dataSnapshot.child("status").getValue().toString();

                    userNameET.setText(nameDb);
                    userBioET.setText(bioDb);
                 Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileImageView);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
