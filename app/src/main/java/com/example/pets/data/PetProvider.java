package com.example.pets.data;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Switch;
import com.example.pets.data.PetContract.PetEntry;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PetProvider extends ContentProvider {
    public static final String LOG_TAG = PetProvider.class.getSimpleName();
   private static final int PETS= 100;
   private static final int PETS_ID = 101;
   private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

   static {
       sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS,PETS );
       sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS + "/#", PETS_ID);
   }



    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        mDbHelper = new PetDbHelper(getContext());


        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match= sUriMatcher.match(uri);
        switch (match){
            case PETS:
                cursor= database.query(PetEntry.TABLE_NAME,projection,selection,selectionArgs, null, null,sortOrder );
                break;
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                try {
                    throw  new IllegalAccessException("Cannot Find Uri: " + uri);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final  int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                insertPets(uri, values);
                return uri;
            default:
                try {
                    throw new IllegalAccessException("Insertion not supported " + uri );
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
        }
    }
    private Uri insertPets(Uri uri, ContentValues values){

        String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
        if (name==null){
            try {
                throw new IllegalAccessException("Pet Require Name");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
        Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
        if (gender== null || !PetEntry.isValidGender(gender) ){
            try {
                throw new IllegalAccessException("Require valid Gender");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
        if (weight == null && weight > 0){
            try {
                throw  new IllegalAccessException(" Require valid weight");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(PetEntry.TABLE_NAME,null,values);
        if (id==-1){
            Log.e(LOG_TAG, "insertion not possible"+ uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, id);


    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return updatePets(uri, values, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePets(uri, values, selection, selectionArgs);
            default:
                try {
                    throw new IllegalAccessException("Update is not supported for id Uri: " + uri );
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

        }

    }
    private int updatePets(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                try {
                    throw new IllegalAccessException("Pets Require a name ");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
            if (values.containsKey(PetEntry.COLUMN_PET_GENDER)){
                Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
                if (gender == null || !PetEntry.isValidGender(gender)){
                    try {
                        throw new IllegalAccessException("Pets require  gender");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)){
                Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
                if (weight == null && weight > 0 ){
                    try {
                        throw new IllegalAccessException("Pet require the weight");
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            if (values.size()==0){
                return 0;
            }

            SQLiteDatabase database =mDbHelper.getWritableDatabase();

        int rowUpdate = database.update(PetEntry.TABLE_NAME,values,selection,selectionArgs);
        if (rowUpdate!=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowUpdate;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
       SQLiteDatabase database = mDbHelper.getWritableDatabase();
       final int match = sUriMatcher.match(uri);

       int rowDeleted;

       switch (match){
           case PETS:
               rowDeleted= database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
               break;
           case PETS_ID:
               selection= PetEntry._ID + "=?";
               selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
               rowDeleted= database.delete(PetEntry.TABLE_NAME, selection,selectionArgs);
               break;
           default:
               try {
                   throw new IllegalAccessException("Deletion not supported: "+ uri);
               } catch (IllegalAccessException e) {
                   throw new RuntimeException(e);
               }
       }

        if (rowDeleted!= 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rowDeleted;
     }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                try {
                    throw new IllegalAccessException("Unknown Uri " + uri);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
        }
    }
}
