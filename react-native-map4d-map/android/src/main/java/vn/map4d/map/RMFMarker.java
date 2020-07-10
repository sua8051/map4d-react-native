package vn.map4d.map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.view.ViewTreeObserver;
import android.graphics.drawable.Animatable;
import android.graphics.Bitmap;
import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.image.CloseableStaticBitmap;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.common.ResizeOptions;

import vn.map4d.map.core.*;
import vn.map4d.map.annotations.*;

import android.util.Log;

import vn.map4d.map.camera.*;
import vn.map4d.types.MFLocationCoordinate;
import vn.map4d.map.camera.MFCameraPosition;

import javax.annotation.Nullable;

public class RMFMarker extends RMFFeature {
  private MFMarkerOptions markerOptions;
  private MFLocationCoordinate position;
  private double rotation;
  private String title;
  private String snippet;
  private boolean draggable;
  private float zIndex;
  private boolean visible;
  private float windowAnchorU;
  private float windowAnchorV;
  private double anchorU;
  private double anchorV;
  private double elevation;
  private MFBitmapDescriptor iconBitmapDescriptor;
  private String userData;
  private MFMarker marker;

  private final DraweeHolder<?> logoHolder;
  private DataSource<CloseableReference<CloseableImage>> dataSource;
  private final ControllerListener<ImageInfo> mLogoControllerListener =
    new BaseControllerListener<ImageInfo>() {
      @Override
      public void onFinalImageSet(
          String id,
          @Nullable final ImageInfo imageInfo,
          @Nullable Animatable animatable) {
        CloseableReference<CloseableImage> imageReference = null;
        try {
          imageReference = dataSource.getResult();
          if (imageReference != null) {
            CloseableImage image = imageReference.get();
            if (image != null && image instanceof CloseableStaticBitmap) {
              CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) image;
              Bitmap bitmap = closeableStaticBitmap.getUnderlyingBitmap();
              if (bitmap != null) {
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                iconBitmapDescriptor = MFBitmapDescriptorFactory.fromBitmap(bitmap);
              }
            }
          }
        } finally {
          dataSource.close();
          if (imageReference != null) {
            CloseableReference.closeSafely(imageReference);
          }
        }

        if (RMFMarker.this.marker != null) {
          RMFMarker.this.marker.setIcon(iconBitmapDescriptor);
        }
      }
    };

  public RMFMarker(Context context) {
    super(context);
    this.position = new MFLocationCoordinate(0, 0);
    this.rotation = 0.0;
    this.title = "";
    this.snippet = "";
    this.draggable = false;
    this.zIndex = 0.0f;
    this.visible = true;
    this.windowAnchorU = 0.5f;
    this.windowAnchorV = 0.0f;
    this.anchorU = 0.5;
    this.anchorV = 0.5;
    this.elevation = 0.0;
    this.userData = null;

    logoHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    logoHolder.onAttach();
  }

  private GenericDraweeHierarchy createDraweeHierarchy() {
    return new GenericDraweeHierarchyBuilder(getResources())
      .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
      .setFadeDuration(0)
      .build();
  }

  public void setIcon(String uri, int width, int height) {
    if (uri == null) {
      iconBitmapDescriptor = null;
    }
    else if (uri.startsWith("http://") || uri.startsWith("https://") ||
      uri.startsWith("file://") || uri.startsWith("asset://") || uri.startsWith("data:")) {
      ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri));
      if (width != 0 && height != 0) {
        builder.setResizeOptions(ResizeOptions.forDimensions(width, height));
      }
      ImageRequest imageRequest = builder.build();
      ImagePipeline imagePipeline = Fresco.getImagePipeline();
      dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);
      DraweeController controller = Fresco.newDraweeControllerBuilder()
        .setImageRequest(imageRequest)
        .setControllerListener(mLogoControllerListener)
        .setOldController(logoHolder.getController())
        .build();
      logoHolder.setController(controller);
    }
  }

  public void setPosition(ReadableMap data) {
    this.position = new MFLocationCoordinate(data.getDouble("latitude"), data.getDouble("longitude"));
    if (marker != null) {
      marker.setPosition(this.position);
    }
  }

  public void setRotation(double rotation) {
    this.rotation = rotation;
    if (marker != null) {
      marker.setRotation(this.rotation);
    }
  }

  public void setTitle(String title) {
    this.title = title;
    if (marker != null) {
      marker.setTitle(this.title);
    }
  }

  public void setSnippet(String snippet) {
    this.snippet = snippet;
    if (marker != null) {
      marker.setSnippet(this.snippet);
    }
  }

  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
    if (marker != null) {
      marker.setDraggable(this.draggable);
    }
  }

  public void setZIndex(double zIndex) {
    this.zIndex = (float) zIndex;
    if (marker != null) {
      marker.setZIndex(this.zIndex);
    }
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
    if (marker != null) {
      marker.setVisible(this.visible);
    }
  }

  public void setAnchor(ReadableMap data) {
    this.anchorU = data != null && data.hasKey("x") ? data.getDouble("x") : 0.5;
    this.anchorV = data != null && data.hasKey("y") ? data.getDouble("y") : 0.5;
  }

  public void setInfoWindowAnchor(ReadableMap data) {
    double x = data != null && data.hasKey("x") ? data.getDouble("x") : 0.5;
    double y = data != null && data.hasKey("y") ? data.getDouble("y") : 0.0;
    this.windowAnchorU = (float) x;
    this.windowAnchorV = (float) y;
    if (marker != null) {
      marker.setWindowAnchor(this.windowAnchorU, this.windowAnchorV);
    }
  }

  public void setElevation(double elevation) {
    this.elevation = elevation;
    if (marker != null) {
      // Current SDK version don't have this method, update later
      // marker.setElevation(elevation);
    }
  }

  public void setUserData(ReadableMap userData) {
    this.userData = userData.toString();
    if (marker != null) {
      marker.setUserData(this.userData);
    }
  }

  public void addToMap(Map4D map) {
    this.marker = map.addMarker(getMarkerOptions());      
    //updateTracksViewChanges() --> not implemented
  }

  public void removeFromMap(Map4D map) {
    if (marker == null) {
      return;
    }
    marker.remove();
    marker = null;
    //updateTracksViewChanges();
   }

  public MFMarkerOptions getMarkerOptions() {
    if (markerOptions == null) {
      markerOptions = new MFMarkerOptions();
    }

    fillMarkerOptions(markerOptions);
    return markerOptions;
  }

  private MFMarkerOptions fillMarkerOptions(MFMarkerOptions options) {
    options.position(position);
    options.anchor((float) anchorU, (float) anchorV);
    options.infoWindowAnchor(windowAnchorU, windowAnchorV);
    options.title(title);
    options.snippet(snippet);
    options.rotation(rotation);
    options.draggable(draggable);
    options.elevation(elevation);
    options.zIndex(zIndex);
    options.icon(iconBitmapDescriptor);
    options.userData(userData);
    return options;
  }

  public Object getFeature() {
    return marker;
  }
}