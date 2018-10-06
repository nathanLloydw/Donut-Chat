package com.example.nathanwilliams.messagingapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity
{

    private TextView mDisplayID,mProfileStatus,mProfileFriends;
    private ImageView mProfileImage;
    private Button mSendRequest, mDeclineRequest;

    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    private ProgressDialog mProgressDialog;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayID = findViewById(R.id.profile_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mProfileFriends = findViewById(R.id.profile_friends);
        mProfileImage = findViewById(R.id.profile_image);
        mSendRequest = findViewById(R.id.profile_request_btn);
        mDeclineRequest = findViewById(R.id.profile_cancel_btn);

        mCurrent_state = "not_friends";


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading 0_0");
        mProgressDialog.setMessage("Please wait whilst we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDeclineRequest.setVisibility(View.INVISIBLE);
        mDeclineRequest.setEnabled(false);


        mUserDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String display_bio = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();

                mDisplayID.setText(display_name);
                mProfileStatus.setText(display_bio);
                Picasso.get().load(display_image).placeholder(R.drawable.avatar).into(mProfileImage);

                // friend list and request data

                mFriendRequestDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(user_id))
                        {
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received"))
                            {

                                mCurrent_state ="req_received";
                                mSendRequest.setText("Accept Friend Request");

                                mDeclineRequest.setVisibility(View.VISIBLE);
                                mDeclineRequest.setEnabled(true);
                            }
                            else if(request_type.equals("sent"))
                            {
                                mCurrent_state ="req_sent";
                                mSendRequest.setText("Cancel Friend Request");

                                mDeclineRequest.setVisibility(View.INVISIBLE);
                                mDeclineRequest.setEnabled(false);

                            }

                            mProgressDialog.dismiss();

                        }
                        else
                        {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.hasChild(user_id))
                                    {

                                        mCurrent_state ="friends";
                                        mSendRequest.setText("Un-Friend");

                                        mDeclineRequest.setVisibility(View.INVISIBLE);
                                        mDeclineRequest.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                    mProgressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                        mProgressDialog.dismiss();

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        mSendRequest.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mSendRequest.setEnabled(false);

                // not friends section -------------------------------->

                if(mCurrent_state.equals("not_friends"))
                {

                    DatabaseReference newNotificationref = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationref.getKey();

                    HashMap<String, String> notificationData =  new HashMap<>();
                    notificationData.put("from",mCurrent_user.getUid());
                    notificationData.put("type","request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId,notificationData);


                   mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {
                            if(databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this,"error is sending notification",Toast.LENGTH_SHORT).show();
                            }
                            mSendRequest.setEnabled(true);
                            mCurrent_state ="req_sent";
                            mSendRequest.setText("Cancel friend request");

                        }
                    });


                }
                // Cancel request starts here ------------------------------->

                if(mCurrent_state.equals("req_sent"))
                {
                    mFriendRequestDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            mFriendRequestDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {


                                    mCurrent_state ="not_friends";
                                    mSendRequest.setText("Send Friend Request");
                                    mSendRequest.setEnabled(true);

                                    mDeclineRequest.setVisibility(View.INVISIBLE);
                                    mDeclineRequest.setEnabled(false);
                                }
                            });

                        }
                    });
                }

                // request recived section -- accept friend request --------------------->

                if(mCurrent_state.equals("req_received"))
                {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date",currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date",currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id , null);
                    friendsMap.put("Friends_req/" + user_id + "/" + mCurrent_user.getUid() , null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener()
                    {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference)
                        {

                            mSendRequest.setEnabled(true);
                            mCurrent_state = "friends";
                            mSendRequest.setText("un-Friend");

                        }
                    });
                }

                // delete friend section -------------------------->

                if(mCurrent_state.equals("friends"))
                {
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {


                                    mSendRequest.setEnabled(true);
                                    mCurrent_state ="not_friends";
                                    mSendRequest.setText("Send Friend Request");
                                }
                            });

                        }
                    });
                }



            }


        });

    }
}
