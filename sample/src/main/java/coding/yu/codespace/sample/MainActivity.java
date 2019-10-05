package coding.yu.codespace.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ImageView imageView = findViewById(R.id.image);
//        int id = Resources.getSystem().getIdentifier("text_select_handle_left", "drawable", "android");
//        Log.e("Yu", "id:" + id);
//        imageView.setImageResource(id);
    }
}
