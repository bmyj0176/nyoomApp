package com.example.madproject.pages.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.madproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Alerts extends Fragment {
    View rootView;
    ImageView returnButton;
    TextView SETTINGS;
    private ListView alertsListView;
    private ArrayList<String> alertsList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private EditText addalerts;
    private TextView add, delete;
    private FirebaseFirestore db;
    private SharedPreferences emailpref;
    private ImageView ajaw;
    boolean isAdmin = false;
    int selectedItemPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_alerts, container, false);
        add = rootView.findViewById(R.id.AddBtn);
        add.setOnClickListener(view -> onAdd());
        delete = rootView.findViewById(R.id.DeleteBtn);
        delete.setOnClickListener(view -> onDel());
        addalerts = rootView.findViewById(R.id.Addalerts);
        addalerts.setOnFocusChangeListener(this::onTextboxFocus);
        returnButton = rootView.findViewById(R.id.ReturnButton6);
        returnButton.setOnClickListener(view -> goBack());
        SETTINGS = rootView.findViewById(R.id.SETTINGS2);
        alertsListView = rootView.findViewById(R.id.AlertsList);
        listAdapter = new AlertsAdapter(getContext(), alertsList);
        alertsListView.setAdapter(listAdapter);
        alertsListView.setOnItemClickListener(this::onListItemClick);
        // manage theme
        manageTheme();

        db = FirebaseFirestore.getInstance();
        ajaw = rootView.findViewById(R.id.ajaw);

        checkEmail(); // Call function to check email
        getAnnouncements();
        return rootView;
    }

    public void manageTheme() {
        if (ThemeManager.isDarkTheme()) {
            rootView.setBackgroundColor(getResources().getColor(R.color.mainBackground));
            returnButton.setImageTintList(getResources().getColorStateList(R.color.hintGray));
            SETTINGS.setTextColor(getResources().getColor(R.color.hintGray));
            addalerts.setBackgroundTintList(getResources().getColorStateList(R.color.backgroundPanel));
            addalerts.setHintTextColor(getResources().getColor(R.color.hintGray));
        } else { // light
            rootView.setBackgroundColor(getResources().getColor(R.color.LhintGray));
            returnButton.setImageTintList(getResources().getColorStateList(R.color.LdarkGray));
            SETTINGS.setTextColor(getResources().getColor(R.color.LdarkGray));
            addalerts.setBackgroundTintList(getResources().getColorStateList(R.color.LbackgroundPanel));
            addalerts.setHintTextColor(getResources().getColor(R.color.LhintGray));
        }
    }

    private void restartActivity() { // restart itself
        FragmentTransaction transaction = getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new Alerts());
        transaction.commit();
    }

    private void goBack() {
        getParentFragmentManager().popBackStack();
    }

    void getAnnouncements() {
        db.collection("announcements")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String message = document.getString("message");
                                alertsList.add(message);
                                listAdapter.notifyDataSetChanged();
                                addalerts.setText("");
                            }
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("readAnnouncementsCount", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("readAnnouncementsCount", task.getResult().size()).apply();
                        } else {
                            Log.w("Firestore", "Error getting documents.", task.getException());
                            alertsList.add("Failed to load announcements.");
                        }
                    }
                });
    }

    private void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isAdmin) {
            addalerts.clearFocus();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                child.setBackgroundColor(getResources().getColor(R.color.transparent));
            }
            if (ThemeManager.isDarkTheme()) {
                view.setBackgroundColor(getResources().getColor(R.color.darkGray));
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.LdarkGray));
            }
            delete.setVisibility(View.VISIBLE);
            selectedItemPosition = position;
        }
    }

    private void onTextboxFocus(View view, boolean hasFocus) {
        if (hasFocus) {
            add.setVisibility(View.VISIBLE);
            delete.setVisibility(View.GONE);
        } else {
            add.setVisibility(View.GONE);
            delete.setVisibility(View.VISIBLE);
        }
    }

    private void checkEmail() {
        emailpref = getActivity().getSharedPreferences("Emailpref", Context.MODE_PRIVATE);
        String userEmail = emailpref.getString("email", "default@gmail.com"); // Default if not found
        Log.d("email",userEmail);
        if (userEmail.equals("nyoom123@gmail.com")) {
            isAdmin = true;
            addalerts.setVisibility(View.VISIBLE);
            ajaw.setVisibility(View.VISIBLE);
        } else {
            isAdmin = false;
            addalerts.setVisibility(View.GONE);
            ajaw.setVisibility(View.GONE);
        }
    }



    private void onAdd() {
        String alerts = addalerts.getText().toString();
        if (alerts.isEmpty()) {
            Toast.makeText(getContext(), "Text cannot be empty!", Toast.LENGTH_SHORT).show();
            return; // Stop if the text is empty
        }

        Map<String, Object> announcement = new HashMap<>();
        announcement.put("message", alerts);
        announcement.put("timestamp", System.currentTimeMillis());

        db.collection("announcements")
                .add(announcement) // Auto-generates a document ID
                .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
                        restartActivity();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to save!", Toast.LENGTH_SHORT).show());

    }



    private void onDel() {
        String announcementText = alertsList.get(selectedItemPosition);
        alertsList.remove(selectedItemPosition);
        listAdapter.notifyDataSetChanged();

        // 5️⃣ Find the document that matches the text
        db.collection("announcements")
                .whereEqualTo("message", announcementText) // Match by text
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            db.collection("announcements")
                                    .document(doc.getId()) // Get the document ID
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(), "Deleted successfully!", Toast.LENGTH_SHORT).show();
                                        restartActivity();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(getContext(), "Failed to delete!", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(getContext(), "No matching document found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to find document!", Toast.LENGTH_SHORT).show());

    }
    public class AlertsAdapter extends ArrayAdapter<String> {
        private Context context;
        private List<String> alertsList;

        public AlertsAdapter(Context context, List<String> alertsList) {
            super(context, R.layout.alerts_list_item, alertsList);
            this.context = context;
            this.alertsList = alertsList;
        }

        TextView alertTextView;
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            // Inflate the custom layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View listItem = inflater.inflate(R.layout.alerts_list_item, parent, false);

            alertTextView = listItem.findViewById(R.id.AlertsText);
            alertTextView.setText(alertsList.get(position));
            // manage theme
            manageThemeLV();

            return listItem;
        }
        private void manageThemeLV() {
            if (ThemeManager.isDarkTheme()) {
                alertTextView.setTextColor(getResources().getColor(R.color.white));
            } else { // light
                alertTextView.setTextColor(getResources().getColor(R.color.black));
            }
        }
    }

}
