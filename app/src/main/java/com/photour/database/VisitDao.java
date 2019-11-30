package com.photour.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;
import com.google.android.libraries.maps.model.LatLng;
import com.photour.converter.LatLngListConverter;
import com.photour.model.Visit;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Visits database
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
@Dao
@TypeConverters({LatLngListConverter.class})
public interface VisitDao {

  @Insert
  long insert(Visit visit);

  @Query("UPDATE visits SET elapsedTime = :elapsedTime, latLngList = :latLngList WHERE id = :id")
  void update(final long id, final long elapsedTime, final ArrayList<LatLng> latLngList);

  @Query("SELECT * FROM visits")
  LiveData<List<Visit>> getAllVisits();

  @Query("SELECT * FROM visits INNER JOIN photos ON visits.id = photos.visitId")
  Visit findByImage();
}