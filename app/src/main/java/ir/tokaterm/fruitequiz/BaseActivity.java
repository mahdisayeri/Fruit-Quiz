package ir.tokaterm.fruitequiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class BaseActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_nav_home:
                    Intent main=new Intent(BaseActivity.this,MainActivity.class);
                    startActivity(main);
                  //  mTextMessage.setText(R.string.title_nav_home);
                    return true;
                case R.id.bottom_nav_word_list:
                    mTextMessage.setText(R.string.title_nave_wordList);
                    return true;
                case R.id.bottom_nav_profile:
                    mTextMessage.setText(R.string.title_nave_profile);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mTextMessage = (TextView) findViewById(R.id.base_message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.base_bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
