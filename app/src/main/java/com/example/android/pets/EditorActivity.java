/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetsContract.PetEntry;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText nameEditText;
    private EditText breedEditText;
    private EditText weightEditText;
    private Spinner genderSpinner;
    private int gender = PetEntry.GENDER_UNKNOWN;
    private static final int PET_LOADER = 1;
    private Uri uriReceived;
    private boolean petHasChanged = false;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            petHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        uriReceived = intent.getData();
        if (uriReceived == null) {
            setTitle(getString(R.string.add_new_pet));
            invalidateOptionsMenu();
        }
        else {
            setTitle(getString(R.string.edit_pet));
            getLoaderManager().initLoader(PET_LOADER, null, this);
        }

        nameEditText = findViewById(R.id.edit_pet_name);
        breedEditText = findViewById(R.id.edit_pet_breed);
        genderSpinner = findViewById(R.id.spinner_gender);
        weightEditText = findViewById(R.id.edit_pet_weight);

        nameEditText.setOnTouchListener(onTouchListener);
        breedEditText.setOnTouchListener(onTouchListener);
        genderSpinner.setOnTouchListener(onTouchListener);
        weightEditText.setOnTouchListener(onTouchListener);
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        genderSpinner.setAdapter(genderSpinnerAdapter);
        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        gender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        gender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        gender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                savePet();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!petHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListner =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListner);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        super.onPrepareOptionsPanel(view, menu);
        if (uriReceived == null) {
            MenuItem delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
        }
        return true;
    }


    private void savePet() {
        String name = nameEditText.getText().toString().trim();
        String breed = breedEditText.getText().toString().trim();
        String weightString = weightEditText.getText().toString().trim();

        if (uriReceived == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(breed) &&
                TextUtils.isEmpty(weightString) && gender == PetEntry.GENDER_UNKNOWN)
            return;

        int weight = 0;
        if (!TextUtils.isEmpty(weightString))
            weight = Integer.parseInt(weightString);

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, name);
        values.put(PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetEntry.COLUMN_PET_GENDER, gender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        if (uriReceived == null) {
            Uri uri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
            if (uri == null)
                Toast.makeText(this, R.string.error_saving_pet, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.pet_saved, Toast.LENGTH_LONG).show();
        } else {
            int rowsAffected = getContentResolver().update(uriReceived, values, null, null);
            if (rowsAffected == 0)
                Toast.makeText(this, getString(R.string.error_updating_pet), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, getString(R.string.pet_updated_successfully), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, uriReceived, null, null, null, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst())
            return;
        cursor.moveToFirst();
        nameEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)));
        breedEditText.setText(cursor.getString(cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)));
        weightEditText.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT))));
        genderSpinner.setSelection(cursor.getInt(cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText(null);
        breedEditText.setText(null);
        weightEditText.setText(null);
        genderSpinner.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        if (!petHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListner =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListner);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deletePet() {
        if (uriReceived == null)
            return;
        int rowsDeleted = getContentResolver().delete(uriReceived, null, null);
        if (rowsDeleted == 0)
            Toast.makeText(this, getString(R.string.error_deleting_pet), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, getString(R.string.pet_deleted_successfully), Toast.LENGTH_LONG).show();
        finish();
    }
}