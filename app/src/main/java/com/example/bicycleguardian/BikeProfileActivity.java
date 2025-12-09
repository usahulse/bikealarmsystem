package com.example.bicycleguardian;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.CompoundButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

public class BikeProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BikeProfilePrefs";

    private EditText firstName, lastName, contactNumber, emailAddress, streetAddress, city, state, zipCode, country;
    private EditText manufacturer, model, bikeFrameNumber, color, size, description, insuranceCompany, policyStartDate, policyEndDate, registrationId, dateRegistered;
    private SwitchMaterial isElectric, isInsured, isRegisteredWithPolice, reportedStolen;
    private TextInputLayout insuranceCompanyLayout, registrationIdLayout, dateRegisteredLayout;
    private View insuranceDatesLayout;
    private Button saveButton, selectPhotoButton;
    private ImageView bikePhoto;
    private String bikePhotoPath;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_profile);

        // Initialize views
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        contactNumber = findViewById(R.id.contact_number);
        emailAddress = findViewById(R.id.email_address);
        streetAddress = findViewById(R.id.street_address);
        city = findViewById(R.id.city);
        state = findViewById(R.id.state);
        zipCode = findViewById(R.id.zip_code);
        country = findViewById(R.id.country);
        manufacturer = findViewById(R.id.manufacturer);
        model = findViewById(R.id.model);
        bikeFrameNumber = findViewById(R.id.bike_frame_number);
        color = findViewById(R.id.color);
        size = findViewById(R.id.size);
        description = findViewById(R.id.description);
        isElectric = findViewById(R.id.is_electric);
        isInsured = findViewById(R.id.is_insured);
        insuranceCompanyLayout = findViewById(R.id.insurance_company_layout);
        insuranceCompany = findViewById(R.id.insurance_company);
        insuranceDatesLayout = findViewById(R.id.insurance_dates_layout);
        policyStartDate = findViewById(R.id.policy_start_date);
        policyEndDate = findViewById(R.id.policy_end_date);
        isRegisteredWithPolice = findViewById(R.id.is_registered_with_police);
        registrationIdLayout = findViewById(R.id.registration_id_layout);
        registrationId = findViewById(R.id.registration_id);
        dateRegisteredLayout = findViewById(R.id.date_registered_layout);
        dateRegistered = findViewById(R.id.date_registered);
        reportedStolen = findViewById(R.id.reported_stolen);
        saveButton = findViewById(R.id.btn_save_profile);
        selectPhotoButton = findViewById(R.id.btn_select_photo);
        bikePhoto = findViewById(R.id.bike_photo);

        selectPhotoButton.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // Set listener for the insured switch to toggle visibility of insurance company field
        isInsured.setOnCheckedChangeListener((buttonView, isChecked) -> {
            insuranceCompanyLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            insuranceDatesLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        isRegisteredWithPolice.setOnCheckedChangeListener((buttonView, isChecked) -> {
            registrationIdLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            dateRegisteredLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Set listener for the save button
        saveButton.setOnClickListener(v -> saveProfile());

        // Load existing profile data
        loadProfile();
    }

    private void saveProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Owner Info
        editor.putString("firstName", firstName.getText().toString());
        editor.putString("lastName", lastName.getText().toString());
        editor.putString("contactNumber", contactNumber.getText().toString());
        editor.putString("emailAddress", emailAddress.getText().toString());
        editor.putString("streetAddress", streetAddress.getText().toString());
        editor.putString("city", city.getText().toString());
        editor.putString("state", state.getText().toString());
        editor.putString("zipCode", zipCode.getText().toString());
        editor.putString("country", country.getText().toString());

        // Bike Details
        editor.putString("manufacturer", manufacturer.getText().toString());
        editor.putString("model", model.getText().toString());
        editor.putString("bikeFrameNumber", bikeFrameNumber.getText().toString());
        editor.putString("color", color.getText().toString());
        editor.putString("size", size.getText().toString());
        editor.putString("description", description.getText().toString());
        editor.putBoolean("isElectric", isElectric.isChecked());

        // Insurance Details
        editor.putBoolean("isInsured", isInsured.isChecked());
        editor.putString("insuranceCompany", insuranceCompany.getText().toString());
        editor.putString("policyStartDate", policyStartDate.getText().toString());
        editor.putString("policyEndDate", policyEndDate.getText().toString());

        // Police Registration
        editor.putBoolean("isRegisteredWithPolice", isRegisteredWithPolice.isChecked());
        editor.putString("registrationId", registrationId.getText().toString());
        editor.putString("dateRegistered", dateRegistered.getText().toString());
        editor.putBoolean("reportedStolen", reportedStolen.isChecked());

        // Bike Photo
        if (bikePhotoPath != null) {
            editor.putString("bikePhotoPath", bikePhotoPath);
        }

        editor.apply();

        Toast.makeText(this, R.string.profile_saved_successfully, Toast.LENGTH_SHORT).show();
    }

    private void loadProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Owner Info
        firstName.setText(sharedPreferences.getString("firstName", ""));
        lastName.setText(sharedPreferences.getString("lastName", ""));
        contactNumber.setText(sharedPreferences.getString("contactNumber", ""));
        emailAddress.setText(sharedPreferences.getString("emailAddress", ""));
        streetAddress.setText(sharedPreferences.getString("streetAddress", ""));
        city.setText(sharedPreferences.getString("city", ""));
        state.setText(sharedPreferences.getString("state", ""));
        zipCode.setText(sharedPreferences.getString("zipCode", ""));
        country.setText(sharedPreferences.getString("country", ""));

        // Bike Details
        manufacturer.setText(sharedPreferences.getString("manufacturer", ""));
        model.setText(sharedPreferences.getString("model", ""));
        bikeFrameNumber.setText(sharedPreferences.getString("bikeFrameNumber", ""));
        color.setText(sharedPreferences.getString("color", ""));
        size.setText(sharedPreferences.getString("size", ""));
        description.setText(sharedPreferences.getString("description", ""));
        isElectric.setChecked(sharedPreferences.getBoolean("isElectric", false));

        // Insurance Details
        isInsured.setChecked(sharedPreferences.getBoolean("isInsured", false));
        insuranceCompany.setText(sharedPreferences.getString("insuranceCompany", ""));
        policyStartDate.setText(sharedPreferences.getString("policyStartDate", ""));
        policyEndDate.setText(sharedPreferences.getString("policyEndDate", ""));

        // Police Registration
        isRegisteredWithPolice.setChecked(sharedPreferences.getBoolean("isRegisteredWithPolice", false));
        registrationId.setText(sharedPreferences.getString("registrationId", ""));
        dateRegistered.setText(sharedPreferences.getString("dateRegistered", ""));
        reportedStolen.setChecked(sharedPreferences.getBoolean("reportedStolen", false));

        // Set initial visibility of insurance field
        insuranceCompanyLayout.setVisibility(isInsured.isChecked() ? View.VISIBLE : View.GONE);
        insuranceDatesLayout.setVisibility(isInsured.isChecked() ? View.VISIBLE : View.GONE);
        registrationIdLayout.setVisibility(isRegisteredWithPolice.isChecked() ? View.VISIBLE : View.GONE);
        dateRegisteredLayout.setVisibility(isRegisteredWithPolice.isChecked() ? View.VISIBLE : View.GONE);
        bikePhotoPath = sharedPreferences.getString("bikePhotoPath", null);
        if (bikePhotoPath != null) {
            bikePhoto.setImageURI(Uri.fromFile(new File(bikePhotoPath)));
        }
    }

    private void checkPermissionAndOpenGallery() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_CODE);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, R.string.storage_permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                bikePhotoPath = saveImageToInternalStorage(selectedImageUri);
                bikePhoto.setImageURI(Uri.fromFile(new File(bikePhotoPath)));
            } catch (IOException e) {
                Toast.makeText(this, R.string.failed_to_save_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String saveImageToInternalStorage(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        File outputFile = new File(getFilesDir(), "bike_photo.jpg");
        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        }
        return outputFile.getAbsolutePath();
    }
}
