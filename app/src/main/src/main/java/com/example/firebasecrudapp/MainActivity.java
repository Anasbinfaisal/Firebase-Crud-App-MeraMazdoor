package com.example.firebasecrudapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements CourseRVAdapter.CourseClickInterface {

    private static final String MY_PREFS_NAME = "isAdmin";
    //creating variables for fab, firebase database, progress bar, list, adapter,firebase auth, recycler view and relative layout.
    private FloatingActionButton addCourseFAB;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private RecyclerView courseRV;
    private FirebaseAuth mAuth;
    private ProgressBar loadingPB;
    private ArrayList<CourseRVModal> courseRVModalArrayList;
    private CourseRVAdapter courseRVAdapter;
    private RelativeLayout homeRL;
    private Boolean isAdmin = false;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_drawer_activty);
        //initializing all our variables.
        courseRV = findViewById(R.id.idRVCourses);
        homeRL = findViewById(R.id.idRLBSheet);
        loadingPB = findViewById(R.id.idPBLoading);
        addCourseFAB = findViewById(R.id.idFABAddCourse);
        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        courseRVModalArrayList = new ArrayList<>();
        //on below line we are getting database reference.
        databaseReference = firebaseDatabase.getReference("Courses");



        addCourseFAB.setVisibility(View.INVISIBLE);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        isAdmin = prefs.getBoolean("isAdmin", false);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null)
        {
            isAdmin = bundle.getBoolean("isAdmin");
            Log.d("oncreate", "inside_bundle!=null condition: " + isAdmin +" " + email);
        }
        Log.d("oncreate", "oncreatestart: " + isAdmin);


            email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail().toString();



        //on below line adding a click listener for our floating action button.
       if(isAdmin) {
           addCourseFAB.setVisibility(View.VISIBLE);
       }

        addCourseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opening a new activity for adding a course.
                Intent i = new Intent(MainActivity.this, AddCourseActivity.class);
                i.putExtra("isAdmin", isAdmin);
                startActivity(i);
            }
        });

       NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.emailtextView);
        navUsername.setText(email);

       navigationView.setNavigationItemSelectedListener(item -> {

           if(item.getItemId() == R.id.nav_Profile){

               Toast.makeText(MainActivity.this, " " + email, Toast.LENGTH_SHORT).show();
           }
           DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
           drawerLayout.closeDrawer(GravityCompat.START);

           return true;
       });

        //on below line initializing our adapter class.
        courseRVAdapter = new CourseRVAdapter(courseRVModalArrayList, this, this);
        //setting layout malinger to recycler view on below line.
        courseRV.setLayoutManager(new LinearLayoutManager(this));
        //setting adapter to recycler view on below line.
        courseRV.setAdapter(courseRVAdapter);
        //on below line calling a method to fetch courses from database.
        getCourses();

        Log.d("oncreate", "oncreateend: " + isAdmin);
        SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isAdmin", isAdmin);
        editor.commit();


    }

    @Override
    public void onBackPressed() {

        
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.super.onBackPressed();
                        mAuth.signOut();
                        Toast.makeText(getApplicationContext(), "User Logged Out", Toast.LENGTH_LONG).show();
                        //on below line we are signing out our user.
                        mAuth.signOut();
                        //on below line we are opening our login activity.
                        isAdmin = false;
                        isAdmin = false;
                        SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isAdmin", isAdmin);
                        editor.commit();
                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(i);
                       MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
        
        
    }


    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        isAdmin = prefs.getBoolean("isAdmin", false);

        Log.d("onresume", "onResume: " + isAdmin);

        if(isAdmin) {
            addCourseFAB.setVisibility(View.VISIBLE);
                    } else {
            addCourseFAB.setVisibility(View.GONE);
        }


    }


    private void getCourses() {
        //on below line clearing our list.
        courseRVModalArrayList.clear();
        //on below line we are calling add child event listener method to read the data.
        databaseReference.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //on below line we are hiding our progress bar.
                loadingPB.setVisibility(View.GONE);
//                //adding snapshot to our array list on below line.
                courseRVModalArrayList.add((snapshot).getValue(CourseRVModal.class));
                //notifying our adapter that data has changed.
                courseRVAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //this method is called when new child is added we are notifying our adapter and making progress bar visibility as gone.
                loadingPB.setVisibility(View.GONE);
                //adding snapshot to our array list on below line.
              //  courseRVModalArrayList.add(snapshot.getValue(CourseRVModal.class));
                courseRVAdapter.notifyDataSetChanged();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                //notifying our adapter when child is removed.
                courseRVAdapter.notifyDataSetChanged();
                loadingPB.setVisibility(View.GONE);

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //notifying our adapter when child is moved.
                courseRVAdapter.notifyDataSetChanged();
                loadingPB.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCourseClick(int position) {
        //calling a method to display a bottom sheet on below line.
        displayBottomSheet(courseRVModalArrayList.get(position));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //adding a click listner for option selected on below line.
        int id = item.getItemId();
        if (id == R.id.idLogOut) {//displaying a toast message on user logged out inside on click.
            Toast.makeText(getApplicationContext(), "User Logged Out", Toast.LENGTH_LONG).show();
            //on below line we are signing out our user.
            mAuth.signOut();
            isAdmin = false;
            SharedPreferences sharedPref = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isAdmin", isAdmin);
            editor.commit();

            //on below line we are opening our login activity.
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //on below line we are inflating our menu file for displaying our menu options.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("SetTextI18n")
    private void displayBottomSheet(CourseRVModal modal) {
        //on below line we are creating our bottom sheet dialog.
        final BottomSheetDialog bottomSheetTeachersDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        //on below line we are inflating our layout file for our bottom sheet.
        View layout = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, homeRL);
        //setting content view for bottom sheet on below line.
        bottomSheetTeachersDialog.setContentView(layout);

        //on below line we are setting a cancelable
        bottomSheetTeachersDialog.setCancelable(false);
        bottomSheetTeachersDialog.setCanceledOnTouchOutside(true);
        //calling a method to display our bottom sheet.
        bottomSheetTeachersDialog.show();
        //on below line we are creating variables for our text view and image view inside bottom sheet
        //and initialing them with their ids.
        TextView courseNameTV = layout.findViewById(R.id.idTVCourseName);
        TextView courseDescTV = layout.findViewById(R.id.idTVCourseDesc);
        TextView suitedForTV = layout.findViewById(R.id.idTVSuitedFor);
        TextView priceTV = layout.findViewById(R.id.idTVCoursePrice);

        //on below line we are setting data to different views on below line.
        courseNameTV.setText(modal.getCourseName());
        courseDescTV.setText(modal.getCourseDescription());
        suitedForTV.setText("Suited for: " + modal.getBestSuitedFor());
        priceTV.setText("Rs." + modal.getCoursePrice());

//        Button viewBtn = layout.findViewById(R.id.idBtnVIewDetails);
        Button editBtn = layout.findViewById(R.id.idBtnEditCourse);

        if(isAdmin) {
            editBtn.setVisibility(View.VISIBLE);
        }
            //adding on click listener for our edit button.
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //on below line we are opening our EditCourseActivity on below line.
                    Intent i = new Intent(MainActivity.this, EditCourseActivity.class);
                    i.putExtra("isAdmin", isAdmin);
                    //on below line we are passing our course modal
                    i.putExtra("course", modal);
                    startActivity(i);
                }
            });

        }

        //adding click listener for our view button on below line.
//        viewBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //on below line we are navigating to browser for displaying course details from its url
//                Intent i = new Intent();  //Intent.ACTION_VIEW);
////                i.setData(Uri.parse(modal.getCourseLink()));
//                startActivity(i);
//            }
//        }
//        );

  //  }


}