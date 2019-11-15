package com.android.photour.ui.photos;

import android.Manifest;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.android.photour.ImageElement;
import com.android.photour.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentUris.withAppendedId;

public class PhotosViewModel extends AndroidViewModel {

  // Statics for readwrite images
  private MutableLiveData<List<ImageElement>> _images = new MutableLiveData<List<ImageElement>>();
  public LiveData<List<ImageElement>> images = _images;
  static final List<ImageElement> ITEMS = new ArrayList<>();
  private ContentObserver contentObserver = null;
  private static final int QUERY_BY_DATE = 0;
  private static final int QUERY_BY_TRIPS = 1;

  public PhotosViewModel(@NonNull Application application) {
    super(application);
    loadImages();
  }

  /**
   * Helper function to recalculate number of columns
   *
   * @param context Context of application
   * @param columnWidthDp Size of column in dp
   * @return int number of columns
   */
  int calculateNoOfColumns(Context context, float columnWidthDp) {
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
    return (int) (screenWidthDp / columnWidthDp + 0.5);
  }

  private void loadImages() {
    List<ImageElement> imageList = queryImages(QUERY_BY_TRIPS);
    _images.postValue(imageList);

    if (contentObserver == null) {
        contentObserver = new ContentObserver(new Handler()) {
          @Override
          public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadImages();
          }
        };
       this.getApplication().getContentResolver().registerContentObserver(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, contentObserver);
    }
  }

  private List<ImageElement> queryImages(int config) {
    List<ImageElement> images = new ArrayList<>();

    String[] projection = new String[]{MediaStore.Images.Media._ID,
              MediaStore.Images.Media.DISPLAY_NAME,
              MediaStore.Images.Media.DATE_TAKEN,
              "_data"
    };
    String selection = null;
    if (config == QUERY_BY_DATE) {
      selection = MediaStore.Images.Media.DATE_TAKEN + " >= ?";
    } else if (config == QUERY_BY_TRIPS) {
      selection = "( _data LIKE ? )";
    }

    String[] selectionArgs = new String[] {"%Test%"}; //Change for folder name when fully completed

    String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    Cursor query = getApplication().getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
    );
    int i = 0;
    String previousPath = "";
    ImageElement imageElement = null;
    System.out.println(query.getCount());
    while (i < query.getCount()) {
      query.moveToPosition(i);
      String currentPath = getPath(query.getString(query.getColumnIndexOrThrow("_data")));
      long columnIndex = query.getLong(query.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
      Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columnIndex);
      if (!previousPath.equals(currentPath)) {
        System.out.println("new card: "+currentPath);
        if (imageElement != null) {
          images.add(imageElement);
        }
        imageElement = new ImageElement(currentPath);
        previousPath = currentPath;
      }
      imageElement.addUri(contentUri);
      i++;
    }
    images.add(imageElement);
    query.close();
    System.out.println(images);
    return images;
  }

  private String getPath(String dir) {
   String[] temp = dir.split("/");
   return temp[temp.length - 2];
  }
//  /**
//   *
//   * @param dir
//   * @param config 0 = By trips(folder), 1 = By Date
//   */
//  public static void loadSavedImages(File dir, int config) {
//    ITEMS.clear();
//
//
//    //by trips
//    if (config == 0) {
//      File[] files = dir.listFiles();
//      for (File file : files) {
//        if (file.isDirectory()) { // need to change to get SQL db in future
//          File[] images = file.listFiles();
//          List<File> tempFiles = new ArrayList<>();
//          for (File image : images) {
//            String absolutePath = image.getAbsolutePath();
//            String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
//            if (extension.equals(".jpg") || extension.equals(".png")) {
//              tempFiles.add(image);
//            }
//          }
//          ITEMS.add(new ImageElement(file.toString(), tempFiles));
//        }
//      }
//    } else if (config == 1) {
//      File[] files = dir.listFiles();
//      List<File> tempFiles = new ArrayList<>();
//      for (File file : files) {
//        if (file.isDirectory()) { // need to change to get SQL db in future
//          File[] images = file.listFiles();
//
//          for (File image : images) {
//            String absolutePath = image.getAbsolutePath();
//            String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
//            if (extension.equals(".jpg") || extension.equals(".png")) {
//              tempFiles.add(image);
//            }
//          }
//        }
//      }
//      Collections.sort(tempFiles, (object1, object2) ->
//              (int) ((object1.lastModified() > object2.lastModified()) ?
//                      object1.lastModified(): object2.lastModified()));
//      // to be completed (need to split array to dates)
//    }
//
//  }


  private List<File> createImageElement(File dir, List<File> tempFiles) {
    ImageElement imageElement = new ImageElement(dir.toString());
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File file : files) {
        String absolutePath = file.getAbsolutePath();
        String extension = absolutePath.substring(absolutePath.lastIndexOf("."));
        if (extension.equals(".jpg") || extension.equals(".png")) {
//          loadImage(file);
        }
      }
    }
    return null;
  }

}
