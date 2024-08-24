package com.example.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pets.data.PetContract.PetEntry;
import com.example.pets.data.PetDbHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PET_LOADER=0;
    PetCursorAdapter mCursorAdapter;
    PetDbHelper mDbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);


        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView petListView = findViewById(R.id.listView);
        View emptyView = findViewById(R.id.emptyView);

        petListView.setEmptyView(emptyView);

        mCursorAdapter = new PetCursorAdapter(CatalogActivity.this, null);
        petListView.setAdapter(mCursorAdapter);



        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this,EditorActivity.class);
                Uri currentPetUri= ContentUris.withAppendedId(PetEntry.CONTENT_URI,id);
                intent.setData(currentPetUri);
                startActivity(intent);


            }
        });

        getLoaderManager().initLoader(PET_LOADER, null, CatalogActivity.this);


    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deletAllPets();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertPet() {

        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME, "TOTO");
        values.put(PetEntry.COLUMN_PET_BREED_NAME, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 8);

        Uri newUri= getContentResolver().insert(PetEntry.CONTENT_URI, values);
        Log.e("CatalogActivity", "this is complete "+ newUri);

    }

    private void deletAllPets(){
        int rowDeleted = getContentResolver().delete(PetEntry.CONTENT_URI,null,null);
        Log.v("CatalogActivity", "rowDeleted Row Deleted from Pet Database: " + rowDeleted);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection ={
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED_NAME

        };
        return new CursorLoader(this,PetEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}