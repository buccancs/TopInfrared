package com.topinfrared.tc001.standalone.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.topinfrared.tc001.standalone.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemLocalFileBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final CheckBox cbFileSelect;

  @NonNull
  public final ImageView ivFileType;

  @NonNull
  public final ImageView ivThumbnail;

  @NonNull
  public final TextView tvFileDate;

  @NonNull
  public final TextView tvFileName;

  @NonNull
  public final TextView tvFileSize;

  @NonNull
  public final TextView tvFileType;

  private ItemLocalFileBinding(@NonNull CardView rootView, @NonNull CheckBox cbFileSelect,
      @NonNull ImageView ivFileType, @NonNull ImageView ivThumbnail, @NonNull TextView tvFileDate,
      @NonNull TextView tvFileName, @NonNull TextView tvFileSize, @NonNull TextView tvFileType) {
    this.rootView = rootView;
    this.cbFileSelect = cbFileSelect;
    this.ivFileType = ivFileType;
    this.ivThumbnail = ivThumbnail;
    this.tvFileDate = tvFileDate;
    this.tvFileName = tvFileName;
    this.tvFileSize = tvFileSize;
    this.tvFileType = tvFileType;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemLocalFileBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemLocalFileBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_local_file, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemLocalFileBinding bind(@NonNull View rootView) {
    int id;
    missingId: {
      id = R.id.cbFileSelect;
      CheckBox cbFileSelect = ViewBindings.findChildViewById(rootView, id);
      if (cbFileSelect == null) {
        break missingId;
      }

      id = R.id.ivFileType;
      ImageView ivFileType = ViewBindings.findChildViewById(rootView, id);
      if (ivFileType == null) {
        break missingId;
      }

      id = R.id.ivThumbnail;
      ImageView ivThumbnail = ViewBindings.findChildViewById(rootView, id);
      if (ivThumbnail == null) {
        break missingId;
      }

      id = R.id.tvFileDate;
      TextView tvFileDate = ViewBindings.findChildViewById(rootView, id);
      if (tvFileDate == null) {
        break missingId;
      }

      id = R.id.tvFileName;
      TextView tvFileName = ViewBindings.findChildViewById(rootView, id);
      if (tvFileName == null) {
        break missingId;
      }

      id = R.id.tvFileSize;
      TextView tvFileSize = ViewBindings.findChildViewById(rootView, id);
      if (tvFileSize == null) {
        break missingId;
      }

      id = R.id.tvFileType;
      TextView tvFileType = ViewBindings.findChildViewById(rootView, id);
      if (tvFileType == null) {
        break missingId;
      }

      return new ItemLocalFileBinding((CardView) rootView, cbFileSelect, ivFileType, ivThumbnail,
          tvFileDate, tvFileName, tvFileSize, tvFileType);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
