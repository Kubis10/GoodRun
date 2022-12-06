package com.przybysz.runapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_home, null);
        TextView time = (TextView) root.findViewById(R.id.time_text);
        Button start = (Button) root.findViewById(R.id.start_btn);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapsFragment()).commit();
            }
        });
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        time.setText(formatter.format(date));
        final Handler handler = new Handler();
        Runnable refresh = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                Date date = new Date();
                time.setText(formatter.format(date));
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(refresh, 10000);
        return root;
    }

}