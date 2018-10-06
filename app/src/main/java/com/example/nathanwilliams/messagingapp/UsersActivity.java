package com.example.nathanwilliams.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mToolbar = findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (
                        Users.class,
                        R.layout.users_single_layout,
                        UsersViewHolder.class,
                        mUsersDatabase
                )
        {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int i)
            {
                usersViewHolder.setName(users.getName());
                usersViewHolder.setBio(users.getStatus());
                usersViewHolder.setPic(users.getImage(), getApplicationContext());

                final String user_id = getRef(i).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);



                    }
                });

            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;


        public UsersViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name)
        {
            TextView userNameView = mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);


        }
        public void setBio(String bio)
        {
            TextView userBioView = mView.findViewById(R.id.users_single_status);
            userBioView.setText(bio);
        }

        public void setPic(String pic, Context ctx)
        {
            CircleImageView userImg = mView.findViewById(R.id.users_single_image);


            Picasso.get().load(pic).placeholder(R.drawable.avatar).into(userImg);

        }

    }
}
