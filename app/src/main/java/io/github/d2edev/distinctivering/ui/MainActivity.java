package io.github.d2edev.distinctivering.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.util.Utility;

public class MainActivity extends AppCompatActivity {
    public static final String TAG="TAG_"+MainActivity.class.getSimpleName();
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container,new MainFragment(), MainFragment.TAG).commit();
        Utility.firstLaunchPreparations(this);


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about: {
                showAboutDialog();

            }
            default:super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.about_item,null);
        TextView header= (TextView) view.findViewById(R.id.about_header);
        header.setText(getString(R.string.app_name)+" v."+Utility.getAppVersion(this));
        TextView linkText= (TextView) view.findViewById(R.id.about_link);
        linkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewPage=new Intent(Intent.ACTION_VIEW);
                viewPage.setData(Uri.parse("http://"+getString(R.string.about_web_address)));
                startActivity(viewPage);
            }
        });
        builder
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.create().show();
    }
}


