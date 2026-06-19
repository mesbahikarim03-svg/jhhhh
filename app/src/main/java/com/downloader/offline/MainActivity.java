
package com.downloader.offline;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private Button btnFetch, btnDownload;
    private TextView tvStatus;
    private String fileUrl = "";
    private String fileName = "downloaded_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // تهيئة مكتبة التحميل السريع
        PRDownloader.initialize(getApplicationContext());

        etUrl = findViewById(R.id.etUrl);
        btnFetch = findViewById(R.id.btnFetch);
        btnDownload = findViewById(R.id.btnDownload);
        tvStatus = findViewById(R.id.tvStatus);

        btnFetch.setOnClickListener(v -> {
            fileUrl = etUrl.getText().toString().trim();
            if (!fileUrl.isEmpty()) {
                detectLinkContentType(fileUrl);
            } else {
                Toast.makeText(MainActivity.this, "الرجاء إدخال رابط أولاً", Toast.LENGTH_SHORT).show();
            }
        });

        btnDownload.setOnClickListener(v -> startDownloading());
    }

    // فحص الرابط ومعرفة نوع المحتوى مباشرة من الهاتف
    private void detectLinkContentType(String url) {
        tvStatus.setText("جاري فحص الرابط...");
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).head().build(); // جلب معلومات الرابط فقط بدون تحميله بالكامل أولاً

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvStatus.setText("فشل الاتصال بالرابط، تأكد من صحته."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String contentType = response.header("Content-Type");
                    runOnUiThread(() -> {
                        if (contentType != null) {
                            tvStatus.setText("تم التعرف على المحتوى: " + contentType);
                            // توليد اسم امتداد للملف بناءً على النوع
                            if (contentType.contains("video")) fileName = "video_" + System.currentTimeMillis() + ".mp4";
                            else if (contentType.contains("audio")) fileName = "audio_" + System.currentTimeMillis() + ".mp3";
                            else if (contentType.contains("image")) fileName = "image_" + System.currentTimeMillis() + ".jpg";
                            else fileName = "file_" + System.currentTimeMillis();
                            
                            btnDownload.setVisibility(View.VISIBLE);
                        } else {
                            tvStatus.setText("تم العثور على رابط مباشر ولكن لم يتم تحديد النوع بدقة.");
                            btnDownload.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    // بدء عملية التحميل
    private void startDownloading() {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        tvStatus.setText("جاري التحميل في مجلد Downloads...");

        PRDownloader.download(fileUrl, downloadPath, fileName)
                .build()
                .start(new com.downloader.OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        tvStatus.setText("تم التحميل بنجاح وحفظ الملف باسم: " + fileName);
                        btnDownload.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(com.downloader.Error error) {
                        tvStatus.setText("حدث خطأ أثناء تحميل الملف.");
                    }
                });
    }
                                                      }
