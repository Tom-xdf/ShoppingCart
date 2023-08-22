package com.example.shoppingcart.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.shoppingcart.enity.CartInfo;
import com.example.shoppingcart.enity.GoodsInfo;

import java.util.ArrayList;
import java.util.List;

public class ShoppingDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "shopping.db";//数据库名字
    //    商品信息表
    private static final String TABLE_GOODS_INFO = "goods_info";//数据库表名

    //    购物车信息表
    private static final String TABLE_CART_INFO = "cart_info";

    private static final int DB_VERSION = 1;//版本名

    private static ShoppingDBHelper mHelper = null;
    private SQLiteDatabase mRDB = null;//SQLiteDatabase,读的实例
    private SQLiteDatabase mWDB = null;//SQLiteDAtabase,写的实例

    private ShoppingDBHelper(Context context) {
        super(context,
                DB_NAME,
                null,
                DB_VERSION);
    }

    // 利用单例模式获取数据库帮助器的唯一实例
    public static ShoppingDBHelper getInstance(Context context) {
        if (mHelper == null) {
            mHelper = new ShoppingDBHelper(context);
        }
        return mHelper;
    }

    // 打开数据库的读连接
    public SQLiteDatabase openReadLink() {
        if (mRDB == null || !mRDB.isOpen()) {
            mRDB = mHelper.getReadableDatabase();
        }
        return mRDB;
    }

    // 打开数据库的写连接
    public SQLiteDatabase openWriteLink() {
        if (mWDB == null || !mWDB.isOpen()) {
            mWDB = mHelper.getWritableDatabase();
        }
        return mWDB;
    }

    // 关闭数据库连接
    public void closeLink() {
        if (mRDB != null && mRDB.isOpen()) {
            mRDB.close();
            mRDB = null;
        }

        if (mWDB != null && mWDB.isOpen()) {
            mWDB.close();
            mWDB = null;
        }
    }

    // 创建数据库，执行建表语句
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建商品信息表
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_GOODS_INFO +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " name VARCHAR NOT NULL," +
                " description VARCHAR NOT NULL," +
                " price FLOAT NOT NULL," +
                " pic_path VARCHAR NOT NULL);";
        db.execSQL(sql);

        // 创建购物车信息表
        sql = "CREATE TABLE IF NOT EXISTS " + TABLE_CART_INFO +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " goods_id INTEGER NOT NULL," +
                " count INTEGER NOT NULL);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 添加多条商品信息
    public void insertGoodsInfos(List<GoodsInfo> list) {
        // 插入多条记录，要么全部成功，要么全部失败
        try {
            mWDB.beginTransaction();
            for (GoodsInfo info : list) {
                ContentValues values = new ContentValues();
                values.put("name", info.name);
                values.put("description", info.description);
                values.put("price", info.price);
                values.put("pic_path", info.picPath);
                mWDB.insert(TABLE_GOODS_INFO, null, values);
            }
            mWDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mWDB.endTransaction();
        }
    }

    //    查询所有的商品信息
    public List<GoodsInfo> queryAllGoodsInfo() {
        String sql = "select * from " + TABLE_GOODS_INFO;
        List<GoodsInfo> list = new ArrayList<>();
        Cursor cursor = mRDB.rawQuery(sql, null);
        while (cursor.moveToNext()) {
//            建立游标
            GoodsInfo info = new GoodsInfo();
            info.id = cursor.getInt(0);
            info.name = cursor.getString(1);
            info.description = cursor.getString(2);
            info.price = cursor.getFloat(3);
            info.picPath = cursor.getString(4);
            list.add(info);
        }
        return list;
    }

    //      添加商品到购物车
    public void insertCartInfo(int goodsId) {
//        如果购物车不存在该商品，添加一条信息
        CartInfo cartInfo = queryCartInfoByGoodsId(goodsId);
        ContentValues values = new ContentValues();
        values.put("goods_id", goodsId);
        if (cartInfo == null) {
//            购物车为空，不存在商品，那么添加进去，商品从1开始
            values.put("count", 1);
            mWDB.insert(TABLE_CART_INFO, null, values);
        } else {
//            如果购物车中已经存在商品，那么更新商品数量
            values.put("_id", cartInfo.id);
            values.put("count", ++cartInfo.count);
            mWDB.update(TABLE_CART_INFO,
                    values,
                    "_id=?",
                    new String[]{String.valueOf(cartInfo.id)});
        }
    }

    //        如果购物车不存在该商品，添加一条信息
    private CartInfo queryCartInfoByGoodsId(int goodsId) {
//        查询TABLE_CART_INFO
        Cursor cursor = mRDB.query(TABLE_CART_INFO,
                null,
                "goods_id=?",
                new String[]{String.valueOf(goodsId)},
                null,
                null,
                null);
        CartInfo info = null;
//        move.ToNext()遍历游标，查询表中的指定字段
        if (cursor.moveToNext()) {
            info = new CartInfo();
            info.id = cursor.getInt(0);
//            商品id
            info.goodsId = cursor.getInt(1);
//            商品数量
            info.count = cursor.getInt(2);
        }
        return info;
    }

    //    统计购物车商品的总数量
    public int countCartInfo() {
        int count = 0;
        String sql = "select sum(count) from " + TABLE_CART_INFO;
        Cursor cursor = mRDB.rawQuery(sql, null);
        return count;
    }

    public List<CartInfo> queryAllCartInfo() {
        List<CartInfo> list = new ArrayList<>();
//        查询数据库的商品记录
        Cursor cursor = mRDB.query(TABLE_CART_INFO,
                null,
                null,
                null,
                null,
                null,
                null);
//        查询数据库所有记录
        while (cursor.moveToNext()) {
            CartInfo info = new CartInfo();
            info.id = cursor.getInt(0);
            info.goodsId = cursor.getInt(1);
            info.count = cursor.getInt(2);
            list.add(info);
        }
        return list;
    }

    //    根据商品id来查询商品信息
    public GoodsInfo queryGoodsInfoById(int goodsId) {
        GoodsInfo info = null;
        Cursor cursor = mRDB.query(TABLE_GOODS_INFO,
                null,
                "_id=?",
                new String[]{String.valueOf(goodsId)},
                null,
                null,
                null);
//        根据id来查找商品信息
        if (cursor.moveToNext()) {
            info = new GoodsInfo();
            info.id = cursor.getInt(0);
            info.name = cursor.getString(1);
            info.description = cursor.getString(2);
            info.price = cursor.getFloat(3);
            info.picPath = cursor.getString(4);
//            查找出商品信息

        }
        return info;
    }

//    根据商品Id来删除购物信息
    public void deleteCartInfoByGoodsId(int goodsId) {
        mWDB.delete(TABLE_CART_INFO,"goods_id=?",new String[]{String.valueOf(goodsId)});
    }

//    删除所有的商品信息
    public void deleteAllCartInfo(){
        mWDB.delete(TABLE_CART_INFO,"1=1",null);
    }
}
