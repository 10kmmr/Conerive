package com.example.sage.mapsexample;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddFriendsActivity extends AppCompatActivity {
    private static final String TAG = "AddFriendsActivity";

    //View Variable
    public Button sendRequestBTN;
    public Button sendMultipleRequestsBTN;
    public ListView contactsListView;
    public EditText phoneNumberET;

    private FirebaseFirestore firestoreDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ContactsListAdapter contactsListAdapter;

    private String recieverPhone;
    ArrayList<ContactsListDataModel> arrayList_Android_Contacts;


    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        firestoreDB = FirebaseFirestore.getInstance();
        phoneNumberET = findViewById(R.id.contact_number);
        sendRequestBTN = findViewById(R.id.send_request);
        sendMultipleRequestsBTN = findViewById(R.id.send_requests);
        contactsListView = findViewById(R.id.contacts);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        requestQueue = Volley.newRequestQueue(this);

        fp_get_Android_Contacts();
        contactsListAdapter = new ContactsListAdapter(getApplicationContext(), R.layout.contacts_list_item, arrayList_Android_Contacts);
        contactsListView.setAdapter(contactsListAdapter);

        sendRequestBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recieverPhone = phoneNumberET.getText().toString();

                firestoreDB.collection("USERS").whereEqualTo("Phone", recieverPhone)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(queryDocumentSnapshots.size()>0) {

                                    final String recieverId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                    final String recieverName = queryDocumentSnapshots.getDocuments().get(0).getString("Name");

                                    firestoreDB.collection("USERS").document(currentUser.getUid())
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if (documentSnapshot.contains("Friends")) {
                                                        ArrayList friendIds = (ArrayList<String>) documentSnapshot.get("Friends");
                                                        if (friendIds.contains(recieverId)) {
                                                            Toast.makeText(AddFriendsActivity.this, recieverName + " is already your friend", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            sendFriendRequestToServer();
                                                        }
                                                    } else {
                                                        sendFriendRequestToServer();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFailure - 2: " + e);
                                                }
                                            });
                                } else {
                                    Toast.makeText(AddFriendsActivity.this, "This user does not have the application! ", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure - 1: " + e);
                    }
                });
            }
        });
    }

    void sendFriendRequestToServer(){
        String url = "https://conerive-fcm.herokuapp.com/sendrequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
                        startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("senderId", currentUser.getUid());
                params.put("phone", recieverPhone);
                return params;
            }
        };
        requestQueue.add(postRequest);
    }

    public void fp_get_Android_Contacts() {
        arrayList_Android_Contacts = new ArrayList<>();
        Cursor cursor_Android_Contacts = null;
        ContentResolver contentResolver = getContentResolver();
        try {
            cursor_Android_Contacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception ex) {
            Log.e("Error on contact", ex.getMessage());
        }
        Log.d(TAG, "numOfContacts: "+cursor_Android_Contacts.getCount());
        if (cursor_Android_Contacts.getCount() > 0) {
            while (cursor_Android_Contacts.moveToNext()) {
                String contact_id = cursor_Android_Contacts.getString(cursor_Android_Contacts.getColumnIndex(ContactsContract.Contacts._ID));
                String contact_display_name = cursor_Android_Contacts.getString(cursor_Android_Contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = "";
                int hasPhoneNumber = Integer.parseInt(cursor_Android_Contacts.getString(cursor_Android_Contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                            , null
                            , ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?"
                            , new String[]{contact_id}
                            , null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phoneCursor.close();
                }
                ContactsListDataModel android_contact = new ContactsListDataModel(contact_display_name, phoneNumber);

                if(android_contact.getPhone().length()>8)
                        arrayList_Android_Contacts.add(android_contact);
            }

        }
    }

    class ContactsListAdapter extends ArrayAdapter<ContactsListDataModel> {

        public ContactsListAdapter(Context context, int resource, ArrayList<ContactsListDataModel> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            Log.d(TAG, "accessed getView" + "position : " + position);
            if (view == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                view = vi.inflate(R.layout.contacts_list_item, null);
            }

            final ContactsListDataModel contactsListDataModel = getItem(position);

            if (contactsListDataModel != null) {

                TextView contactNameTV = view.findViewById(R.id.contact_name);
                TextView contactNumberTV = view.findViewById(R.id.contact_number);

                contactNameTV.setText(contactsListDataModel.getName());
                contactNumberTV.setText(contactsListDataModel.getPhone());

            }
            return view;
        }
    }

    public class ContactsListDataModel {
        String Name;
        String Phone;

        public ContactsListDataModel(String name, String phone) {
            this.Name = name;
            this.Phone = phone;
        }

        public String getName() {
            return Name;
        }

        public String getPhone() {
            return Phone;
        }

    }

}
