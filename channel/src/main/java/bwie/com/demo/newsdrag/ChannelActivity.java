package bwie.com.demo.newsdrag;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bwie.com.demo.IApplication;
import bwie.com.demo.R;
import bwie.com.demo.newsdrag.adapter.DragAdapter;
import bwie.com.demo.newsdrag.adapter.OtherAdapter;
import bwie.com.demo.newsdrag.bean.Beans;
import bwie.com.demo.newsdrag.bean.ChannelItem;
import bwie.com.demo.newsdrag.bean.ChannelManage;
import bwie.com.demo.newsdrag.view.DragGrid;
import bwie.com.demo.newsdrag.view.OtherGridView;

/**
 * 频道管理
 * @Author RA
 * @Blog http://blog.csdn.net/vipzjyno1
 */
public class ChannelActivity extends Activity implements OnItemClickListener, AdapterView.OnItemLongClickListener {
    /** 用户栏目的GRIDVIEW */
    private DragGrid userGridView;
    /** 其它栏目的GRIDVIEW */
    private OtherGridView otherGridView;
    /** 用户栏目对应的适配器，可以拖动 */
    DragAdapter userAdapter;
    /** 其它栏目对应的适配器 */
    OtherAdapter otherAdapter;
    /** 其它栏目列表 */
    ArrayList<ChannelItem> otherChannelList = new ArrayList<ChannelItem>();
    /** 用户栏目列表 */
    ArrayList<ChannelItem> userChannelList = new ArrayList<ChannelItem>();
    /** 是否在移动，由于这边是动画结束后才进行的数据更替，设置这个限制为了避免操作太频繁造成的数据错乱。 */
    boolean isMove = false;

