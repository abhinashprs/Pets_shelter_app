package com.example.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.example.pets.data.PetContract.PetEntry;
import com.example.pets.data.PetDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PET_LOADER = 0;

    private Uri mCurrentPetUri;


    private EditText mNameEditText;

    private EditText mBreedEditText;

    private EditText mWeightEditText;


    private Spinner mGenderSpinner;


    private int mGender = 0;

    private boolean mPetHasChange=false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChange=true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();
        if (mCurrentPetUri == null) {
            setTitle(R.string.editor_activity_title_new_pet);

            invalidateOptionsMenu();

        } else {
            setTitle(R.string.editor_activity_title_edit_pet);

            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);


        }


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);


        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);

        setupSpinner();
    }


    private void setupSpinner() {

        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(EditorActivity.this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                mGender = 0; // Unknown
            }
        });
    }

    private void savePet() {

        String nameString = mNameEditText.getText().toString().trim();
        String mBreedString = mBreedEditText.getText().toString().trim();
        String mWeightString = mWeightEditText.getText().toString().trim();

        if (mCurrentPetUri== null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(mBreedString) &&
                TextUtils.isEmpty(mWeightString) &&
                mGender==PetEntry.GENDER_UNKNOWN){
            return;
        }


        ContentValues values = new ContentValues();

        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED_NAME, mBreedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        int weight = 0;
        if (!TextUtils.isEmpty(mWeightString)){
            weight= Integer.parseInt(mWeightString);
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

       if (mCurrentPetUri == null){
           Uri uriNew = getContentResolver().insert(PetEntry.CONTENT_URI, values);
           if (uriNew==null){
               Toast.makeText(this, getString(R.string.insert_failed),Toast.LENGTH_SHORT).show();
           }else {
               Toast.makeText(this,getString(R.string.insert_successful ),Toast.LENGTH_SHORT).show();
           }
       }else {

           int rowAffected = getContentResolver().update(mCurrentPetUri, values, null);
            if (rowAffected== 0){
                Toast.makeText(this,getString(R.string.insert_failed),Toast.LENGTH_SHORT).show();

            }else {

                Toast.makeText(this,getString(R.string.insert_successful ), Toast.LENGTH_SHORT).show();
            }
       }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentPetUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //insert pet
                savePet();
                // finish activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConformationDialog();

                    return true;
                    // Respond to a click on the "Up" arrow button in the app bar
                    case android.R.id.home:
                        if (!mPetHasChange) {
                            // Navigate back to parent activity (CatalogActivity)
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            return true;
                        }

                        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                        showUnsavedChangesDialog(discardButtonClickListener);


        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed(){
        if (!mPetHasChange){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener dialogOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();


            }
        };
        showUnsavedChangesDialog(dialogOnClickListener);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED_NAME,
                PetEntry.COLUMN_PET_WEIGHT,
                PetEntry.COLUMN_PET_GENDER


        };
        return new CursorLoader(EditorActivity.this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToNext()) {
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED_NAME);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);

            mNameEditText.setText(name);
            mWeightEditText.setText(Integer.toString(weight));
            mBreedEditText.setText(breed);
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                case PetEntry.GENDER_UNKNOWN:
                    mGenderSpinner.setSelection(0);
                    break;
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGenderSpinner.setSelection(0);
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mNameEditText.setText("");

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.title_for_discard);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog!= null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog= builder.create();
        alertDialog.show();
    }

    private void showDeleteConformationDialog()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(EditorActivity.this);
        builder.setMessage(R.string.delet_dialoge_msg);
        builder.setPositiveButton(R.string.delet, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog!= null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void deletePet(){

        if (mCurrentPetUri != null){
            int rowDeleted = getContentResolver().delete(mCurrentPetUri,null,null);
            if(rowDeleted == 0){
                Toast.makeText(EditorActivity.this,getString(R.string.delet),Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(EditorActivity.this,getString(R.string.editor_deleting_fail),Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

}
