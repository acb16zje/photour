package com.android.photour.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.android.photour.model.ImageElement;

import java.util.Date;
import java.util.List;

@Dao
public interface ImageDao {
  @Query("SELECT * FROM image_element ORDER BY date DESC")
  LiveData<List<ImageElement>> getAll();

  @Query("SELECT * FROM image_element WHERE id IN (:ids)")
  LiveData<List<ImageElement>> loadAllByIds(int[] ids);

  @Query("SELECT * FROM image_element WHERE trip_name LIKE :trip")
  LiveData<List<ImageElement>> findByTrip(String trip);

  @Query("SELECT * FROM image_element WHERE date LIKE :date")
  LiveData<List<ImageElement>> findByDate(Date date);

  @Insert
  void insertImages(ImageElement... images);

  @Delete
  void delete(ImageElement image);

  @Query("DELETE FROM image_element")
  void deleteAll();
}
