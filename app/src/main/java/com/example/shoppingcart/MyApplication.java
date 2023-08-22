package com.example.shoppingcart;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shoppingcart.database.ShoppingDBHelper;
import com.example.shoppingcart.enity.GoodsInfo;
import com.example.shoppingcart.util.FileUtil;
import com.example.shoppingcart.util.ShareUtil;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class MyApplication extends Application {


    //    单例模式
    private static MyApplication mApp;
    //    声明一个公共的信息映射对象，可当作全局变量使用
    public HashMap<String, String> infoMap = new HashMap<>();

    //    声明一个书籍数据库对象
//    private BookDatabase bookDatabase;

//    购物车中商品总数量
    public int goodsCount;

    public static MyApplication getInstance() {
        return mApp;
    }

    //    在app启动时调用
    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        Log.d("ning", "MyApplication onCreate");
//
////        构建书籍数据库的实例
//        bookDatabase = Room.databaseBuilder(this, BookDatabase.class, "book")
////                允许迁移数据库，（发生数据库变更时，Room默认删除原数据库再创建新的数据库。
////                如此一来原来的记录会丢失，故而要改为迁移方式以便保存原有记录）
//                .addMigrations()
//                //允许在主线程里面操作数据库（Room默认不能在主线程中操作数据库）
//                .allowMainThreadQueries()
//                .build();
//
////        初始化商品信息
        initGoodsInfo();
    }

    private void initGoodsInfo() {
//        获取共享参数保存的是否首次打开参数
//        读取信息如果打开什么都没有，默认第一次打开
        boolean isFirst = ShareUtil.getInstance(this).readBoolean("first", true);
//        获取当前App的私有下载路径
        String directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                .toString() +
                File.separatorChar;

        if (isFirst) {
//            模拟网络图片下载
            List<GoodsInfo> list = GoodsInfo.getDefaultList();
            for (GoodsInfo info : list) {
//                得到drawable的id
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), info.pic);
//                文件完整路径
                String path = directory + info.id + ".jpg";

//                得到id之后往存储卡保存商品图片
                FileUtil.saveImage(path, bitmap);

//              回收位图对象
                bitmap.recycle();
                info.picPath = path;
                //            打开数据库，把商品信息插入到表中
//            获取数据库
                ShoppingDBHelper dbHelper = ShoppingDBHelper.getInstance(this);
//            写入
                dbHelper.openWriteLink();
//           往数据库插入数据
                dbHelper.insertGoodsInfos(list);
//          关闭连接
                dbHelper.closeLink();
//            把是否首次打开写入共享参数
                ShareUtil.getInstance(this).writeBoolean("first", false);
            }

        }
    }

    //    App终止的时候调用
    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("ning", "onTerminate");
    }

    //    在配置改变时调用，例如从横屏变为竖屏
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("ning", "onConfigurationChanged");
    }

//    获取书籍数据库的实例
//    public BookDatabase getBookDB(){
//        return bookDatabase;
//    }
}
