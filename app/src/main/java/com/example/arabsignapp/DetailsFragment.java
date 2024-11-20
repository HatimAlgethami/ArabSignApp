package com.example.arabsignapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    RecyclerView listView;
    DetailsAdapter adapter;
    SearchView searchView;
    ArrayList<Word> sentence;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String param1, String param2) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.details_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationBarView navigationBar_v = getActivity().findViewById(R.id.nav_view);
        navigationBar_v.getMenu().findItem(R.id.navigation_chat).setChecked(true);

        listView = getView().findViewById(R.id.detailslist);
        listView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        TextView tv_noData = getView().findViewById(R.id.detailsnodata);

        String sessionId = getActivity().getIntent().getStringExtra("itemId");
        String sessionName = getActivity().getIntent().getStringExtra("itemName");

        adapter = new DetailsAdapter(sessionId,this);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference reference = firestore.collection("history");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference ref = firestore.document("users/" + uid);
        reference.whereEqualTo("user_id", ref).whereEqualTo("name", sessionName)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (value != null) {
                        sentence = new ArrayList<>();

                        if (value.getDocuments().isEmpty()) {
                            adapter.setSentence(new ArrayList<>());
                            listView.setAdapter(adapter);
                            tv_noData.setText("لا يوجد بيانات");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                sentence = doc.toObject(Session.class).getSentence();
                            } catch (Exception e) {
                                sentence = null;
                            }
                        }

                        if (sentence == null || sentence.isEmpty()) {
                            adapter.setSentence(new ArrayList<>());
                            listView.setAdapter(adapter);
                            tv_noData.setText("لا يوجد بيانات");
                            return;
                        }

                        tv_noData.setText("");

                        try {
                            adapter.setSentence(sentence);
                            listView.setAdapter(adapter);
                        } catch (Exception t) {
                        }
                    }
                });


        searchView = getView().findViewById(R.id.searchdetails);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.isBlank()) {
                    adapter.setSentence(sentence);
                    listView.setAdapter(adapter);
                    return false;
                }
                searchResults(query);
                return false;
            }
        });

    }

    private void searchResults(String searchText) {
        if (sentence == null) {
            return;
        }
        ArrayList<Word> filteredSentence = new ArrayList<>();
        for (Word word : sentence) {
            if (word.getWord().contains(searchText) ||
                    String.valueOf(word.getAccuracy()).contains(searchText)) {
                filteredSentence.add(word);
            }
        }
        adapter.setSentence(filteredSentence);
        listView.setAdapter(adapter);
    }
}