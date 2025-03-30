package com.example.thunghiem;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainPDFActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int FOLDER_PICKER_CODE = 101;
    private static final int MANAGE_STORAGE_PERMISSION_CODE = 102;

    private Button btnBrowse;
    private TextView txtPath;
    private RecyclerView recyclerView;
    private PDFAdapter pdfAdapter;
    private List<Uri> pdfUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initViews();
        setupRecyclerView();
        setupListeners();
        checkPermissions();
    }

    private void initViews() {
        btnBrowse = findViewById(R.id.button);
        txtPath = findViewById(R.id.textView5);
        recyclerView = findViewById(R.id.recyclerview);
        pdfUris = new ArrayList<>();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        pdfAdapter = new PDFAdapter(this, pdfUris);
        recyclerView.setAdapter(pdfAdapter);
    }

    private void setupListeners() {
        btnBrowse.setOnClickListener(v -> openFolderPicker());
    }

    private void openFolderPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, FOLDER_PICKER_CODE);
    }

    private void scanPDFFiles(Uri folderUri) {
        pdfUris.clear();
        DocumentFile folder = DocumentFile.fromTreeUri(this, folderUri);

        if (folder != null && folder.exists()) {
            txtPath.setText("Đường dẫn: " + folder.getName());

            for (DocumentFile file : folder.listFiles()) {
                if (file.isFile() && "application/pdf".equals(file.getType())) {
                    pdfUris.add(file.getUri());
                }
            }

            Toast.makeText(this, "Tìm thấy " + pdfUris.size() + " file PDF",
                    Toast.LENGTH_SHORT).show();

            pdfAdapter.notifyDataSetChanged();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",
                            getApplicationContext().getPackageName())));
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, MANAGE_STORAGE_PERMISSION_CODE);
                }
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Cần cấp quyền để truy cập file!",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MANAGE_STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "Đã được cấp quyền quản lý file",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Cần cấp quyền để quản lý file",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        } else if (requestCode == FOLDER_PICKER_CODE && resultCode == RESULT_OK && data != null) {
            Uri folderUri = data.getData();
            if (folderUri != null) {
                // Quét file PDF trong thư mục được chọn
                scanPDFFiles(folderUri);
            }
        }
    }
}