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
public class RequestsFragment extends Fragment
{
    private RecyclerView mReqList;
    private DatabaseReference mReqDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private View mMainView;
    private LinearLayoutManager mManager;


    public RequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mReqList = mMainView.findViewById(R.id.req_list);

        mManager = new LinearLayoutManager(this.getActivity());

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mReqDatabase.keepSynced(true);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(mManager);



        return mMainView;


    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, RequestsViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friends,RequestsViewHolder>
                (

                        Friends.class,
                        R.layout.req_layout,
                        RequestsViewHolder.class,
                        mReqDatabase
                )
        {

            @Override
            protected void populateViewHolder(final RequestsViewHolder viewHolder, final Friends friends, int i)
            {
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


                        viewHolder.mView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                profileIntent.putExtra("user_id",list_user_id);
                                startActivity(profileIntent);

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


        mReqList.setAdapter(friendsRecyclerViewAdapter);


    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public RequestsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name)
        {
            TextView userNameView = mView.findViewById(R.id.req_name);
            userNameView.setText(name);

        }
        public void setUserImage(String image)
        {
            CircleImageView userImageView = mView.findViewById(R.id.req_image);

            if(image.equals("default"))
            {
                Picasso.get().load(R.drawable.avatar).placeholder(R.drawable.avatar).into(userImageView);
            }
            else
            {
                Picasso.get().load(image).placeholder(R.drawable.avatar).into(userImageView);
            }

        }

    }
}
