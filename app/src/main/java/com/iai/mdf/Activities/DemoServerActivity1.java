package com.iai.mdf.Activities;

import android.content.pm.ActivityInfo;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iai.mdf.DependenceClasses.DeviceConfiguration;
import com.iai.mdf.Handlers.CameraHandler;
import com.iai.mdf.Handlers.DrawHandler;
import com.iai.mdf.Handlers.SocketHandler;
import com.iai.mdf.Handlers.TensorFlowHandler;
import com.iai.mdf.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

//import com.moutaigua.isl_android_gaze.FaceDetectionAPI;

/**
 * Created by Mou on 9/22/2017.
 */

public class DemoServerActivity1 extends AppCompatActivity {

    public static final String BUNDLE_KEY_IP = "ip";
    public static final String BUNDLE_KEY_PORT = "port";
    private final String LOG_TAG = "DemoServerActivity1";
    private final String JSON_STRING_START = "RESP_START";
    private final String JSON_KEY_PREDICT_X = "PredictX";
    private final String JSON_KEY_PREDICT_Y = "PredictY";
    private final String JSON_KEY_SEQ_NUMBER = "SequenceNumber";


    private CameraHandler cameraHandler;
    private DrawHandler drawHandler;
    private TextureView textureView;
    private Spinner     spinnerView;
    private ToggleButton toggleButton;
    private FrameLayout frame_background_grid;
    private FrameLayout view_dot_container;
    private FrameLayout frame_gaze_result;
    private FrameLayout frame_bounding_box;
    private TextView    result_board;
    private int[]       SCREEN_SIZE;
    private int[]       TEXTURE_SIZE;
    private BaseLoaderCallback openCVLoaderCallback;
    private boolean isRealTimeDetection = false;
    private Handler autoDetectionHandler = new Handler();
    private Runnable takePicRunnable;
    private TensorFlowHandler tensorFlowHandler;
    private int         mFrameIndex = 0;
    private int         currentClassNum = 4;
    private SocketHandler socketHandler;
    private int         prevReceivedGazeIndex = 0;
    private String  socketIp = null;
    private int     socketPort;
    private DeviceConfiguration confHandler = DeviceConfiguration.getInstance(this);



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle extras = getIntent().getExtras();
        socketIp = extras.getString(BUNDLE_KEY_IP);
        socketPort = extras.getInt(BUNDLE_KEY_PORT);

        // init openCV
        initOpenCV();

        SCREEN_SIZE = fetchScreenSize();
        textureView = findViewById(R.id.activity_demo_preview_textureview);
        // ensure texture fill the screen with a certain ratio
        TEXTURE_SIZE = SCREEN_SIZE;
        int expected_height = TEXTURE_SIZE[0]*DataCollectionActivity.Image_Size.getHeight()/DataCollectionActivity.Image_Size.getWidth();
        if( expected_height< TEXTURE_SIZE[1] ){
            TEXTURE_SIZE[1] = expected_height;
        } else {
            TEXTURE_SIZE[0] = TEXTURE_SIZE[1]*DataCollectionActivity.Image_Size.getWidth()/DataCollectionActivity.Image_Size.getHeight();
        }
        textureView.setLayoutParams(new RelativeLayout.LayoutParams(TEXTURE_SIZE[0], TEXTURE_SIZE[1]));


        view_dot_container = findViewById(R.id.activity_demo_layout_dotHolder_background);
        frame_background_grid = findViewById(R.id.activity_demo_layout_background_grid);
        frame_background_grid.setLayoutParams(new RelativeLayout.LayoutParams(TEXTURE_SIZE[0], TEXTURE_SIZE[1]));
//        frame_background_grid.bringToFront();
        frame_gaze_result = findViewById(R.id.activity_demo_layout_dotHolder_result);
        frame_gaze_result.bringToFront();
        drawHandler = new DrawHandler(this, fetchScreenSize());
        drawHandler.setDotHolderLayout(view_dot_container);
//        drawHandler.showAllCandidateDots();

