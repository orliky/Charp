package com.yso.charp.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.HashMap;

import static com.yso.charp.mannager.FireBaseManager.FB_CHILD_CLIENT_USERS;
import static com.yso.charp.mannager.FireBaseManager.getDatabaseReferencem;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUser;
import static com.yso.charp.mannager.FireBaseManager.getFirebaseUserPhone;


/**
 * A simple {@link Fragment} subclass.
 */
@SuppressLint ("ValidFragment")
public class UserListFragment extends Fragment implements ChatItemClickListener
{

    private HashMap<String, User> userList = new HashMap<>();
    private UserListAdapter mAdapter;
    private TextView mNoUsersTitle;
    @SuppressLint ("StaticFieldLeak")
    private static UserListFragment mInstance;
    private UserRepo mUserRepo;
    private ValueEventListener mValueEventListener;

    public UserListFragment()
    {
    }

    public static UserListFragment getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new UserListFragment();
        }
        return mInstance;
    }

    public ValueEventListener getValueEventListener()
    {
        return mValueEventListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mUserRepo = new UserRepo();

        userList = PersistenceManager.getInstance().getUsersMap();

        mAdapter = new UserListAdapter(getContext(), userList);
        RecyclerView recyclerView = view.findViewById(R.id.list_of_users);

        mAdapter.setClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(getContext()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        mNoUsersTitle = view.findViewById(R.id.no_users_title);

        initValueEventListener();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadClientUsers();
        if (userList.size() == 0)
        {
            mNoUsersTitle.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoUsersTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (getFirebaseUser() != null)
        {
            getDatabaseReferencem().child(FB_CHILD_CLIENT_USERS).child(getFirebaseUserPhone()).removeEventListener(mValueEventListener);
        }
    }

    private void loadClientUsers()
    {
        FireBaseManager.loadClientUsers(mValueEventListener);
    }

    private void initValueEventListener()
    {
        mValueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    userList.put(user.getPhone(), user);
                    if (mUserRepo.getByPhone(user.getPhone()) == null)
                    {
                        mUserRepo.insert(user);
                    }
                }
                PersistenceManager.getInstance().setUsersMap(userList);
                mAdapter.setItems(userList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        };
    }

    @Override
    public void onItemClick(String key)
    {
        android.support.v4.app.FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.chat_container, ChatFragment.getInstance(key)).commit();

        ((MainActivity) getActivity()).goToFragment(ChatFragment.getInstance(key), "");
    }

    public void refreshAdapter()
    {
        if (isAdded())
        {
            loadClientUsers();
        }
    }
}