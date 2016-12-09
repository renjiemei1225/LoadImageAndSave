package ren.jiemei.loadimageandsave;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getCanonicalName();
    private ImageView imageview;
    private Button load;

    String path = "http://img06.tooopen.com/images/20161120/tooopen_sl_187242346264.jpg";

    private Bitmap bitmap;
    private File picfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageview = (ImageView) findViewById(R.id.imageview);
        load = (Button) findViewById(R.id.btn_load);
        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = createPicfile();
                if (null != file){

                    String absolutePath = file.getAbsolutePath();
                    Log.e(TAG, "onClick: "+absolutePath );
                    String filepath = absolutePath+getFilePath(path);
                    Log.e(TAG, "onClick: "+filepath );
                    picfile = new File(filepath);
                    if (picfile.exists()){
                        Log.e(TAG, "onClick: "+"文件存在" );

                        bitmap = BitmapFactory.decodeFile(picfile.getAbsolutePath());
                        if (null != bitmap){
                            Message msg = Message.obtain();
                            msg.what =2;
                            handler.sendMessage(msg);
                        }else {
                            Log.e(TAG, "onClick: "+"文件下载错误" );
                        }

                    }else {
                        Log.e(TAG, "onClick: "+"文件不存在" );
                        try {
                            picfile.createNewFile();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    loadpicture(picfile,path);
                                }
                            }).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }else {
                    Log.e(TAG, "createPicfile: "+"没有sd卡" );
                }

            }
        });
    }

    private void loadpicture(File picfile, String path) {
        try {
            FileOutputStream fos = new FileOutputStream(picfile);
            URL url = new URL(path);
            HttpURLConnection httpconn = (HttpURLConnection) url.openConnection();
            httpconn.setConnectTimeout(5000);
            httpconn.setRequestMethod("GET");
            int code = httpconn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == code){
                Log.e(TAG, "loadpicture: "+"正在下载、、、、" );
                Log.e(TAG, "loadpicture: "+"下载文件大小:"+ httpconn.getContentLength());
                InputStream is = httpconn.getInputStream();
                byte[] buff = new byte[512];
                int len ;

                while((len = (is.read(buff))) != -1){
                    fos.write(buff,0,len);

                }
                is.close();
                fos.flush();
                fos.close();
                Log.e(TAG, "loadpicture: "+"下载完成、、" );

                bitmap = BitmapFactory.decodeFile(picfile.getAbsolutePath());
                if (null != bitmap) {
                    Message msg = Message.obtain();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(String path) {
        if (null != path){
            StringBuilder builder = new StringBuilder(path.trim());

            int i = builder.lastIndexOf("/");
            String filepath = builder.substring(i);
            return filepath;
        }else {
            return null;
        }
    }

    private File createPicfile() {
        if (Environment.getExternalStorageState().equals(Environment.getExternalStorageState())){
            String s = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Picture";
            File file = new File(s);
            if (!file.exists()){
                file.mkdir();
            }
            return file;
        }else {
            Log.e(TAG, "createPicfile: "+"没有sd卡" );
        }
        return null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 1){
                imageview.setImageBitmap(bitmap);
                Log.e(TAG, "handleMessage: "+"网络下载图片显示 ");
            }
            if (msg.what == 2){
                imageview.setImageBitmap(bitmap);
                Log.e(TAG, "handleMessage: "+"本地获取图片显示" );
            }

        }
    };
}
