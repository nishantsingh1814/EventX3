package com.eventx.eventx;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static android.icu.lang.UCharacter.JoiningGroup.E;

public class ChatActivity extends AppCompatActivity {

    private EditText messageET;
    private Button sendBtn;
    private String mPostKey;
    private Query query;

    RecyclerView mChatRecycler;

    DatabaseReference mDatabaseEvent;
    private ProgressBar mProgressBar;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mDatabaseEvent = FirebaseDatabase.getInstance().getReference().child("Event");

        mPostKey = getIntent().getStringExtra("event_id");
        mChatRecycler = (RecyclerView) findViewById(R.id.messageRecyclerView);

        layoutManager = new LinearLayoutManager(ChatActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mChatRecycler.setLayoutManager(layoutManager);
        messageET = (EditText) findViewById(R.id.messageEditText);
        sendBtn = (Button) findViewById(R.id.sendButton);
        query = mDatabaseEvent.child(mPostKey).child("chat");


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Chat chat=new Chat(messageET.getText().toString(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                DatabaseReference newPost = mDatabaseEvent.child(mPostKey).child("chat").push();
                newPost.child("message").setValue(messageET.getText().toString());
                newPost.child("username").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

                messageET.setText("");

            }
        });

        messageET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendBtn.setEnabled(true);
                } else {
                    sendBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        setUpViews();

        Log.i("jkloi", "onCreate: "+mPostKey);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setUpViews() {
        final FirebaseRecyclerAdapter<Chat, ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(Chat.class, R.layout.item_chat, ChatViewHolder.class, query) {
            @Override
            protected void populateViewHolder(ChatViewHolder viewHolder, Chat model, int position) {

                viewHolder.setName(model.username);
                viewHolder.setMessage(model.message);
            }
        };

        mChatRecycler.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView nameTv;
        TextView messageTv;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            nameTv = (TextView) mView.findViewById(R.id.messengerTextView);
            messageTv = (TextView) mView.findViewById(R.id.messageTextView);

        }

        public void setName(String username) {
            nameTv.setText(username);
        }

        public void setMessage(String message) {
            messageTv.setText(message);
        }
    }
}
