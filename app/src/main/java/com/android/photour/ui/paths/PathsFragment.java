package com.android.photour.ui.paths;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.android.photour.R;

public class PathsFragment extends Fragment {

  private PathsViewModel pathsViewModel;

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    pathsViewModel = new ViewModelProvider(this).get(PathsViewModel.class);
    View root = inflater.inflate(R.layout.fragment_paths, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    pathsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        textView.setText(s);
      }
    });
    return root;
  }
}
