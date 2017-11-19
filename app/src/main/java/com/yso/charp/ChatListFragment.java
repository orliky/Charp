package com.yso.charp;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment
{

    private ListView listOfChats;
    private FirebaseListAdapter<String> adapter;

    public ChatListFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        listOfChats = view.findViewById(R.id.list_of_chats);

        displayChatList();
    }

    private void displayChatList()
    {

        adapter = new FirebaseListAdapter<String>
                (getActivity(), String.class, android.R.layout.simple_list_item_1, FirebaseDatabase.getInstance().getReference().child("Chats") ) {

            @Override
            protected String parseSnapshot(DataSnapshot snapshot) {
                return snapshot.getKey();
            }

            @Override
            protected void populateView(View v, final String s, int position) {
                TextView text = (TextView)v.findViewById(android.R.id.text1);
                text.setText(s);

                v.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString("chat_with", s); // Put anything what you want

                        ChatFragment chatFragment = new ChatFragment();
                        chatFragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
                    }
                });
            }
        };

        /*adapter = new FirebaseListAdapter<Chat>(getActivity(), Chat.class, R.layout.chat, FirebaseDatabase.getInstance().getReference().child("Chats"))
        {
            @Override
            protected void populateView(View v, final Chat model, int position)
            {
                if (!model.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    TextView name = v.findViewById(R.id.chat_name);
                    name.setText(model.getName());

                    v.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Bundle bundle = new Bundle();
                            bundle.putString("chat_with", model.getUID()); // Put anything what you want

                            ChatFragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(bundle);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
                        }
                    });
                }
            }

            @Override
            protected void onDataChanged()
            {
                super.onDataChanged();
            }
        };*/
        listOfChats.setAdapter(adapter);
    }

}
