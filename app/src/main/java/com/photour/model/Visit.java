package com.photour.model;

import android.os.Parcelable;
import androidx.room.ColumnInfo;
import com.google.auto.value.AutoValue;
import com.google.auto.value.AutoValue.CopyAnnotations;

@AutoValue
public abstract class Visit implements Parcelable {

  @CopyAnnotations
  @ColumnInfo(name = "visit_title")
  public abstract String visitTitle();

  @CopyAnnotations
  @ColumnInfo(name = "file_path")
  public abstract String visitCover();

  @CopyAnnotations
  @ColumnInfo(name = "total_photos")
  public abstract int totalPhotos();

  public static Visit create(String visitTitle, String visitCover, int totalPhotos) {
    return new AutoValue_Visit(visitTitle, visitCover, totalPhotos);
  }
}
