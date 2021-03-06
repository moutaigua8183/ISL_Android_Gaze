package com.iai.mdf.Handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iai.mdf.DependenceClasses.CustomGraphics;
import com.iai.mdf.R;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mou on 9/15/2017.
 */

public class DrawHandler {


    public static final int POINT_TYPE_LEFT = 0;
    public static final int POINT_TYPE_RIGHT = 1;
    private final String LOG_TAG = "DrawHandler";
    private final int ROW_COL_POINT_NUM = 6;
    private Context ctxt;
    private int SCREEN_WIDTH;
    private int SCREEN_HEIGHT;
    private ArrayList<Point> dotCandidates;
    private FrameLayout dotHolderLayout;
    private TextView dotTextView;
    private Point   currDot;
    private int     currDotType;




    public DrawHandler(Context context, int[] screenSize){
        ctxt = context;
        SCREEN_WIDTH = screenSize[0];
        SCREEN_HEIGHT = screenSize[1];
        dotCandidates = new ArrayList<>();
        initDotCandidates();
        dotHolderLayout = null;
        currDot = new Point();
        currDot.set(-1, -1);
        currDotType = -1;
    }

    public void showNextPoint(){
        generateDot();
        showDot();
    }

    public void setDotHolderLayout(FrameLayout frameLayout){
        dotHolderLayout = frameLayout;
    }

    public Point getCurrDot(){
        return currDot;
    }

    public int getCurrDotType(){
        return currDotType;
    }



    /**
     * init point candidates with each stored in a Point variable.
     * Then each point will be added into an ArrayList
     */
    private void initDotCandidates(){
        if( null!= dotCandidates) {
//            int[] TEXTURE_SIZE = new int[]{SCREEN_WIDTH, SCREEN_HEIGHT};
//            int expected_height = TEXTURE_SIZE[0]* DataCollectionActivity.Image_Size.getHeight()/DataCollectionActivity.Image_Size.getWidth();
//            if( expected_height < TEXTURE_SIZE[1] ){
//                TEXTURE_SIZE[1] = expected_height;
//            } else {
//                TEXTURE_SIZE[0] = TEXTURE_SIZE[1]*DataCollectionActivity.Image_Size.getWidth()/DataCollectionActivity.Image_Size.getHeight();
//            }
            int width_interval = SCREEN_WIDTH / ROW_COL_POINT_NUM;
            int height_interval = SCREEN_HEIGHT / ROW_COL_POINT_NUM;
            int offsetX = width_interval / 3;
            int offsetY = height_interval / 3;
            for (int i = 0; i < ROW_COL_POINT_NUM; ++i) {
                for (int j = 0; j < ROW_COL_POINT_NUM; ++j) {
                    dotCandidates.add(new Point(i * width_interval + offsetX, j * height_interval + offsetY));
                }
            }
        }
    }


    /**
     * randomly generate a dot, not the same as the previous one
     */
    private void generateDot(){
        int randIndex;
        do{
            randIndex = new Random().nextInt( dotCandidates.size() );
        } while( dotCandidates.get(randIndex).equals(currDot.x, currDot.y) );
        currDot = dotCandidates.get(randIndex);
        currDotType = new Random().nextInt(2);
    }



    /**
     * show the dot that is generated by this controller
     */
    private void showDot(){
        dotHolderLayout.removeAllViews();
        dotTextView = new TextView(ctxt);
//        dotTextView.setBackgroundResource(R.drawable.dot_r20);
        Bitmap redDot = Bitmap.createBitmap(90,90, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(redDot);
        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(ctxt, R.color.red_dot));
        canvas.translate(redDot.getWidth()/2f,redDot.getHeight()/2f);
        canvas.drawCircle(0,0, 40, paint);
        BitmapDrawable ob = new BitmapDrawable(ctxt.getResources(), redDot);
        dotTextView.setBackground(ob);
//        dotTextView.setText( currDotType ==POINT_TYPE_LEFT ? "L":"R" );
//        dotTextView.setTextAlignment( TextView.TEXT_ALIGNMENT_CENTER );
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = currDot.x;
        params.topMargin = currDot.y;
        dotTextView.setLayoutParams(params);
        //adding view to layout
        dotHolderLayout.addView(dotTextView);
    }


