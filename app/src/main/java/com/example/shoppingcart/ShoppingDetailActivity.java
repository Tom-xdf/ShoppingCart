package com.example.shoppingcart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.shoppingcart.database.ShoppingDBHelper;
import com.example.shoppingcart.enity.GoodsInfo;
import com.example.shoppingcart.util.ToastUtil;

public class ShoppingDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_title;
    private TextView tv_count;
    private TextView tv_goods_price;
    private TextView tv_goods_desc;
    private ImageView iv_goods_pic;
    private ShoppingDBHelper mDBHelper;
    private int mGoodsId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_detail);

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText("商品详情");

        tv_count = findViewById(R.id.tv_count);
        tv_count.setText(String.valueOf(MyApplication.getInstance().goodsCount));

//        调用数据库
        mDBHelper = ShoppingDBHelper.getInstance(this);

        tv_goods_price = findViewById(R.id.tv_goods_price);
        tv_goods_desc = findViewById(R.id.tv_goods_desc);
        iv_goods_pic = findViewById(R.id.iv_goods_pic);

//        给按钮设置点击事件监听
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_cart).setOnClickListener(this);
        findViewById(R.id.btn_add_cart).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDetail();
    }

    private void showDetail() {
//        获取上一个页面传输过来的商品编号
        mGoodsId = getIntent().getIntExtra("goods_id", 0);
        if (mGoodsId > 0) {
//            根据商品编号查询商品数据库中的商品记录
            GoodsInfo info = mDBHelper.queryGoodsInfoById(mGoodsId);
            tv_title.setText(info.name);
            tv_goods_desc.setText(info.description);
            tv_goods_price.setText(String.valueOf((int) info.price));
            iv_goods_pic.setImageURI(Uri.parse(info.picPath));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_back:
//                点击返回直接结束
                finish();
                break;
            case R.id.iv_cart:
//                跳转到购物车
                Intent intent = new Intent(this, ShoppingCartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.btn_add_cart:
//                跳转到购物车按钮
//                获取到商品的ID
                addToCart(mGoodsId);

                break;
        }
    }

    private void addToCart(int godsId) {
//            购物车数量+1
        int count = ++MyApplication.getInstance().goodsCount;
        tv_count.setText(String.valueOf(count));
        mDBHelper.insertCartInfo(godsId);
        ToastUtil.show(this,"成功添加至购物车！");
    }
}