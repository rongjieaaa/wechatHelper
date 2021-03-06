package com.naxions.www.wechathelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;
import com.naxions.www.wechathelper.util.DateUtil;
import com.naxions.www.wechathelper.util.FileUtil;
import com.naxions.www.wechathelper.util.FilterUtil;
import com.naxions.www.wechathelper.util.Md5Utils;
import com.naxions.www.wechathelper.util.PasswordUtiles;
import com.tencent.wcdb.Cursor;
import com.tencent.wcdb.database.SQLiteCipherSpec;
import com.tencent.wcdb.database.SQLiteDatabase;
import com.threekilogram.objectbus.bus.ObjectBus;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//import com.tencent.wcdb.database.SQLiteDatabaseHook;

/**
 * @Author: zengke
 * @Data: 2018.12
 */
public class MainActivity extends AppCompatActivity implements OnDownloadListener, View.OnClickListener, OnButtonClickListener {

    public MainActivity mActivity;

    private SharedPreferences preferences;
    private static CSVPrinter contactCsvPrinter;
    private static CSVPrinter messageCsvPrinter;
    private static final ObjectBus TASK = ObjectBus.newList();

    /**
     * 微信数据库路径
     */
    public final String WX_ROOT_PATH = "/data/data/com.tencent.mm/";
    public final String WX_DB_DIR_PATH = WX_ROOT_PATH + "MicroMsg";
    public final String WX_DB_FILE_NAME = "EnMicroMsg.db";
    public final String WXPACKAGENAME = "com.tencent.mm";
    public final String COPY_WX_DATA_DB = "wx_data.db";
    /**
     * 拷贝到sd 卡的路径
     */
    public String copyPath = Environment.getExternalStorageDirectory().getPath() + "/";
    String copyFilePath = copyPath + COPY_WX_DATA_DB;

    //sp 的 key
    public final String USERNAME = "userName";
    public final String USERINFO = "userInfo";
    //根据输入的不同微信号切换到不同的key
    public String LAST_UPDATE_TIME = EMPTY;

    /**tv_addTime_start
     * apk下载
     */
    private DownloadManager manager;

    /**
     * 上传导出按钮
     */
    private Button btn_updateData;
    private Button btn_export;
    private Button btn_updelay;
    private Button btn_addbytime;
    private TextView tv_export_all_message;
    /**
     * 上次上传时间
     */
    private TextView tv_updateTime;
    private TextView tv_title;
    //描述
    private TextView des_text;
    /**
     * 正在上传提示的 loadingView
     */
    private CustomDialog loadingDialog;

    /**
     * 转圈圈的图
     */
    private AVLoadingIndicatorView loadingView;
    /**
     * 正在上传的提示
     */
    private TextView mRemindText;
    /**
     * 上传成功的图标
     */
    private ImageView iv_success;
    /**
     * 上传失败的图标
     */
    private ImageView iv_fail;
    /**
     * 姓名输入框
     */
    private EditText et_name;
    /**
     * 补充上传日期输入框
     */
    private TextView tv_addTime_start;
    private TextView tv_addTime_end;
    /**
     * 用户姓名
     */
    private String userName;
    /**
     * 点击上传按钮是记录的当前时间
     */
    private String currentTime;
    /**
     * 格式化后的上次上传时间
     */
    private String lastUpdateTime;
    /**
     * 上次上传时间的时间
     */
    private Long longLastUpdateTime; /**
     * 补充上传的时间
     */
    private  String addTime ;
    private  String endTime ;
    /** * 补充上传的时间戳
     */
    private  Long addTimestamp  ;
    private  Long endTimestamp  ;
    /**
     * 文件上传
     */
    private File file1;
    private File file2;
    /**
     * baseUrl
     */
    //测试
     String baseUrl = "http://123.56.95.29:7083/android/wechat/";
    //正式
//     String baseUrl = "http://47.93.121.23:10001/android/wechat/";
     //sql 语句
     String contactSql = "select * from rcontact where verifyFlag = 0 and  type != 2 and type != 0 and type != 33 and nickname != ''and nickname != '文件传输助手'";
     String messageSql = "select * from message where  createTime >";
     String chatroomSql = "select * from chatroom";
     String masSendInfoSql = "select * from massendinfo";
    /**
     * 是否处于 debug 模式
     */
    public static boolean isDebug = true;
    /**
     * 是否选中全部的聊天记录
     */
    public static boolean isSelectAll = false;

    public static boolean isAddAll = false;

    /**
     * 是否仅导出不上传
     */
    public static final String EMPTY = "";
    public static final String ZERO = "0";
    public static final String SUCCESS_CODE = "200";

