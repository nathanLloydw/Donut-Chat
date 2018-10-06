package com.example.nathanwilliams.messagingapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    private RecyclerView mFriendsList;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private LinearLayoutManager mManager;


    public FriendsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

         // Inflate the layout for this fragment
         mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

         mFriendsList = mMainView.findViewById(R.id.friends_list);

         mManager = new LinearLayoutManager(this.getActivity());

         mAuth = FirebaseAuth.getInstance();

         mCurrent_user_id = mAuth.getCurrentUser().getUid();

         mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
         mFriendsDatabase.keepSynced(true);
         mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
         mUserDatabase.keepSynced(true);

         mFriendsList.setHasFixedSize(true);
         mFriendsList.setLayoutManager(mManager);



         return mMainView;


    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends,FriendsViewHolder>
                (

                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
                )
        {

            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends friends, int i)
            {

                viewHolder.setDate(friends.getDate());

                final String list_user_id = getRef(i).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        viewHolder.setName(userName);
                        viewHolder.setUserImage(userThumb);

                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline =  dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }

                        viewHolder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {

                                //click event for whole recycle view
                                CharSequence options [] = new CharSequence[]{"Open profile","Send message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle("select options");
                                builder.setItems(options, new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i)
                                    {

                                        //click event for each item
                                        if(i == 0)
                                        {
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id",list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        else if(i == 1)
                                        {
                                            Intent ChatIntent = new Intent(getContext(),ChatActivity.class);
                                            ChatIntent.putExtra("user_id",list_user_id);
                                            ChatIntent.putExtra("user_name",userName);
                                            startActivity(ChatIntent);

                                        }

                                    }
                                });
                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

            }
        };


        mFriendsList.setAdapter(friendsRecyclerViewAdapter);


    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public FriendsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;

        }

        public void setDate(String date)
        {

            TextView userBioView = mView.findViewById(R.id.users_single_status);
            userBioView.setText(date);

        }
        public void setName(String name)
        {
            TextView userNameView = mView.findViewById(R.id.users_single_name);
            userNameView.setText(name);

        }
        public void setUserImage(String image)
        {
            CircleImageView userImageView = mView.findViewById(R.id.users_single_image);

            if(image.equals("default"))
            {
                Picasso.get().load(R.drawable.avatar).placeholder(R.drawable.avatar).into(userImageView);
            }
            else
            {
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImageView);
            }

        }

        public void setUserOnline(String online_status)
        {
            ImageView userOnline = mView.findViewById(R.id.user_online_icon);

            if(online_status.equals("true"))
            {
                userOnline.setVisibility(View.VISIBLE);
            }
            else
            {
                userOnline.setVisibility(View.INVISIBLE);
            }

        }
    }
}
