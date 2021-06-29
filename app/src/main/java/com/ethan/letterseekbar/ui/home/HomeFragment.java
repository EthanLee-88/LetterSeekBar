package com.ethan.letterseekbar.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethan.letterseekbar.R;
import com.ethan.letterseekbar.ui.view.LettersSeekBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private LettersSeekBar mLettersSeekBar;
    private RecyclerView nameList;
    private ArrayList<ShareContactsBean> mContactList;
    private  ContactsListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mLettersSeekBar = root.findViewById(R.id.my_letters_seek_bar);
        nameList = root.findViewById(R.id.name_list);

        mLayoutManager = new LinearLayoutManager(getActivity());
        nameList.setLayoutManager(mLayoutManager);
        fetchContactList();
        mAdapter = new ContactsListAdapter(LayoutInflater.from(getActivity()), mContactList);
        nameList.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        nameList.addItemDecoration(new FloatingBarItemDecoration(getActivity(), mHeaderList));
        nameList.setAdapter(mAdapter);

        initRes();
        mLettersSeekBar.setLetterChangeListener((String letter, boolean actionUp) -> {
            for (Integer position : mHeaderList.keySet()) {
                if (mHeaderList.get(position).equals(letter)) {
                    mLayoutManager.scrollToPositionWithOffset(position, 0);
                    return;
                }
            }
        });
        return root;
    }

    private final int PERMISSION_REQUEST_CODE_READ_CONTACTS = 71;
    private LinkedHashMap<Integer, String> mHeaderList;
    protected void fetchContactList() {
        mHeaderList = new LinkedHashMap<>();
        if (Utils.checkHasPermission(getActivity(), Manifest.permission.READ_CONTACTS, PERMISSION_REQUEST_CODE_READ_CONTACTS)) {
            mContactList = ContactsManager.getPhoneContacts(getActivity());
        } else {
            mContactList = new ArrayList<>(0);
        }
        preOperation();
    }

    private void preOperation() {
        mHeaderList.clear();
        if (mContactList.size() == 0) {
            return;
        }
        addHeaderToList(0, mContactList.get(0).getInitial());
        for (int i = 2; i < mContactList.size(); i++) {
            if (!mContactList.get(i - 1).getInitial().equalsIgnoreCase(mContactList.get(i).getInitial())) {
                addHeaderToList(i, mContactList.get(i).getInitial());
            }
        }
    }

    private void addHeaderToList(int index, String header) {
        mHeaderList.put(index, header);
    }

    private void initRes(){
//        startActivity(new Intent(getActivity() , ContactListActivity.class));
        mLettersSeekBar.setLetterChangeListener((String letters, boolean up) -> {
            Log.d("TAG", "letters=" + letters + "-up=" + up);
            if (up){
                homeViewModel.setmText(letters);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                }, 1000);
            }else {
                homeViewModel.setmText(letters);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE_READ_CONTACTS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchContactList();
                mAdapter.notifyDataSetChanged();
                Log.d("TAG" , "onRequestPermissionsResult s");
            } else {
                Snackbar.make(nameList, "Do not have enough permission", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}