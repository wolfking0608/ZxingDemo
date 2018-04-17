package com.wcyq.zxingdemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private             int    REQUEST_CODE_SCAN = 111;
    /**
     * 将要生成二维码的内容
     */
    private EditText  codeEdit;
    /**
     * 生成二维码代码
     */
    private Button    twoCodeBtn;
    /**
     * 用于展示生成二维码的imageView
     */
    private ImageView codeImg;
    /**
     * 生成一维码按钮 scanner
     */
    private Button    oneCodeBtn;
    /**
     * 二维码扫描
     */
    private Button    scannerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
    }

    /**
     * 用于初始化界面展示的view
     */
    private void initView() {
        codeEdit = (EditText) findViewById(R.id.code_edittext);
        twoCodeBtn = (Button) findViewById(R.id.code_btn);
        oneCodeBtn = (Button) findViewById(R.id.btn_code);
        scannerBtn = (Button) findViewById(R.id.scanner);
        codeImg = (ImageView) findViewById(R.id.code_img);

    }


    /**
     * 设置生成二维码和扫描二维码的事件
     */
    private void setListener() {
        twoCodeBtn.setOnClickListener(this);
        oneCodeBtn.setOnClickListener(this);
        scannerBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_code://一维码
                oneCodeCreate();
                break;
            case R.id.code_btn://二维码
                twoCodeCreate();
                break;
            case R.id.scanner:{
                AndPermission.with(this).permission(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE).callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        Intent intent = new Intent(MainActivity.this, CaptureActivity.class);

                                /*ZxingConfig是配置类  可以设置是否显示底部布局，闪光灯，相册，是否播放提示音  震动等动能
                                * 也可以不传这个参数
                                * 不传的话  默认都为默认不震动  其他都为true
                                * */

                        ZxingConfig config = new ZxingConfig();
                        config.setPlayBeep(true);
                        config.setShake(true);
                        intent.putExtra(com.yzq.zxinglibrary.common.Constant.INTENT_ZXING_CONFIG, config);

                        startActivityForResult(intent, REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {

                        Uri packageURI = Uri.parse("package:" + MainActivity.this.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(intent);

                        Toast.makeText(MainActivity.this, "没有权限无法扫描呦", Toast.LENGTH_LONG).show();
                    }
                }).start();
            }
                break;
            default:
                break;
        }
    }

    private void twoCodeCreate() {
        String str = codeEdit.getText().toString().trim();
        Bitmap bmp = null;
        try {
            if (str != null && !"".equals(str)) {
                bmp = CreateTwoDCode(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bmp != null) {
            codeImg.setImageBitmap(bmp);
        }
    }

    private void oneCodeCreate() {
        String str = codeEdit.getText().toString().trim();
        int size = str.length();
        for (int i = 0; i < size; i++) {
            int c = str.charAt(i);
            if ((19968 <= c && c < 40623)) {
                Toast.makeText(MainActivity.this, "生成条形码的时刻不能是中文", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Bitmap bmp = null;
        try {
            if (str != null && !"".equals(str)) {
                bmp = CreateOneDCode(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bmp != null) {
            codeImg.setImageBitmap(bmp);
        }
    }


    /**
     * 将指定的内容生成成二维码
     * @param content 将要生成二维码的内容
     * @return 返回生成好的二维码事件
     * @throws Exception WriterException异常
     */
    public Bitmap CreateTwoDCode(String content) throws Exception {
        // 生成二维矩阵,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 300, 300);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        // 二维矩阵转为一维像素数组,也就是一直横着排了
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


    /**
     * 用于将给定的内容生成成一维码 注：目前生成内容为中文的话将直接报错，要修改底层jar包的内容
     * @param content 将要生成一维码的内容
     * @return 返回生成好的一维码bitmap
     * @throws WriterException WriterException异常
     */
    public Bitmap CreateOneDCode(String content) throws WriterException {
        // 生成一维条码,编码时指定大小,不要生成了图片以后再进行缩放,这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, 500, 200);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 通过像素数组生成bitmap,具体参考api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String url = "";
                try {
                    url = data.getStringExtra(com.yzq.zxinglibrary.common.Constant.CODED_CONTENT);
                    Intent intent = new Intent();
                    //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    Log.e(TAG, "url------" + url);
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(url);
                    intent.setData(content_url);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, url, Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}
