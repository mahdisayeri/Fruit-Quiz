package ir.tokaterm.fruitequiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String CHOICES="pref_NumberOfChoice";
    public static final String REGIONS="pref_regionsToInclude";
    private boolean phoneDivice=true;
    private boolean preferencesChenged=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesChengedListener);


        int screenSize=getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if(screenSize==Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize==Configuration.SCREENLAYOUT_SIZE_XLARGE){
          phoneDivice=false;
        }
        if(phoneDivice){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(preferencesChenged){
            MainActivityFragment quizFragment= (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChenged =false;
        }
    }


    private SharedPreferences.OnSharedPreferenceChangeListener preferencesChengedListener=new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            preferencesChenged=true;
            MainActivityFragment quizFragment= (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            if(key.equals(CHOICES)){
               quizFragment.updateGuessRows(sharedPreferences);
               quizFragment.resetQuiz();
            }else if(key.equals(REGIONS)){
                Set<String> regions=sharedPreferences.getStringSet(REGIONS,null);
                if(regions!=null && regions.size()>0){
                  quizFragment.updateRegions(sharedPreferences);
                    quizFragment.resetQuiz();
                }else{
                    regions.add(getString(R.string.default_region));
                    SharedPreferences.Editor editor=sharedPreferences.edit();
                    editor.putStringSet(REGIONS,regions);
                    editor.apply();
                    Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(MainActivity.this, R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        int orientation=getResources().getConfiguration().orientation;
        if(orientation== Configuration.ORIENTATION_PORTRAIT){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }else {
            return false;
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent preferencesIntent=new Intent(this,SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
      //  super.onBackPressed();
        super.onPause();
        AlertDialog.Builder ab=new AlertDialog.Builder(this);
        ab.setMessage(R.string.exit_message);
        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                MainActivity.this.finish();
            }
        });
        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.onResume();
                dialog.cancel();
            }
        });
        ab.create().show();
    }
}
