package com.mapboxnavigation

import android.content.pm.PackageManager
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.mapbox.geojson.Point
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.TileStoreUsageMode
import javax.annotation.Nonnull

class MapboxNavigationManager(var mCallerContext: ReactApplicationContext) : SimpleViewManager<MapboxNavigation>() {
  private var accessToken: String? = null
  init {
    mCallerContext.runOnUiQueueThread {
      try {
        val app = mCallerContext.packageManager.getApplicationInfo(mCallerContext.packageName, PackageManager.GET_META_DATA)
        val bundle = app.metaData
        val accessToken = bundle.getString("MAPBOX_ACCESS_TOKEN")
        this.accessToken = accessToken
        ResourceOptionsManager.getDefault(mCallerContext, accessToken).update {
          tileStoreUsageMode(TileStoreUsageMode.READ_ONLY)
        }
      } catch (e: PackageManager.NameNotFoundException) {
        Log.d("HunterMapbox", "Error in init")
        e.printStackTrace()
      }
    }
  }
  override fun getName(): String = "MapboxNavigation"

  public override fun createViewInstance(@Nonnull reactContext: ThemedReactContext): MapboxNavigation {
    return MapboxNavigation(reactContext, this.accessToken)
  }

  override fun onDropViewInstance(view: MapboxNavigation) {
    view.onDropViewInstance()
    super.onDropViewInstance(view)
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Map<String, String>>? {
    return MapBuilder.of<String, Map<String, String>>(
      "onLocationChange", MapBuilder.of("registrationName", "onLocationChange"),
      "onError", MapBuilder.of("registrationName", "onError"),
      "onCancelNavigation", MapBuilder.of("registrationName", "onCancelNavigation"),
      "onArrive", MapBuilder.of("registrationName", "onArrive"),
      "onRouteProgressChange", MapBuilder.of("registrationName", "onRouteProgressChange"),
    )
  }

  @ReactProp(name = "origin")
  fun setOrigin(view: MapboxNavigation, sources: ReadableArray?) {
    Log.d("HunterMapbox", "setOrigin")
    if (sources == null) {
      view.setOrigin(null)
      return
    }
    view.setOrigin(Point.fromLngLat(sources.getDouble(0), sources.getDouble(1)))
  }

  @ReactProp(name = "stops")
  fun setStops(view: MapboxNavigation, sources: ReadableArray?) {
    Log.d("HunterMapbox", "setStops")
    if (sources == null || sources.size() == 0) {
      view.setStops(null)
      return
    }

    // Assuming each inner array contains two elements (for longitude and latitude)
    val stopsArray: List<Point> = ArrayList<Point>().apply {
      for (i in 0 until sources.size()) {
        val innerArray: ReadableArray = sources.getArray(i)

        if (innerArray.size() >= 2) {
          val longitude = innerArray.getDouble(0)
          val latitude = innerArray.getDouble(1)
          add(Point.fromLngLat(longitude, latitude))
        }
      }
    }.toList()

    // Pass the list of points to the view's setStops method
    view.setStops(stopsArray)
  }

  @ReactProp(name = "destination")
  fun setDestination(view: MapboxNavigation, sources: ReadableArray?) {
    if (sources == null) {
      view.setDestination(null)
      return
    }
    view.setDestination(Point.fromLngLat(sources.getDouble(0), sources.getDouble(1)))
  }

  @ReactProp(name = "shouldSimulateRoute")
  fun setShouldSimulateRoute(view: MapboxNavigation, shouldSimulateRoute: Boolean) {
    view.setShouldSimulateRoute(shouldSimulateRoute)
  }

  @ReactProp(name = "showsEndOfRouteFeedback")
  fun setShowsEndOfRouteFeedback(view: MapboxNavigation, showsEndOfRouteFeedback: Boolean) {
    view.setShowsEndOfRouteFeedback(showsEndOfRouteFeedback)
  }

  @ReactProp(name = "mute")
  fun setMute(view: MapboxNavigation, mute: Boolean) {
    view.setMute(mute)
  }

}
