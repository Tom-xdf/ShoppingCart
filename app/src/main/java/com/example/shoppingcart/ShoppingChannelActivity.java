package com.example.shoppingcart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingcart.database.ShoppingDBHelper;
import com.example.shoppingcart.enity.GoodsInfo;
import com.example.shoppingcart.util.ToastUtil;

import java.util.List;

public class ShoppingChannelActivity extends AppCompatActivity implements View.OnClickListener {

    //    声明一个商品数据库的帮助器对象
    private ShoppingDBHelper mDBHelper;
    private TextView tv_count;
    private GridLayout gl_channel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_channel);


        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("手机商城");


//      购物车商品数量
        tv_count = findViewById(R.id.tv_count);
        gl_channel = findViewById(R.id.gl_channel);

//        购物车图标和返回图标设置点击事件
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_cart).setOnClickListener(this);

        mDBHelper = ShoppingDBHelper.getInstance(this);
//        打开读连接
        mDBHelper.openReadLink();
//        打开写连接
        mDBHelper.openWriteLink();

//        从数据库查询出商品信息，并展示
        showGoods();
    }

    //  查询购物车总数并展示
    @Override
    protected void onResume() {
        super.onResume();
        //  从数据库查询购物车总数并展示
        showCartInfoToal();
    }

    //    查询购物车商品总数，并展示
    private void showCartInfoToal() {
//        手动生成查询购物车的方法
        int count = mDBHelper.countCartInfo();
        MyApplication.getInstance().goodsCount =count;
        tv_count.setText(String.valueOf(count));
    }

    private void showGoods() {
//        获取屏幕宽度：商品条目是一个线性布局，设置布局的宽度为屏幕的一半
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (screenWidth / 2,
                        LinearLayout.
                                LayoutParams.
                                WRAP_CONTENT);
//        查询商品数据库中所有的商品记录
        List<GoodsInfo> list = mDBHelper.queryAllGoodsInfo();

//        移除下面的子视图
        gl_channel.removeAllViews();

        for (GoodsInfo info : list) {
//        获取布局文件item_goods.
//            导入布局文件
            View view = LayoutInflater.from(this).inflate(R.layout.item_goods, null);
//            导入图片
            ImageView iv_thumb = view.findViewById(R.id.iv_thumb);
//            导入手机名称
            TextView tv_name = view.findViewById(R.id.tv_name);
//            导入手机价格
            TextView tv_price = view.findViewById(R.id.tv_price);

//            导入加入加入购物车按钮
            Button btn_add = view.findViewById(R.id.btn_add);
//          添加到购物车点击使事件
            btn_add.setOnClickListener(v -> {
//                这里手动添加一个addTCart方法
                addToCart(info.id, info.name);
            });

//            给控件设置值，添加到表格布局里面
//            把上方导入的图片名字等赋值（预先写好的在GoodsInfo里面）
            iv_thumb.setImageURI(Uri.parse(info.picPath));
            tv_name.setText(info.name);
            tv_price.setText(String.valueOf((int) info.price));
//            tv_price.setText(Float.floatToIntBits(info.price));

            //          点击商品图片，跳转到商品详情页面
            iv_thumb.setOnClickListener(v -> {
                Intent intent = new Intent(ShoppingChannelActivity.this, ShoppingDetailActivity.class);
                intent.putExtra("goods_id", info.id);
                startActivity(intent);
            });

//          把商品视图添加到网格布局
            gl_channel.addView(view, params);
        }
    }

    private void addToCart(int goodsId, String goodsName) {
//        购物车数量+1
        int count = ++MyApplication.getInstance().goodsCount;
//        购物车商品随着加入购物车的商品增加
        tv_count.setText(String.valueOf(count));
//        手动添加insertCartInfo的方法，把加入到购物车商品，添加到数据库
        mDBHelper.insertCartInfo(goodsId);
        ToastUtil.show(this, "已添加一部" + goodsName + "到购物车");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        主界面退出，那么则关闭连接
        mDBHelper.closeLink();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
//                点击了返回按钮，关闭当前页面
                finish();
                break;
            case R.id.iv_cart:
//                点击了购物车图标
//                从商城页面跳转到购物车页面
                Intent intent = new Intent(this, ShoppingCartActivity.class);
//                设置启动模式，避免多次返回同一个页面
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }
}