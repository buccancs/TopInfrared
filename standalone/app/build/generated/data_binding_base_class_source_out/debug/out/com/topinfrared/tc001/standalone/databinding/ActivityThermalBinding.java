package com.topinfrared.tc001.standalone.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.topinfrared.tc001.standalone.R;
import com.topinfrared.tc001.standalone.ui.ThermalOverlayView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityThermalBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnBack;

  @NonNull
  public final Button btnCapture;

  @NonNull
  public final Button btnClearHistory;

  @NonNull
  public final Button btnDngRawL3;

  @NonNull
  public final Button btnParallelRec;

  @NonNull
  public final Button btnRecord;

  @NonNull
  public final Button btnSamsung4K;

  @NonNull
  public final Button btnTempModeArea;

  @NonNull
  public final Button btnTempModeLine;

  @NonNull
  public final Button btnTempModePoint;

  @NonNull
  public final Button btnToggleGradient;

  @NonNull
  public final Button btnToggleHistory;

  @NonNull
  public final LinearLayout enhancedRecordingStatus;

  @NonNull
  public final ImageView ivThermalView;

  @NonNull
  public final ProgressBar progressLoading;

  @NonNull
  public final LinearLayout thermalControls;

  @NonNull
  public final ThermalOverlayView thermalOverlay;

  @NonNull
  public final TextView tvDngRawStatus;

  @NonNull
  public final TextView tvRecordingDuration;

  @NonNull
  public final TextView tvSamsung4KStatus;

  private ActivityThermalBinding(@NonNull ConstraintLayout rootView, @NonNull Button btnBack,
      @NonNull Button btnCapture, @NonNull Button btnClearHistory, @NonNull Button btnDngRawL3,
      @NonNull Button btnParallelRec, @NonNull Button btnRecord, @NonNull Button btnSamsung4K,
      @NonNull Button btnTempModeArea, @NonNull Button btnTempModeLine,
      @NonNull Button btnTempModePoint, @NonNull Button btnToggleGradient,
      @NonNull Button btnToggleHistory, @NonNull LinearLayout enhancedRecordingStatus,
      @NonNull ImageView ivThermalView, @NonNull ProgressBar progressLoading,
      @NonNull LinearLayout thermalControls, @NonNull ThermalOverlayView thermalOverlay,
      @NonNull TextView tvDngRawStatus, @NonNull TextView tvRecordingDuration,
      @NonNull TextView tvSamsung4KStatus) {
    this.rootView = rootView;
    this.btnBack = btnBack;
    this.btnCapture = btnCapture;
    this.btnClearHistory = btnClearHistory;
    this.btnDngRawL3 = btnDngRawL3;
    this.btnParallelRec = btnParallelRec;
    this.btnRecord = btnRecord;
    this.btnSamsung4K = btnSamsung4K;
    this.btnTempModeArea = btnTempModeArea;
    this.btnTempModeLine = btnTempModeLine;
    this.btnTempModePoint = btnTempModePoint;
    this.btnToggleGradient = btnToggleGradient;
    this.btnToggleHistory = btnToggleHistory;
    this.enhancedRecordingStatus = enhancedRecordingStatus;
    this.ivThermalView = ivThermalView;
    this.progressLoading = progressLoading;
    this.thermalControls = thermalControls;
    this.thermalOverlay = thermalOverlay;
    this.tvDngRawStatus = tvDngRawStatus;
    this.tvRecordingDuration = tvRecordingDuration;
    this.tvSamsung4KStatus = tvSamsung4KStatus;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityThermalBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityThermalBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_thermal, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityThermalBinding bind(@NonNull View rootView) {
    int id;
    missingId: {
      id = R.id.btnBack;
      Button btnBack = ViewBindings.findChildViewById(rootView, id);
      if (btnBack == null) {
        break missingId;
      }

      id = R.id.btnCapture;
      Button btnCapture = ViewBindings.findChildViewById(rootView, id);
      if (btnCapture == null) {
        break missingId;
      }

      id = R.id.btnClearHistory;
      Button btnClearHistory = ViewBindings.findChildViewById(rootView, id);
      if (btnClearHistory == null) {
        break missingId;
      }

      id = R.id.btnDngRawL3;
      Button btnDngRawL3 = ViewBindings.findChildViewById(rootView, id);
      if (btnDngRawL3 == null) {
        break missingId;
      }

      id = R.id.btnParallelRec;
      Button btnParallelRec = ViewBindings.findChildViewById(rootView, id);
      if (btnParallelRec == null) {
        break missingId;
      }

      id = R.id.btnRecord;
      Button btnRecord = ViewBindings.findChildViewById(rootView, id);
      if (btnRecord == null) {
        break missingId;
      }

      id = R.id.btnSamsung4K;
      Button btnSamsung4K = ViewBindings.findChildViewById(rootView, id);
      if (btnSamsung4K == null) {
        break missingId;
      }

      id = R.id.btnTempModeArea;
      Button btnTempModeArea = ViewBindings.findChildViewById(rootView, id);
      if (btnTempModeArea == null) {
        break missingId;
      }

      id = R.id.btnTempModeLine;
      Button btnTempModeLine = ViewBindings.findChildViewById(rootView, id);
      if (btnTempModeLine == null) {
        break missingId;
      }

      id = R.id.btnTempModePoint;
      Button btnTempModePoint = ViewBindings.findChildViewById(rootView, id);
      if (btnTempModePoint == null) {
        break missingId;
      }

      id = R.id.btnToggleGradient;
      Button btnToggleGradient = ViewBindings.findChildViewById(rootView, id);
      if (btnToggleGradient == null) {
        break missingId;
      }

      id = R.id.btnToggleHistory;
      Button btnToggleHistory = ViewBindings.findChildViewById(rootView, id);
      if (btnToggleHistory == null) {
        break missingId;
      }

      id = R.id.enhancedRecordingStatus;
      LinearLayout enhancedRecordingStatus = ViewBindings.findChildViewById(rootView, id);
      if (enhancedRecordingStatus == null) {
        break missingId;
      }

      id = R.id.ivThermalView;
      ImageView ivThermalView = ViewBindings.findChildViewById(rootView, id);
      if (ivThermalView == null) {
        break missingId;
      }

      id = R.id.progressLoading;
      ProgressBar progressLoading = ViewBindings.findChildViewById(rootView, id);
      if (progressLoading == null) {
        break missingId;
      }

      id = R.id.thermalControls;
      LinearLayout thermalControls = ViewBindings.findChildViewById(rootView, id);
      if (thermalControls == null) {
        break missingId;
      }

      id = R.id.thermalOverlay;
      ThermalOverlayView thermalOverlay = ViewBindings.findChildViewById(rootView, id);
      if (thermalOverlay == null) {
        break missingId;
      }

      id = R.id.tvDngRawStatus;
      TextView tvDngRawStatus = ViewBindings.findChildViewById(rootView, id);
      if (tvDngRawStatus == null) {
        break missingId;
      }

      id = R.id.tvRecordingDuration;
      TextView tvRecordingDuration = ViewBindings.findChildViewById(rootView, id);
      if (tvRecordingDuration == null) {
        break missingId;
      }

      id = R.id.tvSamsung4KStatus;
      TextView tvSamsung4KStatus = ViewBindings.findChildViewById(rootView, id);
      if (tvSamsung4KStatus == null) {
        break missingId;
      }

      return new ActivityThermalBinding((ConstraintLayout) rootView, btnBack, btnCapture,
          btnClearHistory, btnDngRawL3, btnParallelRec, btnRecord, btnSamsung4K, btnTempModeArea,
          btnTempModeLine, btnTempModePoint, btnToggleGradient, btnToggleHistory,
          enhancedRecordingStatus, ivThermalView, progressLoading, thermalControls, thermalOverlay,
          tvDngRawStatus, tvRecordingDuration, tvSamsung4KStatus);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
