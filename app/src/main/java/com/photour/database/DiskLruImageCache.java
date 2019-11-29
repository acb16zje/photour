package com.photour.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import com.jakewharton.disklrucache.DiskLruCache;
import com.photour.BuildConfig;
import com.photour.helper.CacheHelper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A class for creating and storing disk cache
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class DiskLruImageCache {

  private DiskLruCache mDiskCache;
  private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
  private static final int APP_VERSION = 1;
  private static final int VALUE_COUNT = 1;
  private static final String TAG = "DiskLruImageCache";

  public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize) {
    try {
      final File diskCacheDir = getDiskCacheDir(context, uniqueName);
      mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
//      mCompressFormat = compressFormat;
//      mCompressQuality = quality;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor) throws IOException {
    try (OutputStream out = new BufferedOutputStream(editor.newOutputStream(0),
        CacheHelper.IO_BUFFER_SIZE)) {
      int mCompressQuality = 70;
      return bitmap.compress(mCompressFormat, mCompressQuality, out);
    }

//    try {
//      out = new BufferedOutputStream( editor.newOutputStream( 0 ), CacheHelper.IO_BUFFER_SIZE );
//      int mCompressQuality = 70;
//      return bitmap.compress( mCompressFormat, mCompressQuality, out );
//    } finally {
//      if ( out != null ) {
//        out.close();
//      }
//    }
  }

  private File getDiskCacheDir(Context context, String uniqueName) {

    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
    // otherwise use internal cache dir
    final String cachePath =
        Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
            !CacheHelper.isExternalStorageRemovable() ?
            CacheHelper.getExternalCacheDir(context).getPath() :
            context.getCacheDir().getPath();

    return new File(cachePath + File.separator + uniqueName);
  }

  public void put(String key, Bitmap data) {

    DiskLruCache.Editor editor = null;
    try {
      editor = mDiskCache.edit(key);
      if (editor == null) {
        return;
      }

      if (writeBitmapToFile(data, editor)) {
        mDiskCache.flush();
        editor.commit();
        if (BuildConfig.DEBUG) {
          Log.d("cache_test_DISK_", "image put on disk cache " + key);
        }
      } else {
        editor.abort();
        if (BuildConfig.DEBUG) {
          Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
        }
      }
    } catch (IOException e) {
      if (BuildConfig.DEBUG) {
        Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
      }
      try {
        if (editor != null) {
          editor.abort();
        }
      } catch (IOException ignored) {
      }
    }

  }

  public Bitmap getBitmap(String key) {

    Bitmap bitmap = null;
    try (DiskLruCache.Snapshot snapshot = mDiskCache.get(key)) {

      if (snapshot == null) {
        return null;
      }
      final InputStream in = snapshot.getInputStream(0);
      if (in != null) {
        final BufferedInputStream buffIn = new BufferedInputStream(in, CacheHelper.IO_BUFFER_SIZE);
        bitmap = BitmapFactory.decodeStream(buffIn);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

//    try {
//
//      snapshot = mDiskCache.get(key);
//      if (snapshot == null) {
//        return null;
//      }
//      final InputStream in = snapshot.getInputStream(0);
//      if (in != null) {
//        final BufferedInputStream buffIn =
//            new BufferedInputStream(in, CacheHelper.IO_BUFFER_SIZE);
//        bitmap = BitmapFactory.decodeStream(buffIn);
//      }
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//      if (snapshot != null) {
//        snapshot.close();
//      }
//    }

    if (BuildConfig.DEBUG) {
      Log.d("cache_test_DISK_", bitmap == null ? "" : "image read from disk " + key);
    }

    return bitmap;

  }

  public boolean containsKey(String key) {

    boolean contained = false;
    try (DiskLruCache.Snapshot snapshot = mDiskCache.get(key)) {
      contained = snapshot != null;
    } catch (IOException e) {
      e.printStackTrace();
    }

//    try {
//      snapshot = mDiskCache.get(key);
//      contained = snapshot != null;
//    } catch (IOException e) {
//      e.printStackTrace();
//    } finally {
//      if (snapshot != null) {
//        snapshot.close();
//      }
//    }

    return contained;

  }

  public void clearCache() {
    if (BuildConfig.DEBUG) {
      Log.d("cache_test_DISK_", "disk cache CLEARED");
    }
    try {
      mDiskCache.delete();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public File getCacheFolder() {
    return mDiskCache.getDirectory();
  }

}