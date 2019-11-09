package com.android.photour.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.android.photour.MainActivity;
import com.android.photour.R;
import java.util.Objects;

/**
 * Fragment that display the developers of this application
 *
 * @author Zer Jun Eng & Jia Hua Ng
 */
public class AboutFragment extends Fragment {

  public View onCreateView(
      @NonNull LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState
  ) {
    View root = inflater.inflate(R.layout.fragment_about, container, false);
    return root;
  }
}
