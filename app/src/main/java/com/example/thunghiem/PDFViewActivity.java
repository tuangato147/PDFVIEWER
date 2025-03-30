package com.example.thunghiem;

import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PDFViewActivity extends AppCompatActivity implements OnLoadCompleteListener, OnPageChangeListener {
    private PDFView pdfView;
    private Uri pdfUri;
    private Integer pageNumber = 0;
    private TextView pageNumberText;
    private RelativeLayout controlsLayout;
    private boolean isNightMode = false;
    private FloatingActionButton nightModeButton;
    private ImageButton backButton;
    private int totalPages = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_pdf_view);
        hideSystemUI();
        initViews();
        setupListeners();

        pdfUri = getIntent().getData();
        if (pdfUri != null) {
            displayPdf();
        } else {
            Toast.makeText(this, "Không thể mở file PDF", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        pdfView = findViewById(R.id.pdfView);
        pageNumberText = findViewById(R.id.pageNumberText);
        controlsLayout = findViewById(R.id.controlsLayout);
        nightModeButton = findViewById(R.id.nightModeButton);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        nightModeButton.setOnClickListener(v -> toggleNightMode());
        backButton.setOnClickListener(v -> onBackPressed());

        // Ẩn/hiện controls khi tap vào PDF
        pdfView.setOnClickListener(v -> {
            if (controlsLayout.getVisibility() == View.VISIBLE) {
                controlsLayout.animate().alpha(0f).setDuration(200)
                        .withEndAction(() -> controlsLayout.setVisibility(View.GONE));
            } else {
                controlsLayout.setVisibility(View.VISIBLE);
                controlsLayout.setAlpha(0f);
                controlsLayout.animate().alpha(1f).setDuration(200);
            }
        });
    }

    private void toggleNightMode() {
        isNightMode = !isNightMode;
        displayPdf();
        updateNightModeButton();
    }

    private void updateNightModeButton() {
        nightModeButton.setImageResource(isNightMode ?
                R.drawable.ic_day_mode : R.drawable.ic_night_mode);
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(getWindow(), decorView);

        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(flags);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    private void displayPdf() {
        try {
            pdfView.fromUri(pdfUri)
                    .defaultPage(pageNumber)
                    .onPageChange(this)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .enableDoubletap(true)
                    .enableAnnotationRendering(true)
                    .password(null)
                    .enableAntialiasing(true)
                    .autoSpacing(true)
                    .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.WIDTH)
                    .pageSnap(true)
                    .pageFling(true)
                    .nightMode(isNightMode)
                    .load();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi mở PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        totalPages = pageCount;
        updatePageNumber();
    }

    private void updatePageNumber() {
        if (pageNumberText != null) {
            pageNumberText.setText(String.format("%d / %d", pageNumber + 1, totalPages));
        }
    }

    @Override
    public void loadComplete(int nbPages) {
        totalPages = nbPages;
        updatePageNumber();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideSystemUI();
    }
}