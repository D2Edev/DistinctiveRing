package io.github.d2edev.distinctivering;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import io.github.d2edev.distinctivering.db.DataDBHelper;
import io.github.d2edev.distinctivering.util.Utility;

public class MainFabActivity extends AppCompatActivity {
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDistinctiveRingSettings();

            }
        });

        DataDBHelper dbHelper = new DataDBHelper(this);

    }

    //process click
    private void changeDistinctiveRingSettings() {
        if(Utility.isDistinctiveRingEnabled(this)){

            Utility.setDistinctiveRingEnabled(this,false);
            fab.setImageResource(R.drawable.ic_volume_off_white);
            Toast.makeText(this,"enabled:" + Utility.isDistinctiveRingEnabled(this),Toast.LENGTH_SHORT).show();
        }else{
            Utility.setDistinctiveRingEnabled(this,true);
            fab.setImageResource(R.drawable.ic_volume_up_white);
            Toast.makeText(this,"enabled:" + Utility.isDistinctiveRingEnabled(this),Toast.LENGTH_SHORT).show();
        }
    }

}
