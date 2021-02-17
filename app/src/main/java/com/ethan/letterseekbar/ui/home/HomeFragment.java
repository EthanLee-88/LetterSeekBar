package com.ethan.letterseekbar.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.ethan.letterseekbar.R;
import com.ethan.letterseekbar.ui.view.LettersSeekBar;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private LettersSeekBar mLettersSeekBar;
    private TextView textView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = root.findViewById(R.id.text_home);
        mLettersSeekBar = root.findViewById(R.id.my_letters_seek_bar);
        homeViewModel.getText().observe(getViewLifecycleOwner(), (String s) -> {
            textView.setText(s);
        });
        initRes();
        return root;
    }

    private void initRes(){
        mLettersSeekBar.setLetterChangeListener((String letters, boolean up) -> {
            Log.d("TAG", "letters=" + letters + "-up=" + up);
            if (up){
                homeViewModel.setmText(letters);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    textView.setVisibility(View.GONE);
                }, 1000);
            }else {
                homeViewModel.setmText(letters);
                textView.setVisibility(View.VISIBLE);
            }
        });
    }
}