    private AlertDialog.Builder builder;
    private AlertDialog.Builder b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe_activity);
        initView();

        setDialog();

        initData();
    }

    private void get() {
        RequestParams params = new RequestParams
                ("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");
       /* params.addQueryStringParameter("username","abc");
        params.addQueryStringParameter("password","123");*/
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                String substring = result.substring(0, result.length() - 1);
                Gson gson=new Gson();
                Beans beans = gson.fromJson(substring, Beans.class);
                List<Beans.AppBean> app = beans.getApp();
                String url = app.get(1).getUrl();
                download(url);
            }
            //请求异常后的回调方法
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            //主动调用取消请求的回调方法
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
        });
    }

    private void download(String url) {
        RequestParams params = new RequestParams(url);

        params.setSaveFilePath(Environment.getExternalStorageDirectory()+"/App/");

        params.setAutoRename(true);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(result), "application/vnd.android.package-archive");
                ChannelActivity.this.startActivity(intent);
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }
            @Override
            public void onCancelled(CancelledException cex) {
            }
            @Override
            public void onFinished() {
            }
            //网络请求之前回调
            @Override
            public void onWaiting() {
            }
            //网络请求开始的时候回调
            @Override
            public void onStarted() {
            }
            //下载的时候不断回调的方法
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //当前进度和文件总大小
                Log.d("download","current："+ current +"，total："+total);
            }
        });
    }

    public void setDialog(){
        b = new AlertDialog.Builder(this);
        b.setTitle("版本更新");
        b.setMessage("现在检测到新版本，是否更新?");
        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                get();
            }
        });
        final String[] itmes = {"wifi","手机流量"};
        builder = new AlertDialog.Builder(this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("网络选择");
        builder.setSingleChoiceItems(itmes, -1, new AlertDialog.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    b.show();
                    dialog.cancel();
                }else{
                    startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });
    }

    /** 初始化数据*/
    private void initData() {
        userChannelList = ((ArrayList<ChannelItem>) ChannelManage.getManage(IApplication.getApp().getSQLHelper()).getUserChannel());
        otherChannelList = ((ArrayList<ChannelItem>)ChannelManage.getManage(IApplication.getApp().getSQLHelper()).getOtherChannel());
        userAdapter = new DragAdapter(this, userChannelList);
        userGridView.setAdapter(userAdapter);
        otherAdapter = new OtherAdapter(this, otherChannelList);
        otherGridView.setAdapter(this.otherAdapter);
        //设置GRIDVIEW的ITEM的点击监听
        otherGridView.setOnItemClickListener(this);
        userGridView.setOnItemClickListener(this);
        userGridView.setOnItemLongClickListener(this);
        otherGridView.setOnItemLongClickListener(this);

    }

    /** 初始化布局*/
    private void initView() {
        userGridView = (DragGrid) findViewById(R.id.userGridView);
        otherGridView = (OtherGridView) findViewById(R.id.otherGridView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** GRIDVIEW对应的ITEM点击监听接口  */
    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position,long id) {
        //如果点击的时候，之前动画还没结束，那么就让点击事件无效

        if(isMove){
            return;
        }
        int i = parent.getId();
        if (i == R.id.userGridView) {//position为 0，1 的不可以进行任何操作
            if (position != 0 && position != 1) {
                final ImageView moveImageView = getView(view);
                if (moveImageView != null) {
                    TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                    final int[] startLocation = new int[2];
                    newTextView.getLocationInWindow(startLocation);
                    final ChannelItem channel = ((DragAdapter) parent.getAdapter()).getItem(position);//获取点击的频道内容
                    otherAdapter.setVisible(false);
                    //添加到最后一个
                    otherAdapter.addItem(channel);
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            try {
                                int[] endLocation = new int[2];
                                //获取终点的坐标
                                otherGridView.getChildAt(otherGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
                                MoveAnim(moveImageView, startLocation, endLocation, channel, userGridView);
                                userAdapter.setRemove(position);
                            } catch (Exception localException) {
                            }
                        }
                    }, 50L);
                }
            }

        } else if (i == R.id.otherGridView) {
            final ImageView moveImageView = getView(view);
            if (moveImageView != null) {
                TextView newTextView = (TextView) view.findViewById(R.id.text_item);
                final int[] startLocation = new int[2];
                newTextView.getLocationInWindow(startLocation);
                final ChannelItem channel = ((OtherAdapter) parent.getAdapter()).getItem(position);
                userAdapter.setVisible(false);
                //添加到最后一个
                userAdapter.addItem(channel);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        try {
                            int[] endLocation = new int[2];
                            //获取终点的坐标
                            userGridView.getChildAt(userGridView.getLastVisiblePosition()).getLocationInWindow(endLocation);
                            MoveAnim(moveImageView, startLocation, endLocation, channel, otherGridView);
                            otherAdapter.setRemove(position);
                        } catch (Exception localException) {
                        }
                    }
                }, 50L);
            }

        } else {
        }
    }
    /**
     * 点击ITEM移动动画
     * @param moveView
     * @param startLocation
     * @param endLocation
     * @param moveChannel
     * @param clickGridView
     */
    private void MoveAnim(View moveView, int[] startLocation,int[] endLocation, final ChannelItem moveChannel,
                          final GridView clickGridView) {
        int[] initLocation = new int[2];
        //获取传递过来的VIEW的坐标
        moveView.getLocationInWindow(initLocation);
        //得到要移动的VIEW,并放入对应的容器中
        final ViewGroup moveViewGroup = getMoveViewGroup();
        final View mMoveView = getMoveView(moveViewGroup, moveView, initLocation);
        //创建移动动画
        TranslateAnimation moveAnimation = new TranslateAnimation(
                startLocation[0], endLocation[0], startLocation[1],
                endLocation[1]);
        moveAnimation.setDuration(300L);//动画时间
        //动画配置
        AnimationSet moveAnimationSet = new AnimationSet(true);
        moveAnimationSet.setFillAfter(false);//动画效果执行完毕后，View对象不保留在终止的位置
        moveAnimationSet.addAnimation(moveAnimation);
        mMoveView.startAnimation(moveAnimationSet);
        moveAnimationSet.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isMove = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                moveViewGroup.removeView(mMoveView);
                // instanceof 方法判断2边实例是不是一样，判断点击的是DragGrid还是OtherGridView
                if (clickGridView instanceof DragGrid) {
                    otherAdapter.setVisible(true);
                    otherAdapter.notifyDataSetChanged();
                    userAdapter.remove();
                }else{
                    userAdapter.setVisible(true);
                    userAdapter.notifyDataSetChanged();
                    otherAdapter.remove();
                }
                isMove = false;
            }
        });
    }

    /**
     * 获取移动的VIEW，放入对应ViewGroup布局容器
     * @param viewGroup
     * @param view
     * @param initLocation
     * @return
     */
    private View getMoveView(ViewGroup viewGroup, View view, int[] initLocation) {
        int x = initLocation[0];
        int y = initLocation[1];
        viewGroup.addView(view);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayoutParams.leftMargin = x;
        mLayoutParams.topMargin = y;
        view.setLayoutParams(mLayoutParams);
        return view;
    }

    /**
     * 创建移动的ITEM对应的ViewGroup布局容器
     */
    private ViewGroup getMoveViewGroup() {
        ViewGroup moveViewGroup = (ViewGroup) getWindow().getDecorView();
        LinearLayout moveLinearLayout = new LinearLayout(this);
        moveLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        moveViewGroup.addView(moveLinearLayout);
        return moveLinearLayout;
    }

    /**
     * 获取点击的Item的对应View，
     * @param view
     * @return
     */
    private ImageView getView(View view) {
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        Bitmap cache = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(cache);
        return iv;
    }

    /** 退出时候保存选择后数据库的设置  */
    private void saveChannel() {
        ChannelManage.getManage(IApplication.getApp().getSQLHelper()).deleteAllChannel();
        ChannelManage.getManage(IApplication.getApp().getSQLHelper()).saveUserChannel(userAdapter.getChannnelLst());
        ChannelManage.getManage(IApplication.getApp().getSQLHelper()).saveOtherChannel(otherAdapter.getChannnelLst());
    }

    @Override
    public void onBackPressed() {
        saveChannel();
        super.onBackPressed();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        builder.show();
        return false;
    }
}
