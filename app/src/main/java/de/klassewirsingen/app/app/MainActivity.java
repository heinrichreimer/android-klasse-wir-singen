package de.klassewirsingen.app.app;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import com.orhanobut.logger.Logger;
import de.klassewirsingen.app.R;
import de.klassewirsingen.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    static final Uri HOME_URI = Uri.parse("http://klasse-wir-singen.de");

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.init("KWS");
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            Uri url;
            if (getIntent().getAction().equals(Intent.ACTION_VIEW) && getIntent().getData() != null) {
                url = getIntent().getData();
            } else {
                url = HOME_URI;
            }
            loadUrl(url);
        }
    }

    private void loadUrl(@NonNull Uri url) {
        Logger.d("Loading url: %s", url);

        WebFragment fragment = WebFragment.newInstance(url);


        getSupportFragmentManager().beginTransaction()
                .replace(binding.content.getId(), fragment)
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        super.onBackPressed();
    }
}