        frame_bounding_box = findViewById(R.id.activity_demo_layout_bounding_box);
//        frame_bounding_box.bringToFront();
        frame_bounding_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawHandler.clear(frame_bounding_box);
                drawHandler.clear(frame_gaze_result);
                drawHandler.clear(view_dot_container);
                Log.d(LOG_TAG, "pressed");  //cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_STILL_CAPTURE);
                if( !socketHandler.isConnected() && !isRealTimeDetection ){
                    Toast.makeText(DemoServerActivity1.this, "Restart to connect to the server", Toast.LENGTH_SHORT).show();
                    return;
                }
                isRealTimeDetection = !isRealTimeDetection;
                if(isRealTimeDetection){
                    frame_background_grid.setBackgroundColor(0xFFFFFFFF);   // cover texture with white
                    autoDetectionHandler.post(takePicRunnable);
                    result_board.setText("");
                } else {
                    frame_background_grid.setBackgroundColor(0x00FFFFFF);   // uncover texture with translucent
                    cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_PREVIEW);
                    autoDetectionHandler.removeCallbacks(takePicRunnable);
                    result_board.setText("Press Anywhere to Start");
                }
            }
        });


        spinnerView = (Spinner) findViewById(R.id.activity_demo_spinner_class_number);
        ArrayList<String> classNumOptions = new ArrayList<>();
        classNumOptions.add("2x2");
        classNumOptions.add("2x3");
        classNumOptions.add("3x3");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classNumOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerView.setAdapter(spinnerAdapter);
        spinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!isRealTimeDetection){
                    String selectedLabel = (String)adapterView.getSelectedItem();
                    switch (selectedLabel){
                        case "2x2":
                            currentClassNum = 4;
                            switchGridBackground(frame_background_grid, R.layout.grid4_for_demo);
                            Log.d(LOG_TAG, "Selected: 2x2");
                            break;
                        case "2x3":
                            currentClassNum = 6;
                            switchGridBackground(frame_background_grid, R.layout.grid6_for_demo);
                            Log.d(LOG_TAG, "Selected: 2x3");
                            break;
                        case "3x3":
                            currentClassNum = 9;
                            switchGridBackground(frame_background_grid, R.layout.grid9_for_demo);
                            Log.d(LOG_TAG, "Selected: 3x3");
                            break;
                    }
                } else {
                    // return to the previous state
                    switch (currentClassNum){
                        case 4:
                            spinnerView.setSelection(0);
                            break;
                        case 6:
                            spinnerView.setSelection(1);
                            break;
                        case 9:
                            spinnerView.setSelection(2);
                            break;
                    }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(LOG_TAG, "Selected Nothing ");
            }
        });
        spinnerView.bringToFront();

        toggleButton = findViewById(R.id.activity_demo_toggle_show_dot);
        toggleButton.setChecked(false);

        result_board = findViewById(R.id.activity_demo_txtview_result);
        result_board.setText("Press Anywhere to Start");

        takePicRunnable = new Runnable() {
            @Override
            public void run() {
                cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_STILL_CAPTURE);
//                drawHandler.clear(frame_bounding_box);
                drawHandler.clear(frame_gaze_result);
                autoDetectionHandler.postDelayed(this, confHandler.getDemoCaptureDelayTime());
            }
        };


        tensorFlowHandler = new TensorFlowHandler(this);
        tensorFlowHandler.pickModel(TensorFlowHandler.MODEL_ISL_FILE_NAME);

    }


    @Override
    public void onResume() {
        super.onResume();
        cameraHandler = new CameraHandler(this, true);
        cameraHandler.setOnImageAvailableListenerForPrev(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireNextImage();
                if( cameraHandler.getCameraState()==CameraHandler.CAMERA_STATE_STILL_CAPTURE ) {
                    cameraHandler.setCameraState(CameraHandler.CAMERA_STATE_PREVIEW);
                    Log.d(LOG_TAG, "Take a picture");
                    if( Build.MODEL.equalsIgnoreCase("BLU Studio Touch")) {
                        socketHandler.uploadImageOnBLU(image);
                    } else {
                        socketHandler.uploadImage(image, confHandler);
                    }
                }
                image.close();
            }
        });
        cameraHandler.startPreview(textureView);
        // init socket communication
        initSocketConnection();
    }

    @Override
    public void onPause(){
        super.onPause();
        cameraHandler.stopPreview();
        autoDetectionHandler.removeCallbacks(takePicRunnable);
        isRealTimeDetection = false;
        // stop socket communication
        socketHandler.socketDestroy();
    }



    private void initOpenCV(){
        // used when loading openCV4Android
        openCVLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.d(LOG_TAG, "OpenCV loaded successfully");
//                    mOpenCvCameraView.enableView();
//                    mOpenCvCameraView.setOnTouchListener(ColorBlobDetectionActivity.this);
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
        if (!OpenCVLoader.initDebug()) {
            Log.d(LOG_TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, openCVLoaderCallback);
        } else {
            Log.d(LOG_TAG, "OpenCV library found inside package. Using it!");
            openCVLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private void initSocketConnection(){
        socketHandler = new SocketHandler(socketIp, socketPort);
        socketHandler.setUiThreadHandler(new SocketHandler.StringCallback() {
            @Override
            public void onResponse(String str) {
                try {
                    if (str.equalsIgnoreCase(SocketHandler.SUCCESS_CONNECT_MSG)){
                        return;
                    }
                    JSONObject object = new JSONObject(str);
                    if (object != null) {
                        if( object.getBoolean(SocketHandler.JSON_KEY_VALID) && isRealTimeDetection ) {
                            drawGaze(object);
                            Log.d(LOG_TAG, object.toString());
                        } else {
                            Log.d(LOG_TAG, "invalid");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String str) {
                Log.e(LOG_TAG, str);
                if( str.equalsIgnoreCase(SocketHandler.ERROR_DISCONNECTED) ){
                    Toast.makeText(DemoServerActivity1.this, "Disconnect From Server\nRestart Please", Toast.LENGTH_SHORT).show();
                    frame_bounding_box.performClick();
                } else if (str.equalsIgnoreCase(SocketHandler.ERROR_TIMEOUT)) {
                    Log.d(LOG_TAG, "Timeout");
                } else if (str.equalsIgnoreCase(SocketHandler.ERROR_SETTING)) {
                    Toast.makeText(DemoServerActivity1.this, "Please set the address and the port correctly", Toast.LENGTH_SHORT).show();
                    result_board.setText("Wrong address or port");
                }
            }
        });
        socketHandler.socketCreate();
    }









    private int[] fetchScreenSize(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return new int[]{displayMetrics.widthPixels, displayMetrics.heightPixels};
    }

    private void switchGridBackground(FrameLayout layoutHolder, int layoutId){
        layoutHolder.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View childLayout = inflater.inflate(layoutId, (ViewGroup) findViewById(R.id.grid_for_demo));
        layoutHolder.addView(childLayout);
    }

    private void drawGaze(JSONObject object){
        try {
//            int receivedIdx = object.getInt(SocketHandler.JSON_KEY_SEQ_NUMBER);
//            if( receivedIdx > prevReceivedGazeIndex ){
//                prevReceivedGazeIndex = receivedIdx;
            double portraitHori = object.getDouble(SocketHandler.JSON_KEY_PREDICT_Y);
            double portraitVert = object.getDouble(SocketHandler.JSON_KEY_PREDICT_X);
            float[] loc = new float[2];
            loc[0] = (float) (portraitHori + confHandler.getCameraOffsetPWidth())/confHandler.getScreenSizePWidth();
            loc[1] = (float) (portraitVert + confHandler.getCameraOffsetPHeight())/confHandler.getScreenSizePHeight();
            drawClassifiedResult(loc, toggleButton.isChecked());
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawClassifiedResult(float[] estimateGaze, boolean isShowDot){
        if( estimateGaze!=null ){
            switch (currentClassNum){
                case 4:
                    drawHandler.draw4ClassRegrsResult(estimateGaze, TEXTURE_SIZE, frame_gaze_result, isShowDot); break;
                case 6:
                    drawHandler.draw6ClassRegrsResult(estimateGaze, TEXTURE_SIZE, frame_gaze_result, isShowDot); break;
                case 9:
                    drawHandler.draw9ClassRegrsResult(estimateGaze, TEXTURE_SIZE, frame_gaze_result,  isShowDot); break;
            }
        }
    }





}
