package com.example.barcodereader;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutTextView = findViewById(R.id.aboutTextView);
        aboutTextView.setText(
                Html.fromHtml(
                        "Barcode Reader<br><br>" +
                                "Versioni: 1.0<br><br>" +
                                "Zhvilluar nga: Veli Prenku<br><br>" +
                                "Ky aplikacion përdoret për:<br>" +
                                "- Skanimin e barkodeve<br>" +
                                "- Eksportimin e të dhënave në Excel<br>" +
                                "- Gjenerimin e raporteve<br><br>" +
                                "Faleminderit që përdorni aplikacionin tonë!<br><br>" +
                                "<a href=\"https://github.com/veliprenku?tab=repositories\">GitHub</a>"
                )
        );
        aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
