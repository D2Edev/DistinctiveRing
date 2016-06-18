package io.github.d2edev.distinctivering.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import io.github.d2edev.distinctivering.R;

public class DeleteContactActivity extends AppCompatActivity {
    public static final String TAG="TAG_MainFabActivity";

    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSelectedrecords();

            }
        });


        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.delete_fragment_container,new DeleteFragment(), DeleteFragment.TAG).commit();
getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void deleteSelectedrecords() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_about:{
                Toast.makeText(this,getString(R.string.action_main_about_title),Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
