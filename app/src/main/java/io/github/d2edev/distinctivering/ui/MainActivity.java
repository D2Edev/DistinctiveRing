package io.github.d2edev.distinctivering.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import io.github.d2edev.distinctivering.BuildConfig;
import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.util.Utility;

public class MainActivity extends AppCompatActivity implements BasicActionsListener {
    public static final String TAG = "TAG_" + MainActivity.class.getSimpleName();
    private Toolbar mToolbar;
    public static final int DR_ACTIVE_NOTIFY = 301;

            //TODO make marsmallow permissins logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isTelephonyAvailable(this)) {
            setContentView(R.layout.activity_main);
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mToolbar);
            callMainUI();
            Utility.firstLaunchPreparations(this);

        } else {
            Toast.makeText(this, getString(R.string.no_phone_line), Toast.LENGTH_LONG).show();
            finish();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.common_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about: {
                showAboutDialog();

            }
            default:
                super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAboutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.about_item, null);
        TextView header = (TextView) view.findViewById(R.id.about_header);
        header.setText(getString(R.string.app_name)
                + " v." + Utility.getAppVersion(this)
                +"\n"
                +Utility.getLastBuildTime(this));
        TextView linkText = (TextView) view.findViewById(R.id.about_link);
        linkText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewPage = new Intent(Intent.ACTION_VIEW);
                viewPage.setData(Uri.parse("http://" + getString(R.string.about_web_address)));
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


    @Override
    public void callDeleteUI() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        DeleteFragment df = new DeleteFragment();
        transaction.replace(R.id.fragment_container, df, DeleteFragment.TAG);
        transaction.addToBackStack(DeleteFragment.TAG);
        transaction.commit();
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void callSettingsUI() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "callSettingsUI:");
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, new AppPreferenceFragment(), AppPreferenceFragment.TAG);
        transaction.addToBackStack(AppPreferenceFragment.TAG);
        transaction.commit();
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void callMainUI() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        MainFragment mf = new MainFragment();
        mf.setBasicActionsListener(this);
        transaction.replace(R.id.fragment_container, mf, MainFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(false);
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mToolbar.setNavigationOnClickListener(null);
        }
        super.onBackPressed();
    }


}


