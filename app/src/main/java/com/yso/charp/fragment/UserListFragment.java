package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.adapter.UserListAdapter;
import com.yso.charp.R;
import com.yso.charp.model.User;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class UserListFragment extends Fragment implements UserListAdapter.ItemClickListener {

    private RecyclerView listOfUsers;
    private HashMap<String, User> userList = new HashMap<>();
    private UserListAdapter mAdapter;


    public UserListFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initAdapter(view);

        mAdapter.setClickListener(this);
        listOfUsers.setLayoutManager(new LinearLayoutManager(getActivity()));
        listOfUsers.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(listOfUsers.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        listOfUsers.addItemDecoration(dividerItemDecoration);

        displayChatList();
    }

    private void initAdapter(View view) {

        mAdapter = new UserListAdapter(getContext(), userList);
        listOfUsers = view.findViewById(R.id.list_of_users);

    }

    private void displayChatList() {
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (!user.getPhone().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
                        userList.put(user.getPhone(), user);
                    }
                }
                mAdapter.setItems(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onItemClick(String key) {
        Bundle bundle = new Bundle();
        bundle.putString("user_phone", key);

        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
    }
}