package com.android.photour.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.android.photour.MainActivity;
import com.android.photour.R;
import java.util.Objects;

public class SettingsFragment extends Fragment {

  private SettingsViewModel settingsViewModel;

  public View onCreateView(@NonNull LayoutInflater inflater,
      ViewGroup container, Bundle savedInstanceState) {
    settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    View root = inflater.inflate(R.layout.fragment_settings, container, false);
    final TextView textView = root.findViewById(R.id.text_notifications);
    settingsViewModel.getText().observe(this, textView::setText);
    return root;
  }
}
