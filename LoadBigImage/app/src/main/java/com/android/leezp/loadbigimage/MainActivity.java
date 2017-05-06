package com.android.leezp.loadbigimage;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.leezp.loadbigimage.Listener.OnDoubleClickListener;
import com.android.leezp.loadbigimage.ThreadInterface.LoadImgSuccess;

/**
 * 4.4之前：
 * Uri: content://com.android.externalstorage.documents/document/primary%3A1.png。
 * Path: /storage/emulated/0/1.png
 * 4.4之后：
 * Uri: content://com.android.providers.media.documents/document/image%3A16025。
 * Path: /storage/emulated/0/very_big.jpg
 */

public class MainActivity extends Activity implements OnDoubleClickListener.DoubleClickCallback{
    //整体布局
    private FrameLayout layout;

    //图片
    private ImageView bigImg;
    //显示比例的文本
    private TextView rate;

    //存储此时缩放比例
    private int scale;

    //加载图片成功将其设置在ImageView上的Interface
    private LoadImgSuccess loadImgSuccess = new LoadImgSuccess() {
        @Override
        public void loadImage(final Bitmap bitmap) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bigImg.setImageBitmap(bitmap);
                    rate.setText("1:"+scale);
                }
            });
        }

        @Override
        public void loadError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    rate.setText("该存储位置没有该图片，请认真审查！");
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        initView();

        initEvent();
    }

    /**
     * 权限允许方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (!(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(MainActivity.this, "请允许该权限，才可以运行该程序！", Toast.LENGTH_SHORT);
                    finish();
                }
        }
    }

    /**
     * 初始化对应的控件上的事件
     */
    private void initEvent() {
        layout.setOnTouchListener(new OnDoubleClickListener(this));
        bigImg.setOnTouchListener(new OnDoubleClickListener(this));
    }

    /**
     *  初始化布局上的控件以及视图
     */
    private void initView() {
        layout = (FrameLayout) findViewById(R.id.activity_main);

        bigImg = (ImageView) findViewById(R.id.activity_main_bigPicture);
        rate = (TextView) findViewById(R.id.activity_main_rate);

        initBigImg();
    }

    /**
     * 将大像素的图片显示在ImageView上
     */
    private void initBigImg() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                BitmapFactory.Options opt = new BitmapFactory.Options();
                //将该选项类的inJustDecodeBounds属性设置为true,表示禁止为bitmap分配内存，但可以得到图片的一些高、宽等信息
                opt.inJustDecodeBounds = true;
//              BitmapFactory.decodeResource(getResources(), R.drawable.activity_main_test, opt);
                BitmapFactory.decodeFile("/storage/emulated/0/activity_main_big_picture.png", opt);

                int imageHeight = opt.outHeight;
                int imageWidth = opt.outWidth;

                Display display = getWindowManager().getDefaultDisplay();
                Point point = new Point();

                //因为getRealSize方法只能在API 17以上版本能正常运行，所以根据不同情况，获取手机屏幕的宽度与高度
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    display.getRealSize(point);
                } else {
                    display.getSize(point);
                }
                int screenHeight = point.y;
                int screenWidth = point.x;

                scale = 1;
                int scaleWidth = imageWidth/screenWidth;
                int scaleHeight = imageHeight/screenHeight;
                if (scaleWidth>=scaleHeight && scaleWidth>1) {
                    scale = scaleHeight;
                } else if (scaleWidth<scaleHeight && scaleHeight>1) {
                    scale = scaleWidth;
                }

                opt.inSampleSize = scale;
                opt.inJustDecodeBounds = false;

//              Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.activity_main_test, opt);
                Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/activity_main_big_picture.png", opt);
                if (bitmap != null) {
                    loadImgSuccess.loadImage(bitmap);
                } else {
                    loadImgSuccess.loadError();
                }
            }
        }).start();
    }

    @Override
    public void onDoubleClick() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //处理双点击事件
                if (scale*2 > 10000) {
                    initBigImg();
                } else {
                    scale = scale * 2;
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    //每次双击，就将图片缩小一半
                    opt.inSampleSize = scale;
                    //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.activity_main_test, opt);
                    Bitmap bitmap = BitmapFactory.decodeFile("/storage/emulated/0/activity_main_big_picture.png", opt);
                    if (bitmap != null) {
                        loadImgSuccess.loadImage(bitmap);
                    } else {
                        loadImgSuccess.loadError();
                    }
                }
            }
        }).start();
    }
}