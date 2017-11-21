package com.yso.charp;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
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
public class UserListFragment extends Fragment
{
    private ListView listOfUsers;
    private FirebaseListAdapter<User> adapter;

    public UserListFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        listOfUsers = view.findViewById(R.id.list_of_users);

        displayUserList();
    }

    private void displayUserList()
    {
        final String[] key = new String[1];
        adapter = new FirebaseListAdapter<User>(getActivity(), User.class, R.layout.user, FirebaseDatabase.getInstance().getReference().child("Users"))
        {
            @Override
            protected User parseSnapshot(DataSnapshot snapshot)
            {
                key[0] = snapshot.getKey();
                return super.parseSnapshot(snapshot);
            }

            @Override
            protected void populateView(View v, final User model, final int position)
            {
                if (!model.getUID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                {
                    TextView name = v.findViewById(R.id.user_name);
                    TextView email = v.findViewById(R.id.user_email);
                    //                TextView signedIn = (TextView) v.findViewById(R.id.user_signed_in);

                    // Set their text
                    name.setText(model.getName());
                    email.setText(model.getEmail());

                    //                // Format the date before showing it
                    //                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
                    v.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Bundle bundle = new Bundle();
                            bundle.putString("user_id", getRef(position).getKey()); // Put anything what you want

                            ChatFragment chatFragment = new ChatFragment();
                            chatFragment.setArguments(bundle);
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, chatFragment).commit();
                        }
                    });
                }
                else
                {
                    TextView name = v.findViewById(R.id.user_name);
                    name.setText("You");
                }
            }

            @Override
            protected void onDataChanged()
            {
                super.onDataChanged();
            }
        };
        listOfUsers.setAdapter(adapter);
    }
}