    /**
     * 点击上传按钮时的时时间戳
     */
    private String mTimeStamp;
    private Object updateCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //沉浸式状态栏
        if (Build.VERSION.SDK_INT >= 23) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        mActivity = this;
        initView();
        initData();


    }


    private void initView() {
        btn_updateData = findViewById(R.id.btn_updateData);
        btn_export = findViewById(R.id.btn_export);
        tv_updateTime = findViewById(R.id.tv_updateTime);
        tv_title = findViewById(R.id.tv_title);
        tv_export_all_message = findViewById(R.id.tv_export_all_message);
        btn_addbytime = findViewById(R.id.btn_addbytime);
        btn_updelay = findViewById(R.id.btn_updelay);
        et_name = findViewById(R.id.et_name);
        tv_addTime_start = findViewById(R.id.tv_addTime_start);
        tv_addTime_end = findViewById(R.id.tv_addTime_end);
        des_text = findViewById(R.id.des_text);
        btn_updateData.setOnClickListener(this);
        btn_export.setOnClickListener(this);
        tv_export_all_message.setOnClickListener(this);
        btn_addbytime.setOnClickListener(this);
        tv_addTime_start.setOnClickListener(this);
        tv_addTime_end.setOnClickListener(this);
        btn_updelay.setOnClickListener(this);
    }

    private void initData() {
        //sp中获取销售代表名字和上次上传时间
        preferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
        userName = preferences.getString(USERNAME, EMPTY);
        //多账号切换时,key不同,要拿的时间也不同
        LAST_UPDATE_TIME = userName;
        lastUpdateTime = preferences.getString(LAST_UPDATE_TIME, EMPTY);
        //赋值
        et_name.setText(userName.toCharArray(), 0, userName.length());
        longLastUpdateTime = Long.valueOf(DateUtil.date2Timestamp(lastUpdateTime));
        tv_updateTime.setText(lastUpdateTime);
        if (EMPTY.equals(et_name.getText().toString())) {
            tv_updateTime.setText("暂无上传时间");
        }
        //获取上次上传的时间
        if (EMPTY.equals(lastUpdateTime) || ZERO.equals(lastUpdateTime)) {
            //既没有微信号也没有上次上传时间(第一次安装),将时间重置为0
            tv_updateTime.setText("暂无上传时间");
            longLastUpdateTime = Long.parseLong(ZERO);
        } else {
            //有上次上传时间,赋值并记录
            tv_updateTime.setText(lastUpdateTime);
            longLastUpdateTime = Long.valueOf(DateUtil.date2Timestamp(lastUpdateTime));
            if (isDebug) {
                Log.e("query sp中保存的上次上传时间", DateUtil.timeStamp2Date(longLastUpdateTime + EMPTY));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //上传聊天记录按钮
            case (R.id.btn_updateData):
                isSelectAll = false;
                isAddAll = false;
                uploadData();
                break;
            //导出联系人按钮
            case (R.id.btn_export):
                startActivity(new Intent(mActivity, ExportActivity.class));
                break;
            //强制更新所有聊天记录
            case (R.id.tv_export_all_message):
                isSelectAll = true;
                isAddAll = true;
                uploadData();
                break;
            //强制更新输入日期到下载的所有聊天记录
            case (R.id.btn_addbytime):
                addTime = tv_addTime_start.getText().toString();
                addTimestamp= DateUtil.date2Timestamp(addTime+":000");
                endTime = tv_addTime_end.getText().toString();
                endTimestamp= DateUtil.date2Timestamp(endTime+":000");
                //时间戳格式不对,直接提示
                if(addTimestamp ==0 ){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"您输入的起始日期格式错误",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                //时间戳格式不对,直接提示
                if(endTimestamp == 0 ){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"您输入的截止日期格式错误",Toast.LENGTH_LONG).show();
                        }
                    });

                    return;
                }

                if(endTimestamp <= addTimestamp){
                    Toast.makeText(getApplicationContext(),"输入的截止日期小于/等于起始日期",Toast.LENGTH_LONG).show();
                    return;
                }

                isSelectAll = true;
                isAddAll = false;
                uploadData();
                break;

            case (R.id.btn_updelay):
                Intent intent2 = new Intent(mActivity,PlayerMusicService.class);
                startService(intent2);
                Intent intent = new Intent(mActivity,DaemonService.class);
                startService(intent);
                //延迟上传
                addTime = tv_addTime_start.getText().toString();
                addTimestamp= DateUtil.date2Timestamp(addTime+":000");
                endTime = tv_addTime_end.getText().toString();
                endTimestamp= DateUtil.date2Timestamp(endTime+":000");

                //选择了起始和截止日期
                if(endTimestamp != 0 && addTimestamp !=0  ){
                    isSelectAll = true;
                    isAddAll = false;
                    //提示
                   Toast.makeText(mActivity,"五个小时后将自动执行补充上传,请不要关闭本软件,并在本页面下锁屏",Toast.LENGTH_LONG).show();
                }else{
                    isSelectAll = false;
                    isAddAll = false;
                    //提示
                    Toast.makeText(mActivity,"五个小时后将自动执行增量上传,请不要关闭本软件,并在本页面下锁屏",Toast.LENGTH_LONG).show();
                }
                System.out.println("等待执行");
                new Handler().postDelayed(new Runnable() {
                     @Override
                        public void run() {
                         System.out.println("执行了");
                                uploadData();
                        }
//                    }, 300000); //五分钟
                    }, 3600000); //一小时
