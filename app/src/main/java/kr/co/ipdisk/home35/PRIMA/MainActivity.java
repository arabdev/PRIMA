package kr.co.ipdisk.home35.PRIMA;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    ImageView imageViewInput;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageVIewDrawing;
    ImageView imageViewOutput;
    ImageView imageViewInterest;

    private Mat img_input;
    private Mat img_2;
    private Mat img_3;
    private Mat img_drawing;
    private Mat img_output;
    private Mat img_interest;

    private static final String TAG = "opencv";
    static final int PERMISSION_REQUEST_CODE = 1;
    String[] PERMISSIONS  = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    private boolean hasPermissions(String[] permissions) {
        int ret = 0;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)){
                //퍼미션 허가 안된 경우
                return false;
            }

        }
        //모든 퍼미션이 허가된 경우
        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        //마시멜로( API 23 )이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void copyFile(String filename) {
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d( TAG, "copyFile :: 다음 경로로 파일복사 "+ pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 "+e.toString() );
        }

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults){
        switch(permsRequestCode){

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (!writeAccepted )
                        {
                            showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                            return;
                        }else
                        {
                            read_image_file();
                            imageprocess_and_showResult();
                        }
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {

        final AlertDialog.Builder myDialog = new AlertDialog.Builder(  MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }

            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(kr.co.ipdisk.home35.PRIMA.R.layout.activity_main);

        imageViewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageView2 = (ImageView)findViewById(R.id.imageView2);
        imageView3 = (ImageView)findViewById(R.id.imageView3);
        imageVIewDrawing = (ImageView)findViewById(R.id.imageViewDrawing);
        imageViewOutput = (ImageView)findViewById(R.id.imageViewOutput);
        imageViewInterest = (ImageView)findViewById(R.id.imageViewInterest);

        if (!hasPermissions(PERMISSIONS)) { //퍼미션 허가를 했었는지 여부를 확인
            requestNecessaryPermissions(PERMISSIONS);//퍼미션 허가안되어 있다면 사용자에게 요청
        } else {
            //이미 사용자에게 퍼미션 허가를 받음.
            read_image_file();
            imageprocess_and_showResult();
        }
    }

    private void imageprocess_and_showResult() {

        imageprocessing(img_input.getNativeObjAddr(), img_2.getNativeObjAddr(), img_3.getNativeObjAddr(), img_drawing.getNativeObjAddr(), img_output.getNativeObjAddr(), img_interest.getNativeObjAddr());

        Bitmap bitmapInput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_input, bitmapInput);
        imageViewInput.setImageBitmap(bitmapInput);

        Bitmap bitmapImg2 = Bitmap.createBitmap(img_2.cols(), img_2.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_2, bitmapImg2);
        imageView2.setImageBitmap(bitmapImg2);

        Bitmap bitmapImg3 = Bitmap.createBitmap(img_3.cols(), img_3.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_3, bitmapImg3);
        imageView3.setImageBitmap(bitmapImg3);

        Bitmap bitmapDrawing = Bitmap.createBitmap(img_drawing.cols(), img_drawing.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_drawing, bitmapDrawing);
        imageVIewDrawing.setImageBitmap(bitmapDrawing);

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageViewOutput.setImageBitmap(bitmapOutput);

        Bitmap bitmapInterest = Bitmap.createBitmap(img_interest.cols(), img_interest.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_interest, bitmapInterest);
        imageViewInterest.setImageBitmap(bitmapInterest);

    }

    private void read_image_file() {
        copyFile("ball.jpg");

        img_input = new Mat();
        img_2 = new Mat();
        img_3 = new Mat();
        img_drawing = new Mat();
        img_output = new Mat();
        img_interest = new Mat();

        loadImage("ball.jpg", img_input.getNativeObjAddr());
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void loadImage(String imageFileName, long img);
    public native void imageprocessing(long inputImage, long img2, long img3, long drawing, long outputImage, long interest);
}
