package com.yundian.celebrity.ui.main.fragment;

import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.session.SessionCustomization;

import com.yundian.celebrity.R;
import com.yundian.celebrity.base.BaseFragment;
import com.yundian.celebrity.bean.HaveStarUsersBean;
import com.yundian.celebrity.listener.OnAPIListener;
import com.yundian.celebrity.networkapi.NetworkAPIFactoryImpl;
import com.yundian.celebrity.ui.main.adapter.FansTalkAdapter;
import com.yundian.celebrity.ui.wangyi.session.activity.P2PMessageActivity;
import com.yundian.celebrity.utils.LogUtils;
import com.yundian.celebrity.utils.SharePrefUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 粉丝聊天
 */

public class FansTalkFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.RequestLoadMoreListener {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout swipeLayout;

    private FansTalkAdapter fansTalkAdapter;
    private List<HaveStarUsersBean> dataList = new ArrayList<>();
    private int mCurrentCounter = 1;
    private static final int REQUEST_COUNT = 10;


    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_fans_talk;
    }

    @Override
    public void initPresenter() {
    }

    @Override
    protected void initView() {
        initFindById();
        initAdapter();
        getData(false, 1, 10);
    }

    private void initFindById() {
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_list);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeLayout);
    }

    private void initAdapter() {
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(Color.rgb(47, 223, 189));
        fansTalkAdapter = new FansTalkAdapter(R.layout.adapter_fans_talk_item, dataList);
        fansTalkAdapter.setOnLoadMoreListener(this, mRecyclerView);
        mRecyclerView.setAdapter(fansTalkAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCurrentCounter = fansTalkAdapter.getData().size();
        fansTalkAdapter.setEmptyView(R.layout.message_search_empty_view, (ViewGroup) mRecyclerView.getParent());
        fansTalkAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                HaveStarUsersBean haveStarUsersBean = fansTalkAdapter.getData().get(position);
                SessionCustomization customization = NimUIKit.getCommonP2PSessionCustomization();
                P2PMessageActivity.start(getActivity(), haveStarUsersBean.getFaccid(), haveStarUsersBean.getStarcode(),
                        haveStarUsersBean.getNickname(), customization, null);
            }
        });
    }

    @Override
    public void onRefresh() {
        fansTalkAdapter.setEnableLoadMore(false);
        getData(false, 1, REQUEST_COUNT);
    }

    @Override
    public void onLoadMoreRequested() {
        swipeLayout.setEnabled(false);
        getData(true, mCurrentCounter + 1, REQUEST_COUNT);
        swipeLayout.setEnabled(true);
    }

    public void getData(final boolean isLoadMore, int start, int count) {
        String starCode = SharePrefUtil.getInstance().getStarcode();
        NetworkAPIFactoryImpl.getDealAPI().fansList(starCode, start, count, new OnAPIListener<List<HaveStarUsersBean>>() {
            @Override
            public void onSuccess(List<HaveStarUsersBean> listBeans) {
                if (listBeans == null || listBeans.size() == 0) {
                    fansTalkAdapter.loadMoreEnd(true);  //显示"没有更多数据"
                    return;
                }
                if (isLoadMore) {   //上拉加载--成功获取数据
                    fansTalkAdapter.addData(listBeans);
                    mCurrentCounter = fansTalkAdapter.getData().size();
                    fansTalkAdapter.loadMoreComplete();
                } else {  //下拉刷新  成功获取数据
                    fansTalkAdapter.setNewData(listBeans);
                    mCurrentCounter = listBeans.size();
                    swipeLayout.setRefreshing(false);
                    fansTalkAdapter.setEnableLoadMore(true);
                }
            }

            @Override
            public void onError(Throwable ex) {
                if (isLoadMore) {
                    fansTalkAdapter.loadMoreEnd();
                } else {
                    swipeLayout.setRefreshing(false);  //下拉刷新,应该显示空白页
                    fansTalkAdapter.setEnableLoadMore(true);
                }
                LogUtils.loge("粉丝列表失败-----------");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        onHiddenChanged(getUserVisibleHint());
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            LogUtils.loge("粉丝聊天界面:onHiddenChanged-----------------------------刷新首页" + isVisible());
        } else {
            LogUtils.loge("bu可见------------------刷新");
        }
        super.onHiddenChanged(hidden);

    }

}
