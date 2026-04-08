package com.darshita.photosapp;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PATH = "path";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_DATE = "date";

    private String imagePath;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        imagePath = getIntent().getStringExtra(EXTRA_PATH);
        String name = getIntent().getStringExtra(EXTRA_NAME);
        long size = getIntent().getLongExtra(EXTRA_SIZE, 0);
        long date = getIntent().getLongExtra(EXTRA_DATE, 0);

        ImageView imageView = findViewById(R.id.detailImage);
        TextView tvName = findViewById(R.id.tvName);
        TextView tvPath = findViewById(R.id.tvPath);
        TextView tvSize = findViewById(R.id.tvSize);
        TextView tvDate = findViewById(R.id.tvDate);
        Button btnDelete = findViewById(R.id.btnDelete);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);

        tvName.setText("Name: " + name);
        tvPath.setText("Path: " + imagePath);
        tvSize.setText("Size: " + formatSize(size));
        tvDate.setText("Date: " + formatDate(date));

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return String.format(Locale.getDefault(), "%.2f MB", size / (1024.0 * 1024.0));
    }

    private String formatDate(long date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(date));
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteImage() {
        File file = new File(imagePath);

        // Try simple delete first (works for app-private files or with write permission)
        if (file.delete()) {
            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // For Android 10+, use MediaStore API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deleteUsingMediaStore();
        } else {
            Toast.makeText(this, "Failed to delete image. Check storage permissions.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUsingMediaStore() {
        ContentResolver contentResolver = getContentResolver();

        // Find the image URI in MediaStore
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[]{imagePath};

        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try (android.database.Cursor cursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                try {
                    // Try to delete
                    contentResolver.delete(imageUri, null, null);
                    Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } catch (RecoverableSecurityException e) {
                    // Need user permission - request it
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        IntentSender intentSender = e.getUserAction().getActionIntent().getIntentSender();
                        IntentSenderRequest request = new IntentSenderRequest.Builder(intentSender).build();
                        deletePermissionLauncher.launch(request);
                    }
                } catch (SecurityException e) {
                    // For Android 11+, use createDeleteRequest
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        List<Uri> uris = Collections.singletonList(imageUri);
                        PendingIntent pendingIntent = MediaStore.createDeleteRequest(contentResolver, uris);
                        IntentSenderRequest request = new IntentSenderRequest.Builder(pendingIntent.getIntentSender()).build();
                        deletePermissionLauncher.launch(request);
                    } else {
                        Toast.makeText(this, "Permission denied to delete image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // Image not found in MediaStore, try direct file delete with write permission
                Toast.makeText(this, "Failed to delete image. Check storage permissions.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> deletePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Permission granted, try deleting again
                    if (imageUri != null) {
                        try {
                            getContentResolver().delete(imageUri, null, null);
                            Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                            finish();
                        } catch (SecurityException e) {
                            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Delete permission denied", Toast.LENGTH_SHORT).show();
                }
            });
}
