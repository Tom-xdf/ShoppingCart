package com.example.shoppingcart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingcart.database.ShoppingDBHelper;
import com.example.shoppingcart.enity.CartInfo;
import com.example.shoppingcart.enity.GoodsInfo;
import com.example.shoppingcart.util.ToastUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCartActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_count;
    private LinearLayout ll_cart;
    private ShoppingDBHelper mDBHelper;

    //    声明一个购物车中的商品信息列表
    private List<CartInfo> mCartList;
    //    声明一个根据商品编号查找商品信息的映射,把商品信息缓存起来,这样不用每一次都去查询数据库
    private Map<Integer, GoodsInfo> mGoodsMap = new HashMap<>();
    private TextView tv_total_price;
    private LinearLayout ll_empty;
    private LinearLayout ll_content;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText("购物车");

        ll_cart = findViewById(R.id.ll_cart);
        tv_total_price = findViewById(R.id.tv_total_price);


//        购物车数量
        tv_count = findViewById(R.id.tv_count);
//        显示全局的商品数量，
        tv_count.setText(String.valueOf(MyApplication.getInstance().goodsCount));

//        获取数据库信息,此处不需要打开数据库的读写功能，主界面一打开，主界面还在，数据库便在
        mDBHelper = ShoppingDBHelper.getInstance(this);

//        给返回按钮设置监听
        findViewById(R.id.iv_back).setOnClickListener(this);//返回按钮
        findViewById(R.id.btn_settle).setOnClickListener(this);//结算按钮
        findViewById(R.id.btn_clear).setOnClickListener(this);//清空按钮
        findViewById(R.id.btn_shopping_channel).setOnClickListener(this);//跳转到购物商城

//        设置清空购物车之后显示的文本
        ll_empty = findViewById(R.id.ll_empty);
//        设置表头
        ll_content = findViewById(R.id.ll_content);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        手动添加展示购物车里面的信息的方法
        showCart();
    }

    //    展示购物车信息的列表以及价格信息
    private void showCart() {
//   移除下面的子视图
        ll_cart.removeAllViews();
//        查询购物车数据库里面的所有商品记录
//        手动创建查询购物商品记录的方法
        mCartList = mDBHelper.queryAllCartInfo();
        if (mCartList.size() == 0) {
            return;
        }

        for (CartInfo info : mCartList) {
//            根据商品编号查询商品数据库中的商品记录
//            生成一个查询商品记录的方法
            GoodsInfo goods = mDBHelper.queryGoodsInfoById(info.goodsId);
//            拿到价格等商品信息,把商品信息缓存,这样不用每次获取
            mGoodsMap.put(info.goodsId, goods);


//        获取布局文件item_cart.xml的跟视图
            View view = LayoutInflater.from(this).inflate(R.layout.item_cart, null);
            ImageView iv_thumb = view.findViewById(R.id.iv_thumb);
            TextView tv_name = view.findViewById(R.id.tv_name);
            TextView tv_desc = view.findViewById(R.id.tv_desc);
            TextView tv_count = view.findViewById(R.id.tv_count);
            TextView tv_price = view.findViewById(R.id.tv_price);
            TextView tv_sum = view.findViewById(R.id.tv_sum);

//            给商品赋值
            iv_thumb.setImageURI(Uri.parse(goods.picPath));
            tv_name.setText(goods.name);
            tv_desc.setText(goods.description);
            tv_count.setText(String.valueOf(info.count));
            tv_price.setText(String.valueOf((int) goods.price));
//            设置商品总价
            tv_sum.setText(String.valueOf((int) (info.count * goods.price)));


//          给商品添加长按事件，长按商品行，就删除该商品
            view.setOnLongClickListener(v -> {
//                长按返回true或者false，消耗这个点击事件
                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingCartActivity.this);
                builder.setMessage("是否从购物车中删除" + goods.name + "?");
//                是
                builder.setPositiveButton("是", (dialog, which) -> {
//                    移除当前视图
                    ll_cart.removeView(v);
//                    删除该商品
                    deleteGoods(info);
                });
//                否
                builder.setNegativeButton("否", null);
                builder.create().show();
                return true;
            });

//            给商品行添加点击事件。点击商品行跳转到商品详情页
            view.setOnClickListener(v -> {
                Intent intent = new Intent(ShoppingCartActivity.this, ShoppingDetailActivity.class);
                intent.putExtra("goods_id", goods.id);
                startActivity(intent);
            });


//            往购物车列表添加该商品行
            ll_cart.addView(view);
        }
//        重新计算购物车中的商品总金额
        refreshTotalPrices();

    }

    private void deleteGoods(CartInfo info) {
//        点击了长按删除是，进入到此方法
        MyApplication.getInstance().goodsCount -= info.count;
//        从数据库中删除该商品
//        生成一个在数据库删除商品的方法
//        这里是删除数据库的商品信息
        mDBHelper.deleteCartInfoByGoodsId(info.goodsId);
//        从购物车的列表中删除商品
        CartInfo removed = null;
        for (CartInfo cartInfo : mCartList) {
            if (cartInfo.goodsId == info.goodsId) {
//                把商品信息放在removed里面
                removed = cartInfo;
                break;
            }
        }
//        循环完毕，删除缓存列表的商品信息
        mCartList.remove(removed);

//      显示最新的商品信息
        showCount();
        ToastUtil.show(this, "已从购物车删除" + mGoodsMap.get(info.goodsId).name);
        mGoodsMap.remove(info.goodsId);
//        刷新购物车所有商品的总金额
        refreshTotalPrices();
    }

    //    显示购物车中的商品的数量
    private void showCount() {
        tv_count.setText(String.valueOf(MyApplication.getInstance().goodsCount));
//        购物车没有商品，那么则显示“空空如也 ”
        if (MyApplication.getInstance().goodsCount == 0) {
//            设置tools文本为可见
            ll_empty.setVisibility(View.VISIBLE);
//            隐藏表头
            ll_content.setVisibility(View.GONE);
//            移除ll_Cart的子视图
            ll_cart.removeAllViews();
        } else {
//            如果没有完全清空，则显示
            ll_content.setVisibility(View.VISIBLE);
//          tools:文本设置为不可见
            ll_empty.setVisibility(View.GONE);
        }
    }

    //    重新计算购物车的商品总金额
    private void refreshTotalPrices() {
        int totalPrices = 0;
        for (CartInfo info : mCartList) {
            GoodsInfo goods = mGoodsMap.get(info.goodsId);
            totalPrices += goods.price * info.count;
        }
        tv_total_price.setText(String.valueOf(totalPrices));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
//                点击了返回按钮
//                关闭当前页面
                finish();
                break;
            case R.id.btn_shopping_channel:
//                从购物车跳转到商城主页面
                Intent intent = new Intent(this, ShoppingChannelActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.btn_clear:
//                点击了清空按钮：清空购物车数据库
                mDBHelper.deleteAllCartInfo();
                MyApplication.getInstance().goodsCount = 0;
//                显示最新的商品数量
                showCart();
                showCount();
                ToastUtil.show(this, "购物车已清空");
                break;
            case R.id.btn_settle:
//                点击了结算按钮
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("结算商品");
                builder.setMessage("抱歉，支付功能尚未开通，请下次再来");
                builder.setPositiveButton("我知道了", null);
//                显示弹出的对话框
                builder.create().show();
                break;

        }
    }
}