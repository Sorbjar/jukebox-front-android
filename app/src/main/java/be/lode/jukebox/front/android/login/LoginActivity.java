package be.lode.jukebox.front.android.login;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import be.lode.jukebox.front.android.R;

public class LoginActivity extends FragmentActivity {

    public static final int INDEX_SIMPLE_LOGIN = 0;
    public static final String FRAGMENT_TAG = "fragment_tag";
    private static final String STATE_SELECTED_FRAGMENT_INDEX = "selected_fragment_index";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        fragmentManager = getSupportFragmentManager();

        Fragment fragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(android.R.id.content, new LoginFragment(), FRAGMENT_TAG);
        transaction.commit();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_login, menu);
        return false;
    }
}