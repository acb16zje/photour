package com.photour.ui.visit;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.BitmapDescriptor;
import com.google.android.libraries.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.maps.model.JointType;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.photour.MainActivity;
import com.photour.R;
import com.photour.databinding.FragmentVisitBinding;
import com.photour.helper.AlertDialogHelper;
import com.photour.helper.PermissionHelper;
import com.photour.helper.ToastHelper;
import com.photour.model.Photo;
import com.photour.ui.visit.VisitFragmentDirections.ActionEditVisit;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for ViewVisit page
 *
 * @author Zer Jun Eng, Jia Hua Ng
 */
public class VisitFragment extends Fragment implements OnMapReadyCallback {

  private PermissionHelper permissionHelper;

  private FragmentVisitBinding binding;
  private Activity activity;

  private GoogleMap googleMap;
  private VisitViewModel visitViewModel;
  private ViewPager2 mViewPager;
  private VisitAdapter visitAdapter;
  private int currentPos = -1;
  private List<Marker> markerList = new ArrayList<>();

  /**
   * Called to do initial creation of a fragment.  This is called after {@link #onAttach(Activity)}
   * and before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state, this
   * is the state.
   */
  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);

    activity = getActivity();
    permissionHelper = PermissionHelper.getStoragePermissionHelper(activity, this);

    if (savedInstanceState != null) {
      currentPos = savedInstanceState.getInt("pageItem", 0);
    }

    visitViewModel = new ViewModelProvider(this).get(VisitViewModel.class);
  }

  /**
   * Called to have the fragment instantiate its user interface view. Visit argument is parsed here
   * and loads photos.
   *
   * @param inflater The LayoutInflater object that can be used to inflate any views in the
   * fragment,
   * @param container If non-null, this is the parent view that the fragment's UI should be attached
   * to.  The fragment should not add the view itself, but this can be used to generate the
   * LayoutParams of the view.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   * @return View Return the View for the fragment's UI, or null.
   */
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    super.onCreateView(inflater, container, savedInstanceState);

    binding = FragmentVisitBinding.inflate(inflater, container, false);
    binding.setLifecycleOwner(this);
    binding.setViewModel(visitViewModel);

    if (getArguments() != null) {
      visitViewModel.initVisit(VisitFragmentArgs.fromBundle(getArguments()).getVisit());
    }

    visitViewModel.liveVisit.observe(getViewLifecycleOwner(), visit -> {
      visitViewModel.visit = visit;
      ((MainActivity) activity).setToolbarTitle(visit.visitTitle());
    });

    return binding.getRoot();
  }

  /**
   * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned,
   * but before any saved state has been restored in to the view.
   *
   * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
   * saved state as given here.
   */
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    if (visitViewModel.visit != null) {
      initializeViewPager();
    }
  }

  /**
   * Called when fragment is initialised or resumed. Checks if has storage permission else exit to
   * previous fragment
   */
  @Override
  public void onResume() {
    super.onResume();

    // User might have revoked the permission
    if (!permissionHelper.hasStoragePermission()) {
      Navigation.findNavController(binding.getRoot()).navigateUp();
    }
  }

  /**
   * Called when the Fragment is no longer resumed.  This is generally tied to Activity.onPause of
   * the containing Activity's lifecycle.
   */
  @Override
  public void onPause() {
    super.onPause();
    currentPos = mViewPager.getCurrentItem();
  }

  /**
   * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
   * in a new instance of its process is restarted.  If a new instance of the fragment later needs
   * to be created, the data placed in the Bundle here will be available in the Bundle given to
   * {@link #onCreate(Bundle)}, {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and {@link
   * #onActivityCreated(Bundle)}.
   *
   * @param outState Bundle in which to place your saved state.
   */
  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt("pageItem", mViewPager.getCurrentItem());
    super.onSaveInstanceState(outState);
  }

  /**
   * Helper function to initialise ViewPager and observe image in ViewModel
   */
  private void initializeViewPager() {
    inflateMap();

    mViewPager = binding.imageScroll;
    visitAdapter = new VisitAdapter();
    mViewPager.setAdapter(visitAdapter);

    visitViewModel.photos.observe(getViewLifecycleOwner(), this::resetViewPager);
  }

  /**
   * Helper function to reset viewPager when dataset changes Adds listener to ViewPager to update
   * details onPageChange
   *
   * @param photos List of {@link Photo}
   */
  private void resetViewPager(List<Photo> photos) {
    if (photos == null || photos.isEmpty()) {
      visitViewModel.setPlaceholderText(false);
      visitViewModel.setDetails(-1);
    } else {
      initialiseMarker(photos);
      visitAdapter.setItems(photos);
      visitAdapter.notifyDataSetChanged();
      visitViewModel.setPlaceholderText(true);
      mViewPager.registerOnPageChangeCallback(callback);
      mViewPager.setCurrentItem(currentPos, false);
    }
  }

  /**
   * Inflate the ViewStub, then initialise the Google Map
   */
  private void inflateMap() {
    // Inflate MapFragment lite mode. Lite mode only work when using ViewStub to inflate it
    ViewStub viewStub = binding.viewstubMap.getViewStub();
    if (viewStub != null) {
      viewStub.inflate();
    }

    SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager()
        .findFragmentById(R.id.map_lite_fragment);

    if (supportMapFragment != null) {
      // Disable click events in lite mode, prevent opening Google Maps
      View mapview = supportMapFragment.getView();
      if (mapview != null) {
        supportMapFragment.getView().setClickable(false);
      }

      supportMapFragment.getMapAsync(this);
    }
  }

  /**
   * Called when the map is ready to be used.
   *
   * @param googleMap A non-null instance of a GoogleMap associated with the MapFragment or MapView
   * that defines the callback.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    this.googleMap = googleMap;
    this.googleMap.getUiSettings().setMapToolbarEnabled(false);

    initialisePolyLine();
  }

  /**
   * Function to initialise polyline to indicate visit path Starts with getting LatLngBounds for the
   * camera to be able to fit in the entire visit Finally add all LatLng into PolyLine Object to
   * draw it on the map
   */
  private void initialisePolyLine() {
    List<LatLng> polyLine = visitViewModel.visit.latLngList();

    // Edge case: latLngList is null or empty
    if (polyLine == null || polyLine.isEmpty()) {
      return;
    }

    LatLngBounds.Builder builder = new LatLngBounds.Builder();
    for (LatLng latLng : polyLine) {
      builder.include(latLng);
    }

    LatLngBounds bounds = builder.build();
    this.googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

    PolylineOptions options = new PolylineOptions()
        .width(5)
        .color(Color.rgb(190, 41, 236))
        .jointType(JointType.BEVEL)
        .addAll(polyLine);
    this.googleMap.addPolyline(options);
  }

  /**
   * Add in all the markers of the photo on the map The markers are then added into a list for
   * future reference
   *
   * @param photos The list of photos in the visit to be added as marker
   */

  private void initialiseMarker(List<Photo> photos) {
    for (Photo photo : photos) {
      Marker marker = this.googleMap.addMarker(new MarkerOptions().position(photo.latLng()));
      marker.setTag(photo.id());
      markerList.add(marker);
    }
  }

  /**
   * Helper function to set the marker for Photo when the ViewPager is scrolled
   *
   * @param id id parsed from Photo
   */
  private void setMarker(int id) {
    for (Marker marker : markerList) {
      boolean photoEqualsMarkerTag = id == (int) marker.getTag();

      marker.setZIndex(photoEqualsMarkerTag ? 1f : 0);

      if (photoEqualsMarkerTag) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
      } else {
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.unfocused_marker);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);
        BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        marker.setIcon(smallMarkerIcon);
      }

    }
  }

  /**
   * Initialize the contents of the Fragment host's standard options menu.
   *
   * @param menu The options menu in which you place your items.
   * @see #setHasOptionsMenu
   * @see #onPrepareOptionsMenu
   * @see #onOptionsItemSelected
   */
  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    menu.clear();
    activity.getMenuInflater().inflate(R.menu.menu_visit, menu);
  }

  /**
   * This hook is called whenever an item in options menu is selected.
   *
   * @param item The menu item that was selected.
   * @return boolean Return false to allow normal menu processing to proceed, true to consume it
   * here.
   * @see #onCreateOptionsMenu
   */
  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int itemId = item.getItemId();

    switch (itemId) {
      /*
       * Cannot be edit_visit, {@see MainActivity#onOptionsItemSelected }
       */
      case R.id.edit_visit_option:
        editVisit();
        break;
      case R.id.delete_visit:
        deleteVisit();
        break;
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Navigate to {@link EditVisitFragment}
   */
  private void editVisit() {
    ActionEditVisit actionEditVisit = VisitFragmentDirections.actionEditVisit(visitViewModel.visit);
    Navigation.findNavController(binding.getRoot()).navigate(actionEditVisit);
  }

  /**
   * Delete the current visit. If the deletion is successful, navigate back to {@link
   * com.photour.ui.visits.VisitsFragment}
   */
  private void deleteVisit() {
    AlertDialogHelper.createDeleteConfirmationDialog(activity, () -> {
      if (visitViewModel.deleteVisit()) {
        Navigation.findNavController(binding.getRoot()).navigateUp();
        ToastHelper.tShort(activity, "Visit deleted");
      } else {
        ToastHelper.tShort(activity, "Failed to delete");
      }
    });
  }

  /**
   * Callback on page scroll on ViewPager Details for the image are parsed through ViewModel and
   * marker for the image is set on the map.
   */
  private ViewPager2.OnPageChangeCallback callback = new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
      super.onPageSelected(position);
      int id = visitViewModel.setDetails(position);
      setMarker(id);
    }
  };
}
