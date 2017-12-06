package com.yso.charp.mannager;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yso.charp.Interface.SignOutListener;
import com.yso.charp.CharpApplication;
import com.yso.charp.model.User;
import com.yso.charp.utils.ContactsUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 30-Nov-17.
 */

public class FireBaseManager {
    public static final String FB_CHILD_USERS = "Users";
    public static final String FB_CHILD_CLIENT_USERS = "ClientUsers";
    public static final String FB_CHILD_MESSAGES = "Messages";
    public static final String FB_CHILD_MESSAGES_MESSAGE_TEXT = "messageText";
    public static final String FB_CHILD_MESSAGES_LAST_MESSAGE = "lastMessage";
    public static final String FB_CHILD_NOTIFICATION = "Notifications";
    public static final String FB_CHILD_NOTIFICATION_STATUS = "status";

    private static FirebaseAuth mFirebaseAuth;
    private static FirebaseUser mFirebaseUser;
    private static DatabaseReference mDatabaseReferencem;
    private static String mFirebaseUserPhone;

    public static FirebaseAuth getFirebaseAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        return mFirebaseAuth;
    }

    public static FirebaseUser getFirebaseUser() {
        mFirebaseUser = getFirebaseAuth().getCurrentUser();
        return mFirebaseUser;
    }

    public static String getFirebaseUserPhone() {
//        String phone = getFirebaseUser().getPhoneNumber().replace(" ", "");
        mFirebaseUserPhone = getFirebaseUser().getPhoneNumber();
        return mFirebaseUserPhone;
    }

    public static DatabaseReference getDatabaseReferencem() {
        mDatabaseReferencem = FirebaseDatabase.getInstance().getReference();
        return mDatabaseReferencem;
    }

    public static void updateName(String name) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
        getFirebaseUser().updateProfile(profileUpdates);

        FirebaseDatabase.getInstance().getReference().child(FB_CHILD_USERS).child(getFirebaseUserPhone()).
                setValue(new User(name,
                        getFirebaseUserPhone(),
                        getFirebaseUser().getUid()));

    }

    public static void setUser() {
        getDatabaseReferencem().child(FB_CHILD_USERS).child(getFirebaseUserPhone()).
                setValue(new User(getFirebaseUser().getDisplayName(),
                        getFirebaseUserPhone(),
                        getFirebaseUser().getUid()));
    }

    public static void signOut(FragmentActivity activity, final SignOutListener signOutListener) {
        AuthUI.getInstance().signOut(activity).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                signOutListener.onComplete();
            }
        });
    }

    public static void loadChatMessages(String currentUser, String chatUser, ChildEventListener childEventListener) {
        getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(currentUser).child(chatUser).addChildEventListener(childEventListener);
    }

    public static void loadNotifications(ChildEventListener childEventListener) {
        getDatabaseReferencem().child(FB_CHILD_NOTIFICATION).child(getFirebaseUserPhone()).orderByChild(FB_CHILD_NOTIFICATION_STATUS).equalTo(0).addChildEventListener(childEventListener);
    }

    public static void loadChatList(ValueEventListener valueEventListener) {
        getDatabaseReferencem().child(FB_CHILD_MESSAGES).child(getFirebaseUserPhone()).addValueEventListener(valueEventListener);
    }

    public static void updateChildren(Map map, DatabaseReference.CompletionListener completionListener) {
        getDatabaseReferencem().updateChildren(map, completionListener);
    }

    public static void sendNotification(DatabaseReference databaseReference, Map<String, Object> childUpdates, DatabaseReference.CompletionListener completionListener) {
        databaseReference.updateChildren(childUpdates, completionListener);
    }

    public static void setFlagNotificationAsSent(FirebaseDatabase database, FirebaseAuth firebaseAuth, String notification_key) {
        database.getReference().child(FB_CHILD_NOTIFICATION)
                .child(getFirebaseUserPhone())
                .child(notification_key)
                .child(FB_CHILD_NOTIFICATION_STATUS)
                .setValue(1);
    }


    public static void loadClientUsers(ValueEventListener valueEventListener) {
        getDatabaseReferencem().child(FB_CHILD_CLIENT_USERS).child(getFirebaseUserPhone()).addValueEventListener(valueEventListener);
    }

    public static void updateClientUsers() {
        final HashMap<String, User> clientUsers = new HashMap<>();
//        final HashMap<String, User> allUsers = new HashMap<>();
        getDatabaseReferencem().child(FB_CHILD_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.getPhone().equals(getFirebaseUserPhone())) {
//                        allUsers.put(user.getPhone(), user);
                        if(ContactsUtils.getContactNumber(user.getPhone()) != null && !ContactsUtils.getContactNumber(user.getPhone()).equals(""))
                        {
                            clientUsers.put(user.getPhone(), user);
                        }
                    }
                }

//                ArrayList contacts = ContactsUtils.getAllContactPhoneNumbers(CharpApplication.getAppContext());
//
//                for (Object phone : contacts) {
//                    phone = phone.toString().replace(" ", "");
//                    if (phone.toString().startsWith("0")) {
//                        phone = "+972" + phone.toString().substring(1);
//                    }
//                    if (allUsers.get(phone) != null) {
//                        clientUsers.put((String) phone, allUsers.get(phone));
//                    }
//                }

                FirebaseDatabase.getInstance().getReference().child(FB_CHILD_CLIENT_USERS).child(getFirebaseUserPhone()).setValue(clientUsers);

                PersistenceManager.getInstance().setUsersMap(clientUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
