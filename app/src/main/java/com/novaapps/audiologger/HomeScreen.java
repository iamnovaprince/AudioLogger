package com.novaapps.audiologger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.novaapps.audiologger.fragments.FileFragment;
import com.novaapps.audiologger.fragments.RecorderFragment;

public class HomeScreen extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    LinearLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        init();

        RecorderFragment recorderFragment = new RecorderFragment();
        FileFragment fileFragment = new FileFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, recorderFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.recorderTab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, recorderFragment).commit();
                        return true;

                    case R.id.filesTab:
                        getSupportFragmentManager().beginTransaction().replace(R.id.containerFragment, fileFragment).commit();
                        return true;

                }

                return false;
            }
        });

    }

    void init(){
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        container = findViewById(R.id.containerFragment);
        bottomNavigationView.setSelectedItemId(R.id.recorderTab);
    }

}