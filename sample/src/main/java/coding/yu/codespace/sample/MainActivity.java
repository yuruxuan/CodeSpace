package coding.yu.codespace.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import coding.yu.codespace.CodeSpace;
import coding.yu.codespace.indicator.LineNumIndicator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CodeSpace codeSpace = findViewById(R.id.code_space);
        LineNumIndicator indicator = findViewById(R.id.line_indicator);
        codeSpace.bindLineIndicator(indicator);
    }
}
