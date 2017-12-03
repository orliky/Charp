package com.yso.charp.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.yso.charp.MyApplication;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 30-Nov-17.
 */

public abstract class ContactsUtils
{
    public static String getContactName(final String phoneNumber)
    {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = MyApplication.getAppContext().getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

    public static ArrayList<String> getWhatsAppContacts()
    {
        Cursor c = MyApplication.getAppContext().getContentResolver().query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[] { ContactsContract.RawContacts.CONTACT_ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY },
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[] { "com.whatsapp" },
                null);

        ArrayList<String> myWhatsappContacts = new ArrayList<String>();
        int contactNameColumn = c.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
        while (c.moveToNext())
        {
            // You can also read RawContacts.CONTACT_ID to read the
            // ContactsContract.Contacts table or any of the other related ones.
            myWhatsappContacts.add(c.getString(contactNameColumn));
        }
        return myWhatsappContacts;
    }

    public static ArrayList<String> getAllContactPhoneNumbers(Context context) {
        String CONTACT_ID = ContactsContract.Contacts._ID;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

        ContentResolver cr = context.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
                null,
                null,
                null
        );
        if(pCur != null){
            if(pCur.getCount() > 0) {
                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();
                while (pCur.moveToNext()) {
                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
                    ArrayList<String> curPhones = new ArrayList<>();
                    if (phones.containsKey(contactId)) {
                        curPhones = phones.get(contactId);
                    }
                    curPhones.add(pCur.getString(pCur.getColumnIndex(PHONE_NUMBER)));
                    phones.put(contactId, curPhones);
                }
                Cursor cur = cr.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        new String[]{CONTACT_ID, HAS_PHONE_NUMBER},
                        HAS_PHONE_NUMBER + " > 0",
                        null,null);
                if (cur != null) {
                    if (cur.getCount() > 0) {
                        ArrayList<String> contacts = new ArrayList<>();
                        while (cur.moveToNext()) {
                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
                            if(phones.containsKey(id)) {
                                contacts.addAll(phones.get(id));
                            }
                        }
                        return contacts;
                    }
                    cur.close();
                }
            }
            pCur.close();
        }
        return null;
    }
}
