package com.example.arabsignapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements DeleteDialog{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    RecyclerView listView;
    HistoryAdapter adapter;
    SearchView searchView;
    ArrayList<String> sessionIdList;
    ArrayList<String> sessionNameList;
    CollectionReference reference;
    DocumentReference ref;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
        return inflater.inflate(R.layout.history_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = getView().findViewById(R.id.listView);
        listView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HistoryAdapter(this);

        TextView tv_noData = getView().findViewById(R.id.nodata);
        NavigationBarView navigationBar_v =getActivity().findViewById(R.id.nav_view);
        navigationBar_v.getMenu().findItem(R.id.navigation_chat).setChecked(true);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        reference = firestore.collection("history");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = firestore.document("users/"+uid);

        AppCompatButton deleteAll = getView().findViewById(R.id.btn_delete_all);
        deleteAll.setOnClickListener(v ->
                new ConfirmDeleteDialog("هل انت متأكد من مسح جميع المحادثات؟",
                        this,-1).show(getParentFragmentManager(),""));

        reference.whereEqualTo("user_id",ref).addSnapshotListener((value, error) -> {
            if (error!=null){
                return;
            }

            if (value!=null){
                sessionIdList = new ArrayList<>();
                sessionNameList = new ArrayList<>();

                if (value.getDocuments().isEmpty()){
                    adapter.setNames(new ArrayList<>());
                    adapter.setSessionIds(new ArrayList<>());
                    listView.setAdapter(adapter);
                    deleteAll.setVisibility(View.INVISIBLE);
                    tv_noData.setText("لا يوجد بيانات");
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    sessionIdList.add(doc.getId());
                    sessionNameList.add(String.valueOf(doc.get("name")));
                }

                deleteAll.setVisibility(View.VISIBLE);
                tv_noData.setText("");
                try {
                    adapter.setNames(sessionNameList);
                    adapter.setSessionIds(sessionIdList);
                    listView.setAdapter(adapter);
                }
                catch (Throwable t){
                    return;
                }
            }
        });

        searchView = getView().findViewById(R.id.searchhistory);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.isBlank()){
                    adapter.setNames(sessionNameList);
                    adapter.setSessionIds(sessionIdList);
                    listView.setAdapter(adapter);
                    return false;
                }
                searchResults(query);
                return false;
            }
        });

    }

    private void searchResults(String searchText){
        if (sessionNameList==null||sessionIdList==null){
            return;
        }
        ArrayList<String> filteredSessionNameList = new ArrayList<>();
        ArrayList<String> filteredSessionIdList = new ArrayList<>();
        for (int i=0;i<sessionNameList.size();i++){
            String name = sessionNameList.get(i);
            if (name.contains(searchText)){
                filteredSessionNameList.add(name);
                filteredSessionIdList.add(sessionIdList.get(i));
            }
        }
        adapter.setNames(filteredSessionNameList);
        adapter.setSessionIds(filteredSessionIdList);
        listView.setAdapter(adapter);
    }

    @Override
    public void deleteItem(int position) {
        reference.whereEqualTo("user_id",ref).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        doc.getReference().delete();
                    }
                }).addOnFailureListener(e -> {
                    return;
                });
    }
}