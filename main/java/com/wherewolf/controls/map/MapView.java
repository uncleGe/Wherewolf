package com.wherewolf.controls.map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wherewolf.FacebookIntegration;
import com.wherewolf.MainActivity;
import com.wherewolf.PTimer;
import com.wherewolf.R;
import com.wherewolf.controls.legend.Legend;
import com.wherewolf.features.Feature;
import com.wherewolf.features.FeatureThread;
import com.wherewolf.features.FeatureType;
import com.wherewolf.features.Features;
import com.wherewolf.features.ui.NewFeatureDialogBase;
import com.wherewolf.features.ui.NewFeatureDialog_Alert;
import com.wherewolf.features.ui.NewFeatureDialog_Listing;
import com.wherewolf.features.ui.NewFeatureDialog_Question;
import com.wherewolf.features.ui.NewFeatureDialog_Status;
import com.wherewolf.features.ui.NewFeatureDialog_ThingToDo;
import com.wherewolf.features.ui.NewFeatureDialog_Tip;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Greg on 12/11/13.
 */
public class MapView extends RelativeLayout implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener, LocationListener {
    public static final int MinZoom = 3; // Mapnik Default Size

    public static final float MaxZoom = 20f; // Mapnik Default Size

    public static final int TileSize = 256; // Mapnik Default Size
    public static Features MapFeatures = new Features();

    public boolean debugMap = false;
    Paint p = new Paint();
    FeatureBubble MapBubble;
    Legend MapLegend;


    private float mapZoom = 15;
    public static GeoPoint mapCenter = new GeoPoint(40.764941f, -73.330364f);

    private GestureDetector Detector;
    private ScaleGestureDetector ScaleDetector;
    private int NumberTilesX = 1;
    private int NumberTilesY = 1;

    private int FeatureIconMapSize = 100;
    private float FeatureClickScaler = 1.0f;
    private int FeatureSeperaterScaler = 2;
    public static double lastProcessedZoom = 1000;
    private Location myLoc;
    public static GeoRect NewRect;

    private int CurTileSize = 256;
    private Bitmap centerBitmap = null;

    private boolean centerBitmapAlphaUp = true;
    private int centerBitmapAlpha = 255;

    public MapState State = MapState.Normal;
    public Feature NewFeature = null;

    public final float pixelDensity = getContext().getResources().getDisplayMetrics().density;


    public MapView(Context context) {
        super(context);
        Initialize();
        MainActivity.MainMap = this;


        Resources r = getResources();
        FeatureIconMapSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Initialize();
        MainActivity.MainMap = this;
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Initialize();
        MainActivity.MainMap = this;
    }

    public static long getUsedMemorySize() {

        long freeSize = 0L;
        long totalSize = 0L;
        long usedSize = -1L;
        try {
            Runtime info = Runtime.getRuntime();
            freeSize = info.freeMemory();
            totalSize = info.totalMemory();
            usedSize = totalSize - freeSize;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usedSize;
    }

    private void InitializeGestures() {
        Detector = new GestureDetector(getContext(), this);
        Detector.setOnDoubleTapListener(this);

        ScaleDetector = new ScaleGestureDetector(getContext(), this);
    }

    public double GetDegreesPerPixel(float zoom) {
        double DegreesPerTile = 360 / Math.pow(2, mapZoom);
        double DegreesPerPixel = DegreesPerTile / CurTileSize;
        return DegreesPerPixel;
    }

    public double GetPixelsPerDegree(float zoom) {
        double DegreesPerTile = 360 / Math.pow(2, mapZoom);
        double PixelsPerDegree = CurTileSize / DegreesPerTile;
        return PixelsPerDegree;
    }

    public boolean onTouchEvent(MotionEvent e) {

        Detector.onTouchEvent(e);
        ScaleDetector.onTouchEvent(e);
        return true;
    }

    public boolean onDown(MotionEvent e) {
        Log.d("MapGesture", "Down");
        return true;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("MapGesture", "Fling");
        return true;
    }

    public void onLongPress(MotionEvent e) {
        debugMap = !debugMap;
        if (debugMap) {
            Toast.makeText(getContext(), "Debug Mode!", Toast.LENGTH_SHORT).show();
        }
        Log.d("MapGesture", "LongPress");
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        double DegreesPerPixel = GetDegreesPerPixel(mapZoom);
        GeoPoint topLeft = GetGeoPoint(new Point(0, 0));
        GeoPoint bottomRight = GetGeoPoint(new Point(getWidth(), getHeight()));
        if (topLeft.Latitude - (distanceY * DegreesPerPixel) < 84.0 &&
                bottomRight.Latitude - (distanceY * DegreesPerPixel) > -80.0)
        {
            setMapCenter(new GeoPoint((float) (mapCenter.Latitude - (distanceY * DegreesPerPixel)), (float) (mapCenter.Longitude + (distanceX * DegreesPerPixel))));
            Log.d("MapGesture_OnScroll", String.format("Delta: (%f, %f)", distanceX, distanceY));
        }
        return true;
    }

    public void onShowPress(MotionEvent e) {
        Log.d("MapGesture", "ShowPres");
    }

    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("MapGesture", "SingleTapUp");
        return true;
    }

