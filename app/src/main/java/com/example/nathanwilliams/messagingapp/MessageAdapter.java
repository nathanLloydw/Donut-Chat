package com.example.nathanwilliams.messagingapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase,mChatUserDatabase;
    private FirebaseUser mCurrentUser;



    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);

    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView mName, mTime;



        public MessageViewHolder(View view)
        {
            super(view);



            messageText = view.findViewById(R.id.message_text_layout);
            profileImage = view.findViewById(R.id.message_profile_layout);
            mName = view.findViewById(R.id.name_text_layout);
            mTime = view.findViewById(R.id.time_text_layout);


        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.MessageViewHolder viewHolder, int i)
    {
        mAuth = FirebaseAuth.getInstance();

        final String current_user_id = mAuth.getCurrentUser().getUid();

        Messages c = mMessageList.get(i);

        final String from_user = c.getFrom();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mChatUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mChatUserDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        if(from_user.equals(current_user_id)) {
            mUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot1) {
                    String name = dataSnapshot1.child("name").getValue().toString();
                    String thumb_image = dataSnapshot1.child("thumb_image").getValue().toString();


                    viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                    viewHolder.messageText.setTextColor(Color.WHITE);
                    viewHolder.mName.setText(name);

                    if (!thumb_image.equals("default")) {
                        Picasso.get().load(thumb_image).placeholder(R.drawable.avatar).into(viewHolder.profileImage);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        else if (!from_user.equals(current_user_id))
        {
            mChatUserDatabase.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot2)
                {


                        String chatUserName = dataSnapshot2.child("name").getValue().toString();
                        String ChatUserThumb_image = dataSnapshot2.child("thumb_image").getValue().toString();

                        viewHolder.messageText.setBackgroundColor(Color.WHITE);
                        viewHolder.messageText.setTextColor(Color.BLACK);
                        viewHolder.mName.setText(chatUserName);

                        if(!ChatUserThumb_image.equals("default"))
                        {
                            Picasso.get().load(ChatUserThumb_image).placeholder(R.drawable.avatar).into(viewHolder.profileImage);
                        }


                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }




        viewHolder.messageText.setText(c.getMessage());

    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }
}

