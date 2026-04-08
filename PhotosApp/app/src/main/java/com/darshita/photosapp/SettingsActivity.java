package com.darshita.photosapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PhotosAppPrefs";
    private static final String KEY_FOLDER = "folder_path";
    private static final String KEY_FOLDER_URI = "folder_uri";

    private TextView tvCurrentFolder;
    private MaterialButton btnChangeFolder;
    private String selectedFolder;
    private ActivityResultLauncher<Intent> folderPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        tvCurrentFolder = findViewById(R.id.tvCurrentFolder);
        btnChangeFolder = findViewById(R.id.btnChangeFolder);

        // Register folder picker result handler
        folderPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri treeUri = result.getData().getData();
                        if (treeUri != null) {
                            // Take persistable permission for the selected folder
                            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(treeUri, takeFlags);

                            // Convert URI to path
                            String path = getPathFromUri(treeUri);
                            if (path != null) {
                                selectedFolder = path;
                                getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                        .putString(KEY_FOLDER, selectedFolder)
                                        .putString(KEY_FOLDER_URI, treeUri.toString())
                                        .apply();
                                tvCurrentFolder.setText(selectedFolder);
                                Toast.makeText(this, "Folder selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        loadCurrentFolder();

        btnChangeFolder.setOnClickListener(v -> openFolderPicker());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentFolder();
    }

    private void loadCurrentFolder() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        selectedFolder = prefs.getString(KEY_FOLDER,
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        tvCurrentFolder.setText(selectedFolder);
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        // Optionally set initial folder (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri initialUri = Uri.parse(selectedFolder);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
        }

        folderPickerLauncher.launch(intent);
    }

    private String getPathFromUri(Uri uri) {
        // Try to get real path from URI
        if (uri.getScheme().equals("file")) {
            return uri.getPath();
        }

        // Handle content:// URIs from DocumentsProvider
        if (uri.getAuthority() != null && uri.getAuthority().contains("com.android.externalstorage.documents")) {
            String docId = DocumentsContract.getTreeDocumentId(uri);
            String[] split = docId.split(":");
            if (split.length >= 2) {
                String type = split[0];
                String path = split[1];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + path;
                } else {
                    return "/storage/" + type + "/" + path;
                }
            } else if (split.length == 1) {
                return Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        }

        // Fallback: use persisted URI path
        return uri.toString();
    }
}
