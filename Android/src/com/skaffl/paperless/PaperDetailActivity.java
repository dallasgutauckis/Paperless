package com.skaffl.paperless;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMFile;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.callbacks.FileCreationResponseCallback;
import com.cloudmine.api.rest.callbacks.FileLoadCallback;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.skaffl.paperless.dummy.Worksheets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PaperDetailActivity extends FragmentActivity {

    private static final String TAG = "PaperDetailActivity";
    private PaperDetailFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CMApiCredentials.initialize(Constants.CM_APP_ID, Constants.CM_API_KEY, getApplicationContext());
        setContentView(R.layout.activity_paper_detail);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Log.v(TAG, "savedInstanceState is null");
            Bundle arguments = new Bundle();
            arguments.putString(PaperDetailFragment.ARG_ITEM_ID, getIntent().getStringExtra(PaperDetailFragment.ARG_ITEM_ID));
            mFragment = new PaperDetailFragment();
            mFragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.paper_detail_container, mFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tools, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, PaperListActivity.class));
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            Worksheets.getFiles();
        } else if (item.getItemId() == R.id.save) {
            Worksheets.saveFiles(this);
            return true;
        } else if (item.getItemId() == R.id.reset) {
            Worksheets.resetStudent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
