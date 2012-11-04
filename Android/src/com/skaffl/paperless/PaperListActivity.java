package com.skaffl.paperless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.skaffl.paperless.dummy.Worksheets;

public class PaperListActivity extends FragmentActivity implements PaperListFragment.Callbacks, Constants {

    private boolean mTwoPane;

    // In the class declaration section:
    private DropboxAPI<AndroidAuthSession> mDBApi;

    final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tools, menu);
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_list);

        if (findViewById(R.id.paper_detail_container) != null) {
            mTwoPane = true;
            ((PaperListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.paper_list))
                    .setActivateOnItemClick(true);
        }

        // And later in some initialization function:
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        // MyActivity below should be your activity class name
        //mDBApi.getSession().startAuthentication(PaperListActivity.this);

        Worksheets.init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                // Provide your own storeKeys to persist the access token pair
                // A typical way to store tokens is using SharedPreferences
                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }
    }

    private void storeKeys(String key, String secret) {
        SharedPreferences prefs = getSharedPreferences("paper", MODE_PRIVATE);
        SharedPreferences.Editor prefEdit = prefs.edit();

        prefEdit.putString("key", key);
        prefEdit.putString("secret", secret);
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(PaperDetailFragment.ARG_ITEM_ID, id);
            PaperDetailFragment fragment = new PaperDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.paper_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, PaperDetailActivity.class);
            detailIntent.putExtra(PaperDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
