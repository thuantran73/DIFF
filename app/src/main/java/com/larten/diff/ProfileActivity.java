package com.larten.diff;

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
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class Profile_Activity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendCount;
    private Button mProfileSendreqBtn;
    private Button mDecLineBtn;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendRegDatabase;

    private DatabaseReference mFriendDatabase;

    private FirebaseUser mCurrent_user;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRegDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_reg");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView)findViewById(R.id.profile_image_view);
        mProfileName = (TextView)findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView)findViewById(R.id.profile_status);
        mProfileFriendCount = (TextView)findViewById(R.id.profile_friend_counting);
        mProfileSendreqBtn = (Button)findViewById(R.id.profile_send_reg_btn);
        mDecLineBtn = (Button)findViewById(R.id.profile_deline_friend_btn);


        mCurrent_state = "not_friends";

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(Profile_Activity.this).load(image).placeholder(R.drawable.df_ninja).into(mProfileImage);

                //........................ FRIEND LIST / REQUEST FEATURE ...................

                mFriendRegDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(user_id)){
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                mCurrent_state = "req_received";
                                mProfileSendreqBtn.setText("ACCEPT FRIEND REQUEST");

                                mDecLineBtn.setVisibility(View.VISIBLE);
                                mDecLineBtn.setEnabled(true);
                            }
                            else if(req_type.equals("sent")){
                                mCurrent_state = "req_sent";
                                mProfileSendreqBtn.setText("CANCEL FRIEND REQUEST");

                                mDecLineBtn.setVisibility(View.INVISIBLE);
                                mDecLineBtn.setEnabled(false);
                            }
                        }
                        else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendreqBtn.setText("UNFRIEND THIS PERSON");

                                        mDecLineBtn.setVisibility(View.INVISIBLE);
                                        mDecLineBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendreqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProfileSendreqBtn.setEnabled(false);

                // ................................ NOT FRIEND STATE................................

                if (mCurrent_state.equals("not_friends")){
                    mFriendRegDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mFriendRegDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {


                                        mCurrent_state = "req_sent";
                                        mProfileSendreqBtn.setText("Cancel Friend Request");

                                        mDecLineBtn.setVisibility(View.INVISIBLE);
                                        mDecLineBtn.setEnabled(false);

                                        Toast.makeText(Profile_Activity.this, "Successfull !", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                            else {
                                Toast.makeText(Profile_Activity.this, "Fail cmnr", Toast.LENGTH_LONG).show();
                            }
                            mProfileSendreqBtn.setEnabled(true);
                        }
                    });
                }

                // ................................ CANCEL REQUEST STATE................................


                if (mCurrent_state.equals("req_sent")){
                    mFriendRegDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRegDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendreqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendreqBtn.setText("SEND FRIEND REQUEST");

                                    mDecLineBtn.setVisibility(View.INVISIBLE);
                                    mDecLineBtn.setEnabled(false);
                                }
                            });
                        }
                    });
                }

                // .................................. REQ RECEIVE STATE ........................

                if (mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateInstance().format(new Date());
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRegDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRegDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendreqBtn.setEnabled(true);
                                                    mCurrent_state = "friends";
                                                    mProfileSendreqBtn.setText("UNFRIEND THIS PERSON");

                                                    mDecLineBtn.setVisibility(View.INVISIBLE);
                                                    mDecLineBtn.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
                //.........................................UNFRIEND .................................

                if (mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendreqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendreqBtn.setText("SEND FRIENDS REQUEST");

                                    mDecLineBtn.setVisibility(View.VISIBLE);
                                    mDecLineBtn.setEnabled(true);
                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