    /**
     *  show a dot with given position
     * @param x   x value of the point
     * @param y   y value of the point
     */
    public void showDot(int x, int y){
        if( null==dotHolderLayout ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolderLayout.removeAllViews();
        dotTextView = new TextView(ctxt);
        dotTextView.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = x;
        params.topMargin = y;
        dotTextView.setLayoutParams(params);
        //adding view to layout
        dotHolderLayout.addView(dotTextView);
        //set the current point
        currDot.set(x,y);
    }


    /**
     *  show a dot computed from the given ratio
     * @param width_ratio   the relative location regarding to the screen width
     * @param height_ratio  the relative location regarding to the screen height
     */
    public void showDot(float width_ratio, float height_ratio){
        if( null==dotHolderLayout ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolderLayout.removeAllViews();
        dotTextView = new TextView(ctxt);
        dotTextView.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = Math.round( width_ratio * SCREEN_WIDTH );
        params.topMargin = Math.round( height_ratio * SCREEN_HEIGHT );
        dotTextView.setLayoutParams(params);
        //adding view to layout
        dotHolderLayout.addView(dotTextView);
        //set the current point
        currDot.set(params.leftMargin, params.topMargin);
    }

    public void showDot(int x, int y, FrameLayout dotHolder){
        if( null==dotHolder ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolder.removeAllViews();
        TextView resDotTextView = new TextView(ctxt);
        resDotTextView.setBackgroundResource(R.drawable.dot_green25);
        //setting image position
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = x;
        params.topMargin = y;
        resDotTextView.setLayoutParams(params);
        //adding view to layout
        dotHolder.addView(resDotTextView);
    }

    public void showDots(double[] points, FrameLayout dotHolderLayout){
        int size = points.length / 2;
        dotHolderLayout.removeAllViews();
        for( int i=0; i<size; i++){
            TextView dotTextView = new TextView(ctxt);
            dotTextView.setBackgroundResource(R.drawable.ring_r5);
            //setting image position
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = Math.round( (int)points[2*i] );
            params.topMargin = Math.round( (int)points[2*i+1] );
            dotTextView.setLayoutParams(params);
            //adding view to layout
            dotHolderLayout.addView(dotTextView);
        }
    }

    public void show4CornerDots(){
        if( null==dotHolderLayout ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolderLayout.removeAllViews();
        TextView dot1 = new TextView(ctxt);
        dot1.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params1.leftMargin = 140;
        params1.topMargin = 220;
        dot1.setLayoutParams(params1);
        //adding view to layout
        dotHolderLayout.addView(dot1);
        //
        TextView dot2 = new TextView(ctxt);
        dot2.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.leftMargin = 560;
        params2.topMargin = 220;
        dot2.setLayoutParams(params2);
        //adding view to layout
        dotHolderLayout.addView(dot2);
        //
        TextView dot3 = new TextView(ctxt);
        dot3.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params3.leftMargin = 140;
        params3.topMargin = 880;
        dot3.setLayoutParams(params3);
        //adding view to layout
        dotHolderLayout.addView(dot3);
        //
        TextView dot4 = new TextView(ctxt);
        dot4.setBackgroundResource(R.drawable.dot_r20);
        //setting image position
        FrameLayout.LayoutParams params4 = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params4.leftMargin = 560;
        params4.topMargin = 880;
        dot4.setLayoutParams(params4);
        //adding view to layout
        dotHolderLayout.addView(dot4);
    }

    public void showAllCandidateDots(FrameLayout dotHolderLayout){
        if( null==dotHolderLayout ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolderLayout.removeAllViews();
        for(int i=0; i<dotCandidates.size(); ++i) {
            TextView dot = new TextView(ctxt);
            dot.setBackgroundResource(R.drawable.dot_r20);
            //setting image position
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = dotCandidates.get(i).x;
            params.topMargin = dotCandidates.get(i).y;
            dot.setLayoutParams(params);
            //adding view to layout
            dotHolderLayout.addView(dot);
        }
    }

    public void showAllCandidateDots(){
        if( null==dotHolderLayout ){
            String warning = "No layout is assigned for displaying dots";
            Log.d(LOG_TAG, warning);
            Toast.makeText(ctxt, warning, Toast.LENGTH_SHORT );
            return;
        }
        dotHolderLayout.removeAllViews();
        for(int i=0; i<dotCandidates.size(); ++i) {
            TextView dot = new TextView(ctxt);
            dot.setBackgroundResource(R.drawable.dot_r20);
            //setting image position
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = dotCandidates.get(i).x;
            params.topMargin = dotCandidates.get(i).y;
            dot.setLayoutParams(params);
            //adding view to layout
            dotHolderLayout.addView(dot);
        }
    }


    /**
     *  show a rectangle given corner points
     */
    public void showRect(int x1, int y1, int width, int height, FrameLayout graphicsHolder, boolean isHoldOn){
        CustomGraphics graphics = new CustomGraphics(ctxt, CustomGraphics.TYPE_RECTANGLE);
        ShapeDrawable sd = new ShapeDrawable(new RectShape());
        sd.getPaint().setColor(Color.GREEN);
        sd.getPaint().setStyle(Paint.Style.STROKE);
        sd.getPaint().setStrokeWidth(1);
        View view = new View(ctxt);
        view.setBackground(sd);
        if( !isHoldOn ) {
            graphicsHolder.removeAllViews();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = x1;
        params.topMargin = y1;
        graphicsHolder.addView(view, params);
    }


    /**
     *  show a green-filled rectangle given corner points
     */
    public void fillRect(int x1, int y1, int width, int height, FrameLayout graphicsHolder, int color, boolean isHoldOn){
        CustomGraphics graphics = new CustomGraphics(ctxt, CustomGraphics.TYPE_RECTANGLE);
        ShapeDrawable sd = new ShapeDrawable(new RectShape());
        sd.getPaint().setColor(ContextCompat.getColor(ctxt, color));
        sd.getPaint().setStyle(Paint.Style.FILL);
//        sd.getPaint().setStrokeWidth(1);
        View view = new View(ctxt);
        view.setBackground(sd);
        if( !isHoldOn ) {
            graphicsHolder.removeAllViews();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = x1;
        params.topMargin = y1;
        graphicsHolder.addView(view, params);
    }

    public void drawRandomBlockInCandidates(int width, int height, FrameLayout graphicsHolder, boolean isHoldOn){
        int randIndex;
        do{
            randIndex = new Random().nextInt( dotCandidates.size() );
        } while( dotCandidates.get(randIndex).equals(currDot.x, currDot.y) );
        Point rand = dotCandidates.get(randIndex);
        fillRect(rand.x, rand.y, width, height, graphicsHolder, R.color.desired_square_color, isHoldOn);
    }

    /**
     *  show an orange-circle
     */
    public void showCircle(int x1, int y1, int width, int height, int drawableId, FrameLayout graphicsHolder, boolean isHoldOn){
        View view = new View(ctxt);
        view.setBackground(ContextCompat.getDrawable( ctxt, drawableId));
        if( !isHoldOn ) {
            graphicsHolder.removeAllViews();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = x1;
        params.topMargin = y1;
        graphicsHolder.addView(view, params);
    }



    // draw bounding box and landmarks
    /**
     * Show bounding box on left-right-flipped image
     */
    public void showBoundingBox(int x1, int y1, int width, int height, int windowWidth, FrameLayout graphicsHolder, boolean isHoldOn) {
        CustomGraphics graphics = new CustomGraphics(ctxt, CustomGraphics.TYPE_RECTANGLE);
        ShapeDrawable sd = new ShapeDrawable(new RectShape());
        sd.getPaint().setColor(Color.GREEN);
        sd.getPaint().setStyle(Paint.Style.STROKE);
        sd.getPaint().setStrokeWidth(4);
        View view = new View(ctxt);
        view.setBackground(sd);
        if (!isHoldOn){
            graphicsHolder.removeAllViews();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.leftMargin = windowWidth - x1 - width;
        params.topMargin = y1;
        graphicsHolder.addView(view, params);
    }

    public void showBoundingBoxInLandscape(double[] faceRatio, int[] windowSize, FrameLayout graphicsHolder, boolean isHoldOn){
        double[] rotatedFaceRatio = new double[] {
                1 - faceRatio[1] - faceRatio[3],
                1 - faceRatio[0] - faceRatio[2],
                faceRatio[3],
                faceRatio[2]
        };
        int x = (int)(windowSize[0] * rotatedFaceRatio[0]);
        int y = (int)(windowSize[1] * rotatedFaceRatio[1]);
        int width = (int)(windowSize[0] * rotatedFaceRatio[2]);
        int height = (int)(windowSize[1] * rotatedFaceRatio[3]);
        showRect(x, y, width, height, graphicsHolder, isHoldOn);
    }

    public void showLandmarksInLandscape(double[] landmarks, int width, int height, int[] windowSize, FrameLayout graphicsHolder, boolean isHoldOn){
        if( !isHoldOn ){
            graphicsHolder.removeAllViews();
        }
        for(int i=0; i<landmarks.length; ){
            double xRatio = landmarks[i++]/width;
            double yRatio = landmarks[i++]/height;
            int newX = (int)(windowSize[0] * (1-yRatio));
            int newY = (int)(windowSize[1] * (1-xRatio));
            // display
            TextView resDotTextView = new TextView(ctxt);
            resDotTextView.setBackgroundResource(R.drawable.dot_green5);
            // setting image position
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = newX;
            params.topMargin = newY;
            params.width = 5;
            params.height = 5;
            resDotTextView.setLayoutParams(params);
            // adding view to layout
            graphicsHolder.addView(resDotTextView);
        }
    }

    public void clear(FrameLayout graphicsHolder){
        graphicsHolder.removeAllViews();
    }



    // draw estimation results

    public void draw4ClassRegrsResult(float[] loc, int[] txtrueSize, FrameLayout panel, boolean isShowDot){
        int x;
        int y;
        int width = txtrueSize[0] / 2;
        int height = txtrueSize[1] / 2;
        if( loc[0] < 0.5 && loc[1] < 0.5 ){
            x = 0;
            y = 0;
        } else if (loc[0] >= 0.5 && loc[1] < 0.5){
            x = txtrueSize[0] / 2;
            y = 0;
        } else if (loc[0] < 0.5 && loc[1] >= 0.5){
            x = 0;
            y =txtrueSize[1] / 2;
        } else {
            x = txtrueSize[0] / 2;
            y = txtrueSize[1] / 2;
        }
        if( isShowDot ) {
            showDot((int) (txtrueSize[0] * loc[0]), (int) (txtrueSize[1] * loc[1]), panel);
        }
//        fillRect(x, y, width, height, panel, true);
        int size = Math.min(width, height)/2;
        int cx = x + width/2 - size/2;
        int cy = y + height/2 - size/2;
        showCircle(cx, cy, size, size, R.drawable.ring_orange_10, panel,true);
    }

    public void draw6ClassRegrsResult(float[] loc, int[] txtrueSize, FrameLayout panel, boolean isShowDot) {
        int x;
        int y;
        int width = txtrueSize[0] / 2;
        int height = txtrueSize[1] / 3;
        if (loc[0] < 0.5 && loc[1] < 0.3333) {
            x = 0;
            y = 0;
        } else if (loc[0] < 0.5 && loc[1] >= 0.3333 && loc[1] < 0.6666) {
            x = 0;
            y = height;
        } else if (loc[0] < 0.5 && loc[1] >= 0.6666) {
            x = 0;
            y = 2 * height;
        } else if (loc[0] >= 0.5 && loc[1] < 0.3333) {
            x = width;
            y = 0;
        } else if (loc[0] >= 0.5 && loc[1] >= 0.3333 && loc[1] < 0.6666) {
            x = width;
            y = height;
        } else {
            x = width;
            y = 2 * height;
        }
        if( isShowDot ) {
            showDot((int) (txtrueSize[0] * loc[0]), (int) (txtrueSize[1] * loc[1]), panel);
        }
//        fillRect(x, y, width, height, panel, true);
        int size = Math.min(width, height)/2;
        int cx = x + width/2 - size/2;
        int cy = y + height/2 - size/2;
        showCircle(cx, cy, size, size, R.drawable.ring_orange_8, panel,true);
    }

    public void draw9ClassRegrsResult(float[] loc, int[] txtrueSize, FrameLayout panel, boolean isShowDot) {
        int x;
        int y;
        int width = txtrueSize[0] / 3;
        int height = txtrueSize[1] / 3;
        if (loc[0] < 0.3333 && loc[1] < 0.3333) {
            x = 0;
            y = 0;
        } else if (loc[0] < 0.3333 && loc[1] >= 0.3333 && loc[1] < 0.6666) {
            x = 0;
            y = height;
        } else if (loc[0] < 0.3333 && loc[1] >= 0.6666) {
            x = 0;
            y = 2 * height;
        } else if (loc[0] >= 0.3333 && loc[0] < 0.6666 && loc[1] < 0.3333) {
            x = width;
            y = 0;
        } else if (loc[0] >= 0.3333 && loc[0] < 0.6666 && loc[1] >= 0.3333 && loc[1] < 0.6666) {
            x = width;
            y = height;
        } else if (loc[0] >= 0.3333 && loc[0] < 0.6666 && loc[1] >= 0.6666) {
            x = width;
            y = 2 * height;
        } else if (loc[0] >= 0.6666 && loc[1] < 0.3333) {
            x = 2 * width;
            y = 0;
        } else if (loc[0] >= 0.6666 && loc[1] >= 0.3333 && loc[1] < 0.6666) {
            x = 2 * width;
            y = height;
        } else {
            x = 2 * width;
            y = 2 * height;
        }
        if( isShowDot ) {
            showDot((int) (txtrueSize[0] * loc[0]), (int) (txtrueSize[1] * loc[1]), panel);
        }
//        fillRect(x, y, width, height, panel, true);
        int size = Math.min(width, height)/2;
        int cx = x + width/2 - size/2;
        int cy = y + height/2 - size/2;
        showCircle(cx, cy, size, size, R.drawable.ring_orange_6, panel,true);
    }

    public void draw4ClassClassResult(int classNo, int[] txtrueSize, FrameLayout panel){
        int x;
        int y;
        int width = txtrueSize[0] / 2;
        int height = txtrueSize[1] / 2;
        switch (classNo){
            case 0:
                x = width;
                y = 0;
                break;
            case 1:
                x = width;
                y = height;
                break;
            case 2:
                x = 0;
                y = 0;
                break;
            case 3:
                x = 0;
                y = height;
                break;
            default:
                x = -1;
                y = 1;
                break;
        }
        if( x*y>=0 ) {
            fillRect(x, y, width, height, panel,  R.color.translucent_green, true);
        }
    }

    public void draw6ClassClassResult(int classNo, int[] txtrueSize, FrameLayout panel) {
        int x;
        int y;
        int width = txtrueSize[0] / 2;
        int height = txtrueSize[1] / 3;
        switch (classNo){
            case 0:
                x = width;
                y = 0;
                break;
            case 1:
                x = width;
                y = height;
                break;
            case 2:
                x = width;
                y = height * 2;
                break;
            case 3:
                x = 0;
                y = 0;
                break;
            case 4:
                x = 0;
                y = height;
                break;
            case 5:
                x = 0;
                y = height * 2;
                break;
            default:
                x = -1;
                y = 1;
                break;
        }
        if( x*y>=0 ) {
            fillRect(x, y, width, height, panel,  R.color.translucent_green, false);
        }
    }

    public void draw9ClassClassResult(int classNo, int[] txtrueSize, FrameLayout panel) {
        int x;
        int y;
        int width = txtrueSize[0] / 3;
        int height = txtrueSize[1] / 3;
        switch (classNo){
            case 0:
                x = width * 2;
                y = 0;
                break;
            case 1:
                x = width * 2;
                y = height;
                break;
            case 2:
                x = width * 2;
                y = height * 2;
                break;
            case 3:
                x = width;
                y = 0;
                break;
            case 4:
                x = width;
                y = height;
                break;
            case 5:
                x = width;
                y = height * 2;
                break;
            case 6:
                x = 0;
                y = 0;
                break;
            case 7:
                x = 0;
                y = height;
                break;
            case 8:
                x = 0;
                y = height * 2;
                break;
            default:
                x = -1;
                y = 1;
                break;
        }
        if( x*y>=0 ) {
            fillRect(x, y, width, height, panel,  R.color.translucent_green, false);
        }
    }




}