    public boolean onDoubleTap(MotionEvent e) {
        Log.d("MapGesture", "DoubleTap");
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        Log.d("MapGesture", "DoubleTapEvent");
        return true;
    }

    public void NewFeatureConfirmLocation() {

        NewFeature.Location = mapCenter;
        NewFeatureDialogBase NewFeatureDialog = null;
        switch (NewFeature.Type) {
            case Status:
                NewFeatureDialog = new NewFeatureDialog_Status(getContext());
                break;
            case ThingToDo:
                NewFeatureDialog = new NewFeatureDialog_ThingToDo(getContext());
                break;
            case Tip:
                NewFeatureDialog = new NewFeatureDialog_Tip(getContext());
                break;
            case Question:
                NewFeatureDialog = new NewFeatureDialog_Question(getContext());
                break;
            case Listing:
                NewFeatureDialog = new NewFeatureDialog_Listing(getContext());
                break;
            case Alert:
                NewFeatureDialog = new NewFeatureDialog_Alert(getContext());
                break;
        }

        if (NewFeatureDialog == null)
            return;

        NewFeatureDialog.setMap(this);
        NewFeatureDialog.show();
        State = MapState.Normal;
        ClearCenterBitmap();
    }

    public void NewFeaturePost() {
        try {
            StringBuilder FeaturesURL = new StringBuilder(256);
            FeaturesURL.append("http://ec2-54-187-116-83.us-west-2.compute.amazonaws.com/postfb/");
            FeaturesURL.append("?AT=");
            FeaturesURL.append(FacebookIntegration.GetCurrentAccessToken());
            FeaturesURL.append("&FeatureType=");
            FeaturesURL.append(NewFeature.Type);
            FeaturesURL.append("&Latitude=");
            FeaturesURL.append(NewFeature.Location.Latitude);
            FeaturesURL.append("&Longitude=");
            FeaturesURL.append(NewFeature.Location.Longitude);
            FeaturesURL.append("&ContentText=");
            FeaturesURL.append(NewFeature.Text.replace(" ", "%20"));

            Log.i("Posting", FeaturesURL.toString());
            URL PostFeatureURL = new URL(FeaturesURL.toString());
            Log.i("Parser", FeaturesURL.toString());
            HttpURLConnection Connection = (HttpURLConnection) PostFeatureURL.openConnection();
            Connection.setDoInput(true);
            Connection.setUseCaches(false);
            Connection.addRequestProperty("Cache-Control", "no-cache");

            Connection.connect();
            String s = Connection.getResponseMessage();
            Log.i("Post Response", s);

            Toast.makeText(getContext(), "Posted!", Toast.LENGTH_SHORT).show();

            FeatureThread.QueueRefresh();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Oops, posting may have failed :(", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("MapGesture", "SingleTapConfirmed");
        if (State == MapState.SettingPostPosition) {
            Point CenterPoint = GetPixel(mapCenter);
            double Distance = Math.sqrt(Math.pow(CenterPoint.x - e.getX(), 2) + Math.pow(CenterPoint.y - e.getY(), 2));
            if (Distance < 24) {
                NewFeatureConfirmLocation();
            }
        }
        synchronized (MapFeatures) {
            boolean FeatureClicked = false;
            for (int i = 0; i < MapFeatures.size(); i++) {
                Feature CurrentFeature = MapFeatures.get(i);
                Point FeaturePoint = GetPixel(CurrentFeature.Location);
                double Distance = Math.sqrt(Math.pow(FeaturePoint.x - e.getX(), 2) + Math.pow(FeaturePoint.y - e.getY(), 2));
                if ((CurrentFeature.isObscured == false) && (Distance < (int) (FeatureIconMapSize * FeatureClickScaler))) {
                    FeatureClicked = true;
                    currentFeature = CurrentFeature;
                    MapBubble.FeatureCreator.setText(CurrentFeature.Name);
                    MapBubble.FeatureAge.setText(CurrentFeature.GetFeatureAge());
                    MapBubble.FeatureText.setText(CurrentFeature.Text);
                    MapBubble.CommentsButton.setText("Comments (" + currentFeature.FeatureComments + ")");
                    MapBubble.CurrentFeature = currentFeature;
                    MapBubble.PictureView.setProfileId(Long.toString(currentFeature.UserID));

                    switch (CurrentFeature.Type) {
                        case Status:
                            MapBubble.FeatureTitle.setText("Status");
                            break;
                        case ThingToDo:
                            MapBubble.FeatureTitle.setText("Thing to Do");
                            break;
                        case Tip:
                            MapBubble.FeatureTitle.setText("Tip");
                            break;
                        case Question:
                            MapBubble.FeatureTitle.setText("Question");
                            break;
                        case Listing:
                            MapBubble.FeatureTitle.setText("Listing");
                            break;
                        case Alert:
                            MapBubble.FeatureTitle.setText("Alert");
                            break;
                    }
                }
            }
            if (!FeatureClicked) {
                currentFeature = null;
            }
        }
        return true;
    }

    public boolean onScale(ScaleGestureDetector detector) {
        setMapZoom(mapZoom * (((detector.getScaleFactor() - 1) / 10) + 1));
        Log.d("MapGesture", "onScale - " + detector.getScaleFactor());
        return true;
    }

    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d("MapGesture", "onScaleBegin");
        double DegreesPerPixel = GetDegreesPerPixel(mapZoom);
        GeoPoint topLeft = GetGeoPoint(new Point(0, 0));
        GeoPoint bottomRight = GetGeoPoint(new Point(getWidth(), getHeight()));
        if (topLeft.Latitude > 84.0 ||
                bottomRight.Latitude < -80.0) {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d("MapGesture", "onScaleEnd");
    }

    private void Initialize() {
        setWillNotDraw(false);
        InitializeGestures();

        MapLegend = new Legend(getContext(), this);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.alignWithParent = true;
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        MapLegend.setLayoutParams(lp);
        addView(MapLegend);

        MapBubble = new FeatureBubble(getContext());
        addView(MapBubble);


        MapLegend.bringToFront();
        FeatureThread NewFeatureThread = new FeatureThread();
        FeatureThread.mapView = this;

        Thread WorkerThread = new Thread(NewFeatureThread);
        WorkerThread.setName("Feature Thread");
        WorkerThread.setPriority(Thread.NORM_PRIORITY - 1);
        WorkerThread.start();

        if (!isInEditMode()) {
            LocationManager locManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            myLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (myLoc != null) {
                setMapCenter(new GeoPoint((float) myLoc.getLatitude(), (float) myLoc.getLongitude()));
            }
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d("MapView_onSizeChanged", "W: " + oldw + "->" + w + " H: " + oldh + "->" + h);
        super.onSizeChanged(w, h, oldw, oldh);
        NumberTilesX = (int) Math.ceil((double) w / TileSize);
        NumberTilesY = (int) Math.ceil((double) h / TileSize);
    }

    private Feature currentFeature = null;

    private void UpdateBubblePos() {
        if (currentFeature == null)
        {
            MapBubble.setVisibility(View.INVISIBLE);
            return;
        }

        Point BubPoint = GetPixel(currentFeature.Location);

        Log.i("MapBubble", "Width: " + MapBubble.getWidth());
        BubPoint.x -= MapBubble.getWidth() / 2;
        BubPoint.y -= (MapBubble.getHeight());// - 24); // TODO - Icon Size
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) MapBubble.getLayoutParams(); //= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        params.leftMargin = BubPoint.x; //Your X coordinate
        params.topMargin = BubPoint.y; //Your Y coordinate
        params.rightMargin = -1 * BubPoint.x;

        MapBubble.setLayoutParams(params);

        MapBubble.setVisibility(View.VISIBLE);
    }



    public void onDraw(Canvas canvas) {
        UpdateBubblePos();


        CurTileSize = (int) ((TileSize * 1.4) * ((1 + ((mapZoom - (int) mapZoom)) * 1)));
        PointF CenterTile = WorldToTilePos(mapCenter.Latitude, mapCenter.Longitude, (int) mapZoom);
        int CenterTileX = (int) (CurTileSize * (CenterTile.x - (int) CenterTile.x));
        int CenterTileY = (int) (CurTileSize * (CenterTile.y - (int) CenterTile.y));


        int StartX = canvas.getClipBounds().width() / 2;
        int StartY = canvas.getClipBounds().height() / 2;
        int StartTileX = (int) CenterTile.x;
        int StartTileY = (int) CenterTile.y;
        StartX -= CenterTileX;
        StartY -= CenterTileY;
        while (StartX >= 0) {
            StartX -= CurTileSize;
            StartTileX--;
        }
        while (StartY >= 0) {
            StartY -= CurTileSize;
            StartTileY--;
        }
        int CurTileX = StartTileX;
        int CurTileY = StartTileY;
        for (int x = StartX; x <= canvas.getClipBounds().width(); x += CurTileSize) {
            CurTileY = StartTileY;
            for (int y = StartY; y <= canvas.getClipBounds().height(); y += CurTileSize) {
                p.setAntiAlias(true);
                p.setFilterBitmap(true);
                Bitmap b = TileFetcher.GetTile(getContext(), CurTileX, CurTileY, (int) mapZoom);
                canvas.drawBitmap(b, new Rect(0, 0, TileSize, TileSize), new Rect(x, y, x + CurTileSize, y + CurTileSize), p);
                if (debugMap) {
                    p.setTextSize(15);
                    p.setStyle(Paint.Style.STROKE);
                    p.setAlpha(50);
                    canvas.drawRect(x, y, x + CurTileSize, y + CurTileSize, p);
                    p.setAlpha(255);

                    p.setFakeBoldText(true);
                    p.setStyle(Paint.Style.FILL);
                    canvas.drawText("(" + CurTileX + "," + CurTileY + ") ZOOM: " + (int) mapZoom, x + (CurTileSize / 2) - 20, y + (CurTileSize / 2) + 20, p);
                    canvas.drawText("CurTileSize: " + CurTileSize, x + (CurTileSize / 2) - 20, y + (CurTileSize / 2) + 40, p);
                    canvas.drawText("Memory Usage: " + ((float) getUsedMemorySize() / (1024 * 1024)) + "MB", x + (CurTileSize / 2) - 100, y + (CurTileSize / 2) + 85, p);
                    canvas.drawText("Cached Tiles: " + TileFetcher.Cache.size(), x + (CurTileSize / 2) - 60, y + (CurTileSize / 2) + 105, p);
                    canvas.drawText("Cached Tiles: " + TileFetcher.Cache.size(), x + (CurTileSize / 2) - 60, y + (CurTileSize / 2) + 105, p);
                    canvas.drawText("Rect: " + GetViewBounds().p1, x + (CurTileSize / 2) - 120, y + (CurTileSize / 2) - 35, p);
                    canvas.drawText("Rect: " + GetViewBounds().p2, x + (CurTileSize / 2) - 120, y + (CurTileSize / 2) - 25, p);

                    canvas.drawText(com.facebook.AccessToken.getCurrentAccessToken().getToken(), x + (CurTileSize / 2) - 120, y + (CurTileSize / 2) - 150, p);
                }
                CurTileY++;
            }
            CurTileX++;
        }
        DrawFeatures(canvas);
        DrawCenterBitmap(canvas);
        UpdateBubblePos();
        DrawSearchMarker(canvas);
        super.onDraw(canvas);
        invalidate();
    }

    public void DrawCenterBitmap(Canvas canvas) {
        if (centerBitmap != null && MapLegend != null) {
            if (centerBitmapAlphaUp) {
                centerBitmapAlpha += 10;
                if (centerBitmapAlpha > 255) {
                    centerBitmapAlpha = 255;
                    centerBitmapAlphaUp = false;
                }
            } else {
                centerBitmapAlpha -= 10;
                if (centerBitmapAlpha < 100) {
                    centerBitmapAlpha = 100;
                    centerBitmapAlphaUp = true;
                }
            }

            Paint p = new Paint();
            Point FeaturePoint = GetPixel(mapCenter);
            FeaturePoint.x -= (centerBitmap.getWidth() / 2);
            FeaturePoint.y -= (centerBitmap.getHeight() / 2);
            p.setAlpha(centerBitmapAlpha);
            canvas.drawBitmap(centerBitmap, FeaturePoint.x, FeaturePoint.y, p);
        }
    }

    public void DrawFeatures(Canvas canvas) {
        Paint p = new Paint();
        synchronized (MapFeatures) {
            // Only filter if zoom changes enough
            if ((Math.abs(lastProcessedZoom - mapZoom) > 0.1)) {
                lastProcessedZoom = mapZoom;
                for (int i = 0; i < MapFeatures.size(); i++) {
                    Feature CurrentFeature = MapFeatures.get(i);
                    Point FeaturePoint = GetPixel(CurrentFeature.Location);
                    CurrentFeature.FeaturePointX = FeaturePoint.x;
                    CurrentFeature.FeaturePointY = FeaturePoint.y;
                    CurrentFeature.isObscured = false;
                }

                int featureCount = MapFeatures.size();
                int outerLoopCount = featureCount - 1;
                int featureSizeSeperator = FeatureIconMapSize * FeatureSeperaterScaler / 2;
                Feature CurrentFeature, CompareFeature;

                for (int i = 0; i < outerLoopCount; i++) {
                    CurrentFeature = MapFeatures.get(i);

                    for (int j = i + 1; j < featureCount; j++) {
                        CompareFeature = MapFeatures.get(j);

                        if (CurrentFeature.isObscured ||
                                CompareFeature.isObscured ||
                                CurrentFeature.offScreen ||
                                CompareFeature.offScreen)
                            continue;

                        if ((CurrentFeature.FeaturePointX > CompareFeature.FeaturePointX - featureSizeSeperator) &&
                                (CurrentFeature.FeaturePointX < CompareFeature.FeaturePointX + featureSizeSeperator) &&
                                (CurrentFeature.FeaturePointY > CompareFeature.FeaturePointY - featureSizeSeperator) &&
                                (CurrentFeature.FeaturePointY < CompareFeature.FeaturePointY + featureSizeSeperator)) {
                            if (CurrentFeature == currentFeature) {
                                CurrentFeature.isObscured = false;
                                CompareFeature.isObscured = true;
                                continue;
                            } else if (CompareFeature == currentFeature) {
                                CurrentFeature.isObscured = true;
                                CompareFeature.isObscured = false;
                                continue;
                            }

                            if (CurrentFeature.GetSecondsOld() <= CompareFeature.GetSecondsOld())
                                CompareFeature.isObscured = true;
                            else
                                CurrentFeature.isObscured = true;
                        }
                    }
                }
            }

            for (int i = 0; i < MapFeatures.size(); i++) {
                Feature CurrentFeature = MapFeatures.get(i);
                if (!CurrentFeature.isObscured || currentFeature == CurrentFeature) {

                    if (CurrentFeature.Type != FeatureType.Unknown)
                        p.setARGB(255, 255, 0, 0);
                    else
                        p.setARGB(255, 255, 0, 255);
                    Point FeaturePoint = GetPixel(CurrentFeature.Location);

                    if (FeaturePoint.x + FeatureIconMapSize /2 < 0 ||
                            FeaturePoint.x - FeatureIconMapSize / 2 > canvas.getWidth() ||
                            FeaturePoint.y + FeatureIconMapSize / 2 < 0 ||
                            FeaturePoint.y - FeatureIconMapSize / 2 > canvas.getHeight()){
                        continue;
                    }

                    Bitmap fBitmap = FeatureIcons.GetIcon(CurrentFeature.Type, FeatureIconMapSize);

                    canvas.drawBitmap(fBitmap, FeaturePoint.x - (fBitmap.getWidth() / 2), FeaturePoint.y - (fBitmap.getHeight() / 2), p);
                    if (debugMap) {
                        p.setAlpha(100);
                        canvas.drawCircle(FeaturePoint.x, FeaturePoint.y, (int) (FeatureIconMapSize * FeatureClickScaler), p);
                        p.setAlpha(255);
                    }
                }
            }
        }
    }

    public void DrawSearchMarker (Canvas canvas){

        if (MainActivity.showAnyMarkers) {
            if (MainActivity.submitClicked) {
                if (MainActivity.showSubmitMarkers) {
                    if (MainActivity.markerLocHash.size() > 0) {

                        Bitmap venueMarker = BitmapFactory.decodeResource(getResources(), R.mipmap.star);

                            GeoPoint markerGeoPoint = MainActivity.mapCenterPoint;
//
//                            // Check for no/bad search results
                            if (markerGeoPoint.Latitude == GeoSearch.defaultLat && !MainActivity.showedBadDataMessage) {
                                Toast.makeText(getContext(), "Sorry, we couldn't find your search result :(", Toast.LENGTH_LONG).show();
                                MainActivity.searching = false;
                                MainActivity.showedBadDataMessage = true;
                            } else
                            {

                                Point searchMarkerPixelPoint = GetPixel(markerGeoPoint);
                                Paint p = new Paint();
                                canvas.drawBitmap(venueMarker, searchMarkerPixelPoint.x - (venueMarker.getWidth() / 2), searchMarkerPixelPoint.y - (venueMarker.getHeight() / 2), p);

                                Paint p2 = new Paint();
                                p2.setColor(Color.BLACK);
                                p2.setTextSize(35);
                                p2.setTextAlign(Paint.Align.CENTER);
                                if (MainActivity.queryHasNumbers){
                                    canvas.drawText(MainActivity.primaryAddress, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 20 * pixelDensity, p2);
                                }
                                else
                                {
                                    canvas.drawText(MainActivity.primaryVenueName, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 20 * pixelDensity, p2);
                                    canvas.drawText(MainActivity.primaryVenueAddress, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 35 * pixelDensity, p2);
                                }
                            }
//                        }
                    }
                    else
                    {
                        // Check for no/bad search results
                        if (!MainActivity.showedBadDataMessage)
                        {
                            Toast.makeText(getContext(), "Sorry, we couldn't find your search result :(", Toast.LENGTH_LONG).show();
                            MainActivity.searching = false;
                            MainActivity.showedBadDataMessage = true;
                        }
                    }
                }
            } else {
                // If suggestion list item clicked, draw the marker for that item
                if (MainActivity.markerGeoPoint.Latitude == GeoSearch.defaultLat && !MainActivity.showedBadDataMessage) {
                    Toast.makeText(getContext(), "Sorry, couldn't find it :(", Toast.LENGTH_LONG).show();
                    MainActivity.searching = false;
                    MainActivity.showedBadDataMessage = true;
                } else {
                    Point searchMarkerPixelPoint = GetPixel(MainActivity.markerGeoPoint);
                    Paint p = new Paint();
                    Bitmap venueMarker = BitmapFactory.decodeResource(getResources(), R.mipmap.star);
                    canvas.drawBitmap(venueMarker, searchMarkerPixelPoint.x - (venueMarker.getWidth() / 2), searchMarkerPixelPoint.y - (venueMarker.getHeight() / 2), p);

                    Paint p2 = new Paint();
                    p2.setColor(Color.BLACK);
                    p2.setTextSize(35);
                    p2.setTextAlign(Paint.Align.CENTER);
                    if (MainActivity.queryHasNumbers){
                        canvas.drawText(MainActivity.plainAddressText, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 20 * pixelDensity, p2);

                    }
                    else
                    {
                        canvas.drawText(MainActivity.venueName, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 20 * pixelDensity, p2);
                        canvas.drawText(MainActivity.venueAddress, searchMarkerPixelPoint.x, searchMarkerPixelPoint.y + 35 * pixelDensity, p2);
                    }
                }
            }

            // Handle post-search map location
            if (MainActivity.searching) {
                if (MainActivity.submitClicked) {
                    if (MainActivity.markerLocHash.size() == 0 && !MainActivity.showedBadDataMessage) {
                        setMapCenter(MainActivity.mapCenterAtSubmitTime);
                        Toast.makeText(getContext(), "Sorry, we couldn't find that :(", Toast.LENGTH_LONG).show();
                        MainActivity.showedBadDataMessage = true;
                    } else {
                        setMapCenter(MainActivity.mapCenterPoint);
                    }
                } else {
                    setMapCenter(MainActivity.markerGeoPoint);
                }
                MainActivity.searching = false;
            }
        }
    }


    public Rect GetMapTileBounds() {
        return new Rect();
    }

    public PointF WorldToTilePos(double lat, double lon, int zoom) {
        PointF p = new PointF();
        p.x = (float) ((lon + 180.0) / 360.0 * (1 << zoom));
        p.y = (float) ((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * (1 << zoom));
        return p;
    }

    public PointF TileToWorldPos(double tile_x, double tile_y, int zoom) {
        PointF p = new PointF();
        double n = Math.PI - ((2.0 * Math.PI * tile_y) / Math.pow(2.0, zoom));

        p.x = (float) ((tile_x / Math.pow(2.0, zoom) * 360.0) - 180.0);
        p.y = (float) (180.0 / Math.PI * Math.atan(Math.sinh(n)));

        return p;
    }

    public Point GetPixel(GeoPoint gPoint) // GeoPoint(lat, lng) -> Map View Point (x,y)
    {
        PointF TilePos = WorldToTilePos((double) gPoint.Latitude, (double) gPoint.Longitude, (int) mapZoom);
        PointF CenterPos = WorldToTilePos((double) mapCenter.Latitude, (double) mapCenter.Longitude, (int) mapZoom);

        int x = (int) ((this.getWidth() / 2) - (CenterPos.x - TilePos.x) * CurTileSize);
        int y = (int) ((this.getHeight() / 2) - (CenterPos.y - TilePos.y) * CurTileSize);
        return new Point(x, y);
    }

    public GeoPoint GetGeoPoint(Point mPoint) // Map View Point (x,y) -> GeoPoint(lat, lng)
    {

        PointF CenterPos = WorldToTilePos((double) mapCenter.Latitude, (double) mapCenter.Longitude, (int) mapZoom);
        float tilesX = ((this.getWidth() / 2) / CurTileSize);
        float tilesY = ((this.getHeight() / 2) / CurTileSize);

        float tileX = CenterPos.x - ((((float) this.getWidth() / 2) - mPoint.x) / CurTileSize);
        float tileY = CenterPos.y - ((((float) this.getHeight() / 2) - mPoint.y) / CurTileSize);

        PointF gPoint = TileToWorldPos(tileX, tileY, (int) mapZoom);
        return new GeoPoint(gPoint.y, gPoint.x);
    }

    public GeoRect GetViewBounds() {
        GeoPoint P1 = GetGeoPoint(new Point(0, 0));
        GeoPoint P2 = GetGeoPoint(new Point(getWidth(), getHeight()));
        NewRect = new GeoRect();
        NewRect.p1 = P1;
        NewRect.p2 = P2;

        return NewRect;
    }

    public void setMapZoom(float MapZoom) {
        mapZoom = Math.min(MaxZoom, Math.max(MinZoom, MapZoom));
        invalidate();
    }

    public void setMapCenter(GeoPoint nMapCenter) {
        GeoPoint NewMapCenter = nMapCenter;
        double DegreesPerTile = 170.1022 / Math.pow(2, (int) mapZoom);
        double dpp = DegreesPerTile / CurTileSize;

        float Max = 85.0511f - (float) (dpp * getHeight() / 2.0f);
        float Min = -1 * Max;

        mapCenter = NewMapCenter;
        invalidate();
    }

    public void SetCenterBitmap(Bitmap b, int Alpha) {
        centerBitmap = b;
        centerBitmapAlpha = Alpha;
    }

    private void ClearCenterBitmap() {
        centerBitmap = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("NewLocation", location.toString());
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public void IntentFeature(int FeatureID, float Latitude, float Longitude) {
        Log.i("IntentFeature", "BEGIN");

        this.setMapZoom(13);
        this.setMapCenter(new GeoPoint(Latitude, Longitude));

        Log.i("IntentFeature", "END");
    }

    public void IntentFeatureComments(int FeatureID, float Latitude, float Longitude) {
        Log.i("IntentFeatureComments", "BEGIN");
        IntentFeature(FeatureID, Latitude, Longitude);
        Log.i("IntentFeatureComments", "END");
    }
}