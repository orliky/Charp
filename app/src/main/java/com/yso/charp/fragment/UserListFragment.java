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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.ChatItemClickListener;
import com.yso.charp.R;
import com.yso.charp.activity.MainActivity;
import com.yso.charp.adapter.UserListAdapter;
import com.yso.charp.mannager.FireBaseManager;
import com.yso.charp.mannager.PersistenceManager;
import com.yso.charp.mannager.dataBase.UserRepo;
import com.yso.charp.model.User;
import com.yso.charp.utils.ContactsUtils;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint("ValidFragment")
public class UserListFragment extends Fragment implements ChatItemClickListener {

    private RecyclerView mRecyclerView;
    private HashMap<String, User> userList = new HashMap<>();
    private UserListAdapter mAdapter;
    @SuppressLint("StaticFieldLeak")
    private static UserListFragment mInstance;
    private UserRepo mUserRepo;

    public UserListFragment() {
    }

    public static UserListFragment getInstance() {
        if (mInstance == null) {
            mInstance = new UserListFragment();
        }
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserRepo = new UserRepo();

        userList = PersistenceManager.getInstance().getUsersMap();

        mAdapter = new UserListAdapter(getContext(), userList);
        mRecyclerView = view.findViewById(R.id.list_of_users);

        mAdapter.setClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        loadClientUsers();
    }

    private void loadClientUsers() {
        FireBaseManager.loadClientUsers(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    userList.put(user.getPhone(), user);
                    if (mUserRepo.getByPhone(user.getPhone()) == null) {
                        mUserRepo.insert(user);
                    }
                }
                PersistenceManager.getInstance().setUsersMap(userList);
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

        ((MainActivity) getActivity()).goToChatFragment(bundle);
//        getActivity().setTitle("Charp - " + ContactsUtils.getContactName(key));
    }
}