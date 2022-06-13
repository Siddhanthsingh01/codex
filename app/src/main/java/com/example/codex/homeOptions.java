package com.example.codex;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.codex.HidekeyInternet.KeyboardHideInternet;
import com.example.codex.Logins.Login;
import com.example.codex.Slider.SliderImage;
import com.example.codex.Slider.SliderImageAdapter;
import com.example.codex.category.c_plus;
import com.example.codex.category.clang;
import com.example.codex.category.java;
import com.example.codex.category.python;
import com.example.codex.databinding.ActivityHomeOptionsBinding;
import com.example.codex.drawer.DrawerMenu;
import com.example.codex.quiz.quiz;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class homeOptions extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ActivityHomeOptionsBinding binding;

    ScrollView contentView;

    private TextView name_tv;
    private TextView email_tv;

    //slider
    private ViewPager2 viewPager2;
    private List<SliderImage> imageList;
    private final Handler sliderHandler = new Handler();
    KeyboardHideInternet keyin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //view binding
        binding = ActivityHomeOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        keyin = new KeyboardHideInternet();

        //on clicking logout image
        binding.homeActIvLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            //finishing received activity
            finish();
        });

        //java catagory card view
        binding.java.setOnClickListener(view -> {
            if(!keyin.isConnected(homeOptions.this))
                keyin.connectionAlert(homeOptions.this);
            else
                startActivity(new Intent(this, java.class));
        });


        //python catagory card view
        binding.python.setOnClickListener(view -> {
            if(!keyin.isConnected(homeOptions.this))
                keyin.connectionAlert(homeOptions.this);
            else
                startActivity(new Intent(this, python.class));
        });

        //clang catagory card view
        binding.cv.setOnClickListener(view -> {
            if(!keyin.isConnected(homeOptions.this))
                keyin.connectionAlert(homeOptions.this);
            else
                startActivity(new Intent(this, clang.class));
        });

        //cplus catagory card view
        binding.cplus.setOnClickListener(view -> {
            if(!keyin.isConnected(homeOptions.this))
                keyin.connectionAlert(homeOptions.this);
            else
                startActivity(new Intent(this, c_plus.class));
        });

        //hooks of content view
        contentView = findViewById(R.id.home_content);

        //drawer
        navigationDrawer();


        //slider

        viewPager2 = findViewById(R.id.viewpager2);
        imageList = new ArrayList<>();

        slider();


    }

    private void slider() {


        imageList.add(new SliderImage(R.drawable.bannerone));
        imageList.add(new SliderImage(R.drawable.banner2));
        imageList.add(new SliderImage(R.drawable.bannerone));
        imageList.add(new SliderImage(R.drawable.banner2));
        imageList.add(new SliderImage(R.drawable.bannerone));
        imageList.add(new SliderImage(R.drawable.banner2));

        SliderImageAdapter adapter = new SliderImageAdapter(imageList);
        viewPager2.setAdapter(adapter);

        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setClipChildren(false);
        viewPager2.setClipToPadding(false);

        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(40));
        transformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.14f);
        });
        viewPager2.setPageTransformer(transformer);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable,4000);

            }
        });


    }


    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {

            //slide value will be increased until reaches to last image i.e., img.size-1
            if (viewPager2.getCurrentItem() != imageList.size()-1){
                viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
            }
            //when slider reaches the last img then we will send it back to image 0
            else{
                viewPager2.setCurrentItem(viewPager2.getCurrentItem() - (imageList.size()-1));
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, 4000);
    }

    //end of slider

    private void navigationDrawer() {
        //brings focus to drawer
        binding.navigationView.bringToFront();
        //says the current clicked item
        binding.navigationView.setNavigationItemSelectedListener(this);
        //default checked item
//        binding.navigationView.setCheckedItem(R.id.nav_home1);

        //calling update nav to set name,mail for nav header
        updateMenuData();



        //menu trigger on btn click
        binding.homeActIvMenuTrigger.setOnClickListener(view -> {
            //if drawer is open the close
            if (binding.drawerLayout.isDrawerVisible(GravityCompat.START))
                binding.drawerLayout.closeDrawer(GravityCompat.START);

                //else start the drawer
            else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
                binding.homeActIvMenuTrigger.setVisibility(View.INVISIBLE);
            }
        });

        //calling the animate drawer fun from DrawerMenu class
        DrawerMenu dm = new DrawerMenu();
        dm.animateNavigationDrawer(binding.drawerLayout, binding.homeContent, binding.homeActIvMenuTrigger);
    }

    private void updateMenuData() {
        //database reference
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        //getting menu header reference
        View header = binding.navigationView.getHeaderView(0);
        //instancing tv using the header reference
        name_tv = header.findViewById(R.id.menu_logo);
        email_tv = header.findViewById(R.id.menu_slogan);

        //getting db data as snapshots
        databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //storing retrieved data in vars
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                //setting to created tv

                name_tv.setText(name);
                email_tv.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getApplicationContext(), "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.nav_logout){
            FirebaseAuth.getInstance().signOut();
            //finishing received activity
            finish();
            startActivity(new Intent(this, Login.class));
        }else{

            DrawerMenu dm = new DrawerMenu();
            dm.navigateMenu(item, this);
        }


        //closing drawer after selecting any option
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        //close the drawer if it is open on back pressed
        if (binding.drawerLayout.isDrawerVisible(GravityCompat.START))
            binding.drawerLayout.closeDrawer(GravityCompat.START);
    }


}