//                    }, 18000000); //五小时

                break;
            case (R.id.tv_addTime_start):
                //时间选择器
                DatePickDialog dialog_start = new DatePickDialog(this);
                //设置上下年分限制
                dialog_start.setYearLimt(5);
                //设置标题
                dialog_start.setTitle("选择时间");
                //设置类型
                dialog_start.setType(DateType.TYPE_YMDHM);
                //设置消息体的显示格式，日期格式
                dialog_start.setMessageFormat("yyyy-MM-dd HH:mm");
                //设置选择回调
                dialog_start.setOnChangeLisener(null);
                //设置点击确定按钮回调
                dialog_start.setOnSureLisener(new OnSureLisener() {
                    @Override
                    public void onSure(Date date) {
                        tv_addTime_start.setText(DateUtil.dateToString(date).substring(0,17)+"00");
                    }
                });
                dialog_start.show();
                break;

            case (R.id.tv_addTime_end):
                DatePickDialog dialog_end = new DatePickDialog(this);
                //设置上下年分限制
                dialog_end.setYearLimt(5);
                //设置标题
                dialog_end.setTitle("选择时间");
                //设置类型
                dialog_end.setType(DateType.TYPE_YMDHM);
                //设置消息体的显示格式，日期格式
                dialog_end.setMessageFormat("yyyy-MM-dd HH:mm");
                //设置选择回调
                dialog_end.setOnChangeLisener(null);
                //设置点击确定按钮回调
                dialog_end.setOnSureLisener(new OnSureLisener() {
                    @Override
                    public void onSure(Date date) {
                        tv_addTime_end.setText(DateUtil.dateToString(date).substring(0,17)+"00");
                    }
                });
                dialog_end.show();
                break;
            default:
                break;
        }
    }

    /**
     * 复制数据库解析并上传数据
     */
    private void uploadData() {
        //判断姓名是否为空
        if (EMPTY.equals(et_name.getText().toString().trim())) {
            Toast.makeText(mActivity, "请先输入您的微信号!", Toast.LENGTH_SHORT).show();
            return;
        }
        //保存姓名
        if (preferences == null) {
            preferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
        }
        //可能修改过用户 更新userName和上传时间;
        userName = et_name.getText().toString().trim();
        LAST_UPDATE_TIME = userName;
        if (preferences == null) {
            preferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
        }
        lastUpdateTime = preferences.getString(LAST_UPDATE_TIME, EMPTY);
        //赋值
        if (EMPTY.equals(lastUpdateTime)) {
            tv_updateTime.setText("暂无上传时间");
            longLastUpdateTime = Long.valueOf(0);
        } else {
            longLastUpdateTime = Long.valueOf(DateUtil.date2Timestamp(lastUpdateTime));
            tv_updateTime.setText(lastUpdateTime);
        }
        et_name.setText(userName.toCharArray(), 0, userName.length());
        //保存姓名
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(USERNAME, et_name.getText().toString().trim());
        edit.commit();
        //判断是否安装了微信
        if (isWeixinAvilible()) {
            //显示 loadingView
            if (loadingDialog == null) {
             loadingDialog = new CustomDialog(mActivity, R.style.customDialog, R.layout.layout_loading_dialog);
             }
             loadingDialog.setCancelable(false);

             loadingDialog.show();
             loadingView = loadingDialog.findViewById(R.id.loadingView);
             mRemindText = loadingDialog.findViewById(R.id.text);
             iv_success = loadingDialog.findViewById(R.id.iv_success);
             iv_fail = loadingDialog.findViewById(R.id.iv_fail);
             mRemindText.setText("正在从微信中导出聊天记录,请稍候");
             loadingView.setVisibility(View.VISIBLE);
             iv_success.setVisibility(View.INVISIBLE);
             iv_fail.setVisibility(View.INVISIBLE);
             //获取上次上传时间并赋值
             getLastUploadTime();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, "请先安装微信", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 请求网络获取上一次的上传时间,
     */
    private void getLastUploadTime() {
        // 创建网络处理的对象
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(50, TimeUnit.SECONDS)
                .build();

        //创建一个请求对象，传入URL地址和相关数据的键值对的对象
        Request request = new Request.Builder()
                .url(baseUrl + "upload/time/get/" + userName)
                .get().build();

        //创建一个能处理请求数据的操作类
        Call call = client.newCall(request);

        //使用异步任务的模式请求数据
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.e("query获取上次的上传时间的 错误信息", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //请求失败直接不让上传,显示失败
                        getUploadTimeError("无法获取您的上传时间,请联系开发人员");
                        Toast.makeText(mActivity, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String time = response.body().string();
                if (isDebug) {
                    Log.e("query 获取上次的上传时间==", time);
                }
                try {
                    JSONObject jsonOtimebject = new JSONObject(time);
                    Object code = jsonOtimebject.get("code");
                    final Object description = jsonOtimebject.get("description");
                    if (SUCCESS_CODE.equals(code.toString())) {
                        Object data = jsonOtimebject.get("data");
                        JSONObject jsondata = new JSONObject(data.toString());
                        final String messageUploadTime = jsondata.getString("messageUploadTime");
                        if (isDebug) {
                            Log.e("query 获取上次的上传时间==", messageUploadTime);
                        }
                        //返回的时间不为空再赋值,保存,不然就不处理,直接获取sp 的时间
                        if (!EMPTY.equals(messageUploadTime) && !ZERO.equals(messageUploadTime)) {
                            //赋值上次上传时间
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv_updateTime.setText(messageUploadTime);
                                    longLastUpdateTime = Long.valueOf(DateUtil.date2Timestamp(messageUploadTime));
                                    //保存到 sp
                                    if (preferences == null) {
                                        preferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
                                    }
                                    SharedPreferences.Editor edit = preferences.edit();
                                    edit.putString(LAST_UPDATE_TIME, messageUploadTime);
                                    edit.commit();
                                    go2GetData();
                                }
                            });
                        } else {
                            go2GetData();
                        }

                    } else {
                        getUploadTimeError("你无法获取上传时间,请联系开发人员");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, description.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取当前时间并一步执行拷贝解密操作
     */
    private void go2GetData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //获取当前时间,并转换格式
                mTimeStamp = DateUtil.getTimeStamp();
                currentTime = DateUtil.timeStamp2Date(mTimeStamp);
                if (isDebug) {
                    Log.e("query当前时间值", DateUtil.timeStamp2Date(mTimeStamp));
                }
                //异步执行文件拷贝和数据查询操作,防止 dialog 不显示
                new MyTask().execute();
            }
        });
    }


    public class MyTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            //拷贝前先提示正在处理
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            //获取root权限
            PasswordUtiles.execRootCmd("chmod 777 -R " + WX_ROOT_PATH);
            //获取root权限
            PasswordUtiles.execRootCmd("chmod 777 -R " + copyFilePath);

            String password = PasswordUtiles.initDbPassword(mActivity);
            String uid = PasswordUtiles.initCurrWxUin(mActivity);
            try {
                String path = WX_DB_DIR_PATH + "/" + Md5Utils.md5Encode("mm" + uid) + "/" + WX_DB_FILE_NAME;
                if (isDebug) {
                    Log.e("path", copyFilePath);
                    Log.e("path===", path);
                    Log.e("path", password);
                }
                if (password.equals(EMPTY) || password == null) {
                    getUploadTimeError("密码获取失败");
                }
                //微信原始数据库的地址
                File wxDataDir = new File(path);

                //将微信数据库拷贝出来，因为直接连接微信的db，会导致微信崩溃
                FileUtil.copyFile(wxDataDir.getAbsolutePath(), copyFilePath);
                File file = new File(copyFilePath);
                //将微信数据库导出到sd卡操作sd卡上数据库
                System.out.println(file.length()+"================================");
                openWxDb(file, mActivity, password);
            } catch (Exception e) {
                if (isDebug) {
                    Log.e("path", e.getMessage());
                }
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //完成后消失
            // loadingDialog.dismiss();
        }
    }

    /**
     * 连接数据库
     */
    public void openWxDb(File dbFile, final Activity mContext, String mDbPassword) {
//        SQLiteDatabase.loadLibs(mContext);
//        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
//            @Override
//            public void preKey(SQLiteDatabase database) {
//            }
//
//            @Override
//            public void postKey(SQLiteDatabase database) {
//                database.rawExecSQL("PRAGMA cipher_migrate;");
//            }
//        };


        SQLiteCipherSpec cipher = new SQLiteCipherSpec()  // 加密描述对象
                .setPageSize(1024)        // SQLCipher 默认 Page size 为 1024
                .setSQLCipherVersion(1);  // 1,2,3 分别对应 1.x, 2.x, 3.x 创建的 SQLCipher 数据库
        // 如以前使用过其他PRAGMA，可添加其他选项


        try {
            //打开数据库连接
//            final SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile.getPath(), mDbPassword, null, hook);
            System.out.println(dbFile.length()+"================================");
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
                    dbFile,     // DB 路径
                    mDbPassword.getBytes(),  // WCDB 密码参数类型为 byte[]
                    cipher,                 // 上面创建的加密描述对象
                    null,                   // CursorFactory
                    null                    // DatabaseErrorHandler
                    // SQLiteDatabaseHook 参数去掉了，在cipher里指定参数可达到同样目的
            );
            runRecontact(mContext, db);
        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");
                }
            });
        }
    }

    /**
     * 微信好友信息
     *
     * @param mContext
     * @param db
     */
    public void runRecontact(final Activity mContext, final SQLiteDatabase db) {

        TASK.toPool(new Runnable() {
            @Override
            public void run() {
                getRecontactData(db);
            }
        }).toMain(new Runnable() {
            @Override
            public void run() {
                //Toast.makeText(mContext, "文件导出完毕完毕", Toast.LENGTH_LONG).show();
            }
        }).run();
    }

    /**
     * 获取当前用户的微信所有联系人
     */
    public void getRecontactData(SQLiteDatabase db) {
        Cursor cursor1 = null;
        //取消联系人的电话号码过滤
       // boolean b ;

        try {
            //新建文件保存联系人信息
            file1 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + et_name.getText().toString().trim() + "ΞcontactΞfile" + ".csv");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file1), "UTF-8"));
            contactCsvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("userName", "nickName", "alias", "conRemark", "type"));
            // 查询所有联系人verifyFlag!=0:公众号等类型，群里面非好友的类型为4，未知类型2）
            cursor1 = db.rawQuery(contactSql, null);
            while (cursor1.moveToNext()) {
                String userName = cursor1.getString(cursor1.getColumnIndex("username"));
                String nickName = cursor1.getString(cursor1.getColumnIndex("nickname"));

                String alias = cursor1.getString(cursor1.getColumnIndex("alias"));
                String conRemark = cursor1.getString(cursor1.getColumnIndex("conRemark"));
                String type = cursor1.getString(cursor1.getColumnIndex("type"));
                if(conRemark.isEmpty()){
                    conRemark = nickName;
               //     取消电话号码过滤,防止因为昵称带电话所以备注为空时不会录入医生
               //     b = FilterUtil.filterPhoneNumber(nickName);
                }else{
               //     b = FilterUtil.filterPhoneNumber(conRemark);
                }
               // System.out.println(b);
               // if (b) {
                    if (isDebug) {
                        Log.e("contact", "userName=" + userName + "nickName=" + nickName + "alias=" + alias + "conRemark=" + conRemark + "type=" + type);
                    }
                        //将联系人信息写入 csv 文件
                        contactCsvPrinter.printRecord(FilterUtil.filterEmoji(userName), FilterUtil.filterEmoji(nickName), FilterUtil.filterEmoji(alias), FilterUtil.filterEmoji(conRemark), type);

               // }
            }
            contactCsvPrinter.printRecord();
            contactCsvPrinter.flush();

                //上传联系人
                upLoadFiles(baseUrl + "contact/import?uploadTime=" + currentTime, file1, 1);
                //联系人上传后再获取并上传群聊记录
                getChatRoomData(db);


        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");

                }
            });
        } finally {
            if (cursor1 != null) {
                cursor1.close();
            }
        }
    }

    //获取群的信息并上传
    private void getChatRoomData(SQLiteDatabase db) {
        Cursor cursor2 = null;
        try {
    //新建文件保存聊天记录
        file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + et_name.getText().toString().trim() + "ΞchatRoomΞfile" + ".csv");
            // 防止出现乱码 utf-8
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2), "UTF-8"));
            messageCsvPrinter = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("chatroomname", "memberlist", "displayname", "roomowner","selfDisplayName"));

            cursor2 = db.rawQuery(chatroomSql, null);

            while (cursor2.moveToNext()) {
                String chatroomname = cursor2.getString(cursor2.getColumnIndex("chatroomname"));
                String memberlist = cursor2.getString(cursor2.getColumnIndex("memberlist"));
                String displayname = cursor2.getString(cursor2.getColumnIndex("displayname"));
                String roomowner = cursor2.getString(cursor2.getColumnIndex("roomowner"));
                String selfDisplayName = cursor2.getString(cursor2.getColumnIndex("selfDisplayName"));
                messageCsvPrinter.printRecord(chatroomname,memberlist, FilterUtil.filterEmoji(displayname),FilterUtil.filterEmoji(roomowner),selfDisplayName);
                }

            messageCsvPrinter.printRecord();
            messageCsvPrinter.flush();

        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");

                }
            });
        } finally {
            if (cursor2 != null) {
                cursor2.close();
            }

        }
        //上传群聊信息
        upLoadFiles(baseUrl + "chatroom/contact/import?uploadTime=" + currentTime, file2, 2);
        //获取消息记录
        getReMessageData(db);
    }

    /**
     * 获取聊天记录并上传
     *
     * @param db
     */
    public void getReMessageData(SQLiteDatabase db) {
        Cursor cursor3 = null;
        if (isDebug) {
            Log.e("query查询分割时间", DateUtil.timeStamp2Date(longLastUpdateTime + EMPTY));
        }
        try {
            //新建文件保存聊天记录
            file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + et_name.getText().toString().trim() + "ΞmessageΞfile" + ".csv");
            // 防止出现乱码 utf-8
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2), "UTF-8"));
            messageCsvPrinter = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("talker", "content", "createTime", "imgPath", "isSend", "type"));

            //判断是否强制更新所有的记录
            if (isSelectAll) {
                //如果是选择全部,则sql 为0
                if(isAddAll){
                    cursor3 = db.rawQuery(messageSql + 0, null);
                    Log.e("query", "更新状态:更新全部记录" + messageSql + 0);
                }else{
                //不是选择全部,则sql 为用户输入值
                    String searchMessageSql = messageSql + addTimestamp+ "  and createTime < "+endTimestamp;
                cursor3 = db.rawQuery(searchMessageSql, null);
                Log.e("query", "更新状态:更新选择的全部记录" + searchMessageSql);
                }

            } else {
                cursor3 = db.rawQuery(messageSql + longLastUpdateTime, null);
                Log.e("query", "更新状态:增量更新部分记录" + messageSql + longLastUpdateTime);
            }

            while (cursor3.moveToNext()) {
                String content = cursor3.getString(cursor3.getColumnIndex("content"));
                String talker = cursor3.getString(cursor3.getColumnIndex("talker"));
                String createTime = cursor3.getString(cursor3.getColumnIndex("createTime"));
                int isSend = cursor3.getInt(cursor3.getColumnIndex("isSend"));
                int imgPath = cursor3.getInt(cursor3.getColumnIndex("imgPath"));
                int type = cursor3.getInt(cursor3.getColumnIndex("type"));
                if (content != null) {
                    if (isDebug) {
                        Log.e("chatInfo", "talker=" + talker + "createTime=" + DateUtil.timeStamp2Date(createTime.toString()) + "content=" + content + "imgPath=" + imgPath + "isSend=" + isSend + "type=" + type);
                    }
                    //将聊天记录写入 csv 文件
                    String messageType;
                    switch (type) {
                        case 1:
                            messageType = "文字消息";
                            break;
                        case 47:
                            messageType = "表情消息";
                            break;
                        case 43:
                            messageType = "视频消息";
                            break;
                        case 49:
                            messageType = "链接/小程序/聊天记录";
                            break;
                        case 50:
                            messageType = "语音视频通话";
                            break;
                        case 3:
                            messageType = "图片消息";
                            break;
                        case 34:
                            messageType = "语音消息";
                            break;
                        case 48:
                            messageType = "地图消息";
                            break;
                        case 10000:
                            messageType = "撤回提醒";
                            break;
                        default:
                            messageType = "其他消息";
                            break;
                    }
                    messageCsvPrinter.printRecord(talker, FilterUtil.filterEmoji(content), DateUtil.timeStamp2Date(createTime.toString()), imgPath, isSend, messageType);
                }
            }


        } catch (Exception e) {
                Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");

                }
            });
        } finally {
            if (cursor3 != null) {
                cursor3.close();
            }
        }

        getMasSendInfo(db,file2,messageCsvPrinter);
    }

    /**
     *  获取利用群发功能发送的聊天记录,并写入 message.csv 文件
     */
    private void getMasSendInfo(SQLiteDatabase db, File file2, CSVPrinter messageCsvPrinter) {
        Cursor cursor4 = null;
        try {
            cursor4 = db.rawQuery(masSendInfoSql,null);
            while (cursor4.moveToNext()) {
                String content = cursor4.getString(cursor4.getColumnIndex("filename"));
                String tolist = cursor4.getString(cursor4.getColumnIndex("tolist"));
                String createTime = cursor4.getString(cursor4.getColumnIndex("createtime"));
                //如果包含多人,则分割后写入 csv
                if (tolist.contains(";")) {
                    String[] nameList = tolist.split(";");
                    for (String name : nameList) {
                        //将聊天记录写入 csv 文件
                        messageCsvPrinter.printRecord(name, FilterUtil.filterEmoji(content), DateUtil.timeStamp2Date(createTime.toString()), 0, 1, "文字消息");
                    }
                    //弱不包含分号,则只有一个人,直接写入 csv
                }else{
                    //将聊天记录写入 csv 文件
                    messageCsvPrinter.printRecord(tolist, FilterUtil.filterEmoji(content), DateUtil.timeStamp2Date(createTime.toString()), 0, 1, "文字消息");
                }
            }
            messageCsvPrinter.printRecord();
            messageCsvPrinter.flush();

        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");

                }
            });
        } finally {
            if (cursor4 != null) {
                cursor4.close();
            }
            if (db != null) {
                db.close();
            }
        }
        //上传聊天记录
        upLoadFiles(baseUrl + "message/import?uploadTime=" + currentTime, file2,3);
    }


    /**
     * @param url
     * @throws Exception isSave 用来表示只有消息表上传成功时,才保存上传时间到 sp
     * type  1 = 联系人    2 = 群聊  3 =  消息记录
     */
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    private void upLoadFiles(String url, File file, final int type) {
        if (isDebug) {
            Log.e("query网址", url + file.getName());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRemindText != null) {
                    switch (type) {
                        case 1:
                            mRemindText.setText("正在向工作台上传联系人,请稍候");
                            break;
                        case 2:
                            mRemindText.setText("正在向工作台上传群聊,请稍候");
                            break;
                        case 3:
                            mRemindText.setText("正在向工作台上传聊天记录,请稍候");
                            break;
                            default:
                            mRemindText.setText("正在向工作台上传文件,请稍候");
                                break;
                    }

                }
            }
        });
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.MINUTES)
                .build();
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (file.exists()) {
            String TYPE = "application/octet-stream";
            RequestBody fileBody = RequestBody.create(MediaType.parse(TYPE), file);
            RequestBody requestBody = builder
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                private String description;

                @Override
                public void onFailure(Call call, final IOException e) {
                    Log.e("query上传文件失败的返回错误", e.toString());
                    //上传失败

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (type) {
                                case 1:
                                    getUploadTimeError("联系人上传失败请联系开发人员");
                                    break;
                                case 2:
                                    getUploadTimeError("群聊记录上传失败请联系开发人员");
                                    break;
                                case 3:
                                    mRemindText.setText("聊天记录上传失败请联系开发人员");
                                    break;
                                default:
                                    getUploadTimeError("上传失败请联系开发人员");
                                    break;
                            }
                        }
                    });


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //失败原因显示
                            des_text.setText(e.toString());

                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String string = response.body().string();
                    //Log.e("query上传文件的返回值", string);
                    try {
                        JSONObject objects = new JSONObject(string);
                        updateCode = objects.get("code");
                        Object description = objects.get("description");
                        this.description = description.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (isDebug) {
                        switch (type) {
                            case 1:
                                Log.e("query上传联系人文件的返回值", string);
                                break;
                            case 2:
                                Log.e("query群聊记录上传文件的返回值", string);
                                break;
                            case 3:
                                Log.e("query上传聊天文件的返回值", string);
                                break;
                            default:
                                Log.e("query上传文件的返回值", string);
                                break;
                        }


                    }
                    if (SUCCESS_CODE.equals(updateCode.toString())) {
                        //上传消息记录成功,重新赋值时间并保存sp时间
                        if (type==3) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (loadingDialog == null) {
                                        loadingDialog = new CustomDialog(mActivity, R.style.customDialog, R.layout.layout_loading_dialog);
                                    }
                                    loadingDialog.setCancelable(true);
                                    loadingView.setVisibility(View.INVISIBLE);
                                    iv_fail.setVisibility(View.INVISIBLE);
                                    iv_success.setVisibility(View.VISIBLE);
                                    mRemindText.setText("聊天记录上传成功");

                                    //重新赋值本次上传时间,并保存在 sp 中
                                        longLastUpdateTime = Long.valueOf(mTimeStamp);
                                        tv_updateTime.setText(currentTime);
                                        if (isDebug) {
                                            Log.e("query聊天记录上传成功后更新的时间", DateUtil.timeStamp2Date(longLastUpdateTime + EMPTY));
                                        }
                                        if (preferences == null) {
                                            preferences = getSharedPreferences(USERINFO, Context.MODE_PRIVATE);
                                        }
                                        SharedPreferences.Editor edit = preferences.edit();
                                        edit.putString(LAST_UPDATE_TIME, currentTime);
                                        edit.commit();
                                    }


                            });
                        } else if(type==1){
                            if (isDebug) {
                                Log.e("query联系人上传成功", EMPTY);
                            }
                        }else {
                            if (isDebug) {
                                Log.e("query群聊上传成功", EMPTY);
                            }
                        }
                    } else {
                        switch (type) {
                            case 1:
                                getUploadTimeError("联系人上传失败" + description);
                                break;
                            case 2:
                                getUploadTimeError("群聊记录上传失败" + description);
                                break;
                            case 3:
                                getUploadTimeError("聊天记录上传失败," + description);
                                break;
                            default:
                                getUploadTimeError("文件上传失败请联系开发人员");
                                break;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //失败原因显示
                                des_text.setText(description);
                                Toast.makeText(getApplicationContext(), description, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                }
            });
        }
    }



    /**
     * 请求失败的弹窗处理
     *
     * @param s
     */
    private void getUploadTimeError(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (loadingDialog == null) {
                    loadingDialog = new CustomDialog(mActivity, R.style.customDialog, R.layout.layout_loading_dialog);
                }
                loadingDialog.setCancelable(true);
                mRemindText.setText(s);
                loadingView.setVisibility(View.INVISIBLE);
                iv_success.setVisibility(View.INVISIBLE);
                iv_fail.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * 判断是否安装了微信
     *
     * @return
     */
    public boolean isWeixinAvilible() {
        final PackageManager packageManager = mActivity.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(WXPACKAGENAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取聊天记录并保存在本地,权限问题暂时废用
     *
     * @param db
     */
    public void exportMessageToSD(SQLiteDatabase db) {
        Cursor c2 = null;
        String isSend ;
        try {
            //新建文件保存聊天记录
            file2 = new File(Environment.getExternalStorageDirectory().getPath() + "/" + "聊天记录ΞmessageΞfile" + ".csv");
            // 防止出现乱码 utf-8
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2), "UTF-8"));
            messageCsvPrinter = new CSVPrinter(writer2, CSVFormat.DEFAULT.withHeader("微信号", "消息状态", "消息类型","聊天消息", "聊天时间"));
            c2 = db.rawQuery(messageSql + "0", null);
            Log.e("query", "更新全部记录" + messageSql + "0");
            while (c2.moveToNext()) {
                String content = c2.getString(c2.getColumnIndex("content"));
                String talker = c2.getString(c2.getColumnIndex("talker"));
                String createTime = c2.getString(c2.getColumnIndex("createTime"));
                int SendType = c2.getInt(c2.getColumnIndex("isSend"));
                if(SendType==1){
                    isSend = "发送";
                }else{
                    isSend = "接受";
                }
                int type = c2.getInt(c2.getColumnIndex("type"));
                if (content != null) {
                    if (isDebug) {
                        Log.e("chatInfo", "talker=" + talker + "createTime=" + DateUtil.timeStamp2Date(createTime.toString()) + "content=" + content + "imgPath="+ "isSend=" + isSend + "type=" + type);
                    }
                    //将聊天记录写入 csv 文件
                    String messageType;
                    switch (type) {
                        case 1:
                            messageType = "文字消息";
                            break;
                        case 47:
                            messageType = "表情消息";
                            break;
                        case 43:
                            messageType = "视频消息";
                            break;
                        case 49:
                            messageType = "链接/小程序/聊天记录";
                            break;
                        case 50:
                            messageType = "语音视频通话";
                            break;
                        case 3:
                            messageType = "图片消息";
                            break;
                        case 34:
                            messageType = "语音消息";
                            break;
                        case 48:
                            messageType = "地图消息";
                            break;
                        case 10000:
                            messageType = "撤回提醒";
                            break;
                        default:
                            messageType = "其他消息";
                            break;
                    }
                    messageCsvPrinter.printRecord(talker,isSend,messageType,FilterUtil.filterEmoji(content), DateUtil.timeStamp2Date(createTime.toString()) );
                }
            }
            messageCsvPrinter.printRecord();
            messageCsvPrinter.flush();
            //提示导出成功
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (loadingDialog == null) {
                        loadingDialog = new CustomDialog(mActivity, R.style.customDialog, R.layout.layout_loading_dialog);
                    }
                    loadingDialog.setCancelable(true);
                    loadingView.setVisibility(View.INVISIBLE);
                    iv_fail.setVisibility(View.INVISIBLE);
                    iv_success.setVisibility(View.VISIBLE);
                    mRemindText.setText("导出成功!");

                }
            });

        } catch (Exception e) {
            Log.e("openWxDb", "读取数据库信息失败" + e.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getUploadTimeError("读取数据库信息失败");

                }
            });
        } finally {
            if (c2 != null) {
                c2.close();
            }
            if (db != null) {
                db.close();
            }
        }
    }

    /*
     * 安装更新
     */
    private void startUpdate3() {
        UpdateConfiguration configuration = new UpdateConfiguration()
                //输出错误日志
                .setEnableLog(true)
                //设置自定义的下载
                //.setHttpManager()
                //下载完成自动跳动安装页面
                .setJumpInstallPage(true)
                //设置对话框背景图片 (图片规范参照demo中的示例图)
                //.setDialogImage(R.drawable.ic_dialog)
                //设置按钮的颜色
                .setDialogButtonColor(Color.parseColor("#2DA1F8"))
                //设置按钮的文字颜色
                .setDialogButtonTextColor(Color.WHITE)
                //支持断点下载
                .setBreakpointDownload(true)
                //设置是否显示通知栏进度
                .setShowNotification(true)
                //设置强制更新
                .setForcedUpgrade(true)
                //设置对话框按钮的点击监听
                .setButtonClickListener(mActivity)
                //设置下载过程的监听
                .setOnDownloadListener(this);
        manager = DownloadManager.getInstance(this);
        manager.setApkName("wechatHelper.apk")
                .setApkUrl("https://github.com/KeZengOo/wechatHelper/raw/master/app/%E8%81%8A%E5%A4%A9%E4%B8%8A%E4%BC%A0%E5%B7%A5%E5%85%B7.apk")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
                .setDownloadPath(Environment.getExternalStorageDirectory() + "/AppUpdate")
                .setApkVersionCode(4)
                .setApkVersionName("1.2.0")
                .setAuthorities(getPackageName())
                .setApkDescription("这都是啥")
                .download();
    }

    @Override
    public void onButtonClick(int id) {

    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {

    }

    @Override
    public void done(File apk) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void error(Exception e) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }

    }
}

