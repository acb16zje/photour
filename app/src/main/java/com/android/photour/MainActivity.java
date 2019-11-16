package com.android.photour;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import static android.media.CamcorderProfile.get;

/**
 * Main activity of the application
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class MainActivity extends AppCompatActivity {

  private LruCache<String, Bitmap> memoryCache;
  private LiveData<NavController> currentNavController;
  private AppBarConfiguration appBarConfiguration;
  private BottomNavExtension navView;
  private Toolbar toolbar;
  private static final int REQUEST_READ_EXTERNAL_STORAGE = 2987;
  private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 7829;


  /**
   * Perform the required actions when the activity is created
   *
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   * down then this Bundle contains the data it most recently supplied in
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    checkStoragePermissions(this);

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;

    memoryCache = new LruCache<String, Bitmap>(cacheSize) {
      @Override
      protected int sizeOf(String key, Bitmap bitmap) {
        return bitmap.getByteCount() / 1024;
      }
    };

    if (savedInstanceState == null) {
      setupBottomNavigationBar();
    }
  }

  /**
   * This method is called after {@link #onStart} when the activity is being re-initialized from a
   * previously saved state. Performs a restore of any view state that had previously been frozen by
   * {@link #onSaveInstanceState}.
   *
   * @param savedInstanceState The data most recently supplied in {@link #onSaveInstanceState}.
   */
  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    setupBottomNavigationBar();
  }

  /**
   * Called on first creation and when restoring state.
   *
   * Setups the bottomnavbar as well as the navcontroller for the fragment
   */
  private void setupBottomNavigationBar() {
    navView = findViewById(R.id.nav_view);
    List<Integer> navGraphIds = new ArrayList<>();
    navGraphIds.add(R.navigation.navigation_visit);
    navGraphIds.add(R.navigation.navigation_photos);
    navGraphIds.add(R.navigation.navigation_paths);
    navGraphIds.add(R.navigation.navigation_settings);

    LiveData<NavController> controller = navView.setupWithNavController(
        navGraphIds,
        this.getSupportFragmentManager(),
        R.id.nav_host_fragment,
        this.getIntent()
    );

    final Observer<NavController> navControllerObserver = navController -> {
      navController.addOnDestinationChangedListener((controller1, destination, arguments) -> {
        if (destination.getId() == R.id.action_settings) {
          navView.setVisibility(View.GONE);
        } else {
          navView.setVisibility(View.VISIBLE);
        }
      });

      appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
      NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    };

    controller.observe(this, navControllerObserver);
    currentNavController = controller;
  }

  /**
   * Initialize the contents of the Activity's standard options menu.
   *
   * @param menu The options menu in which you place your items.
   * @return boolean Must return true for the menu to be displayed; if return false it will not be
   * shown.
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  /**
   * This hook is called whenever an item in your options menu is selected.
   *
   * @param item The menu item that was selected
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   */
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    /*
      Have the NavigationUI look for an action or destination matching the menu
      item id and navigate there if found.
      Otherwise, bubble up to the parent.
     */
    return NavigationUI.onNavDestinationSelected(item,
        Navigation.findNavController(this, R.id.nav_host_fragment))
        || super.onOptionsItemSelected(item);
  }

  /**
   * This method is called whenever the user chooses to navigate Up within the application's
   * activity hierarchy from the action bar.
   *
   * @return boolean True if Up navigation completed successfully and this Activity was finished,
   * false otherwise.
   */
  @Override
  public boolean onSupportNavigateUp() {
    // Have NavigationUI handle up behavior in the ActionBar
    return NavigationUI
        .navigateUp(Objects.requireNonNull(currentNavController.getValue()), appBarConfiguration)
        || super.onSupportNavigateUp();
  }

  /**
   * Sets the visibility of top toolbar (Action bar)
   *
   * @param visible True if the top toolbar (Action bar) should be visible
   */
  public void setToolbarVisibility(boolean visible) {
    toolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  /**
   * Called to process touch screen event. Overridden for custom event handling
   *
   * @param ev The touch screen event
   * @return boolean Return true if this event was consumed.
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      View v = getCurrentFocus();

      // Clear focus of TextInputEditText and hide keyboard when tapped outside
      if (v instanceof TextInputEditText) {
        Rect outRect = new Rect();
        v.getGlobalVisibleRect(outRect);
        if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
          v.clearFocus();
          InputMethodManager imm = (InputMethodManager) getSystemService(
              Context.INPUT_METHOD_SERVICE);
          assert imm != null;
          imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
      }
    }

    return super.dispatchTouchEvent(ev);
  }

  private void checkStoragePermissions(final Context context) {
    int currentAPIVersion = Build.VERSION.SDK_INT;
    if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
          androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
          alertBuilder.setCancelable(true);
          alertBuilder.setTitle("Permission necessary");
          alertBuilder.setMessage("External storage permission is necessary");
          alertBuilder.setPositiveButton(android.R.string.yes, new android.content.DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onClick(DialogInterface dialog, int which) {
              ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
          });
          androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
          alert.show();

        } else {
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }

      }
      if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
          androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
          alertBuilder.setCancelable(true);
          alertBuilder.setTitle("Permission necessary");
          alertBuilder.setMessage("Writing external storage permission is necessary");
          alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) ->
            ActivityCompat.requestPermissions((Activity) context,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              REQUEST_WRITE_EXTERNAL_STORAGE));
          androidx.appcompat.app.AlertDialog alert = alertBuilder.create();
          alert.show();

        } else {
          ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }

      }

    }
  }

  public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
      memoryCache.put(key, bitmap);
    }
  }

  public Bitmap getBitmapFromMemCache(String key) {
    return memoryCache.get(key);
  }
}
