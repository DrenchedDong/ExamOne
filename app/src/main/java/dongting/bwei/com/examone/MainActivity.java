package dongting.bwei.com.examone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import bwie.com.demo.newsdrag.ChannelActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

startActivity(new Intent(this, ChannelActivity.class));
finish();
    }
}
