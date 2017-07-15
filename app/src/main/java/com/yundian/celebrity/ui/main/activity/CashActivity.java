package com.yundian.celebrity.ui.main.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.yundian.celebrity.R;
import com.yundian.celebrity.app.Constant;
import com.yundian.celebrity.base.BaseActivity;
import com.yundian.celebrity.bean.AssetDetailsBean;
import com.yundian.celebrity.bean.BankInfoBean;
import com.yundian.celebrity.bean.OrderReturnBeen;
import com.yundian.celebrity.bean.WithDrawCashReturnBean;
import com.yundian.celebrity.helper.CheckHelper;
import com.yundian.celebrity.listener.OnAPIListener;
import com.yundian.celebrity.networkapi.NetworkAPIFactoryImpl;
import com.yundian.celebrity.utils.FormatUtil;
import com.yundian.celebrity.utils.JudgeIsSetPayPwd;
import com.yundian.celebrity.utils.LogUtils;
import com.yundian.celebrity.utils.NumberUtils;
import com.yundian.celebrity.utils.SharePrefUtil;
import com.yundian.celebrity.utils.ToastUtils;
import com.yundian.celebrity.widget.NormalTitleBar;
import com.yundian.celebrity.widget.PasswordView;

import butterknife.Bind;
import butterknife.OnClick;


/**
 * Created by Benjamin on 17/1/12.
 */

public class CashActivity extends BaseActivity {
    @Bind(R.id.nt_title)
    NormalTitleBar ntTitle;
    @Bind(R.id.tv_user_bank_cssount)
    TextView tvUserBankCssount;
    @Bind(R.id.et_input_cash_money)
    EditText etInputCashMoney;
    @Bind(R.id.tv_cash_all_money)
    TextView tvCashAllMoney;
    @Bind(R.id.cash)
    Button cash;
    @Bind(R.id.tv_user_available_money)
    TextView userAvailableMoney;
    @Bind(R.id.passwordView)
    PasswordView passwordView;
    private boolean flag = true;
    private double inputPrice;
    private CheckHelper checkHelper = new CheckHelper();

    @Override
    public int getLayoutId() {
        return R.layout.activity_cash;
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void initView() {
        initData();
        requestBankInfo();
        requestBalance();
        initListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            requestBalance();
        }
    }

    private void requestBalance() {
        NetworkAPIFactoryImpl.getDealAPI().balance(new OnAPIListener<AssetDetailsBean>() {
            @Override
            public void onSuccess(AssetDetailsBean bean) {
                LogUtils.loge("余额请求成功:" + bean.toString());
                SharePrefUtil.getInstance().putBalance(bean.getBalance());
                userAvailableMoney.setText(String.format(getString(R.string.cash_available_money),
                        bean.getBalance()+""));
            }

            @Override
            public void onError(Throwable ex) {
                LogUtils.loge("余额请求失败:" + ex.getMessage());
            }
        });
    }



    private void initData() {
        ntTitle.setTitleText(getString(R.string.money_cash));
        ntTitle.setRightTitle(getString(R.string.cash_history));
        ntTitle.setRightTitleVisibility(true);

        etInputCashMoney.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        NumberUtils.setEditTextPoint(etInputCashMoney, 2);  //设置输入 提现金额的小数位数
        userAvailableMoney.setText(String.format(getString(R.string.cash_available_money),
                NumberUtils.halfAdjust2(Double.parseDouble(SharePrefUtil.getInstance().getBalance()))));
        checkHelper.checkButtonState1(cash, etInputCashMoney);
    }

    private void initListener() {
        ntTitle.setOnRightTextListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(CashHistoryActivity.class, 100);
            }
        });

        passwordView.setOnFinishInput(new PasswordView.CheckPasCallBake() {

            @Override
            public void checkSuccess(OrderReturnBeen.OrdersListBean ordersListBean, String pwd) {

            }

            @Override
            public void checkError() {
            }

            @Override
            public void checkSuccessPwd(String pwd) {
                doCash(pwd);
                passwordView.setVisibility(View.GONE);
            }
        });

    }

    @OnClick({R.id.tv_cash_all_money, R.id.cash, R.id.tv_forget_deal_pwd, R.id.tv_user_bank_cssount})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_cash_all_money:
                if (SharePrefUtil.getInstance().getBalance() != null) {
                    etInputCashMoney.setText(NumberUtils.halfAdjust2(Double.parseDouble(SharePrefUtil.getInstance().getBalance())));
                }

                break;
            case R.id.cash:
                if (JudgeIsSetPayPwd.isSetPwd(this)) {
                    requestCash();
                }
                break;
            case R.id.tv_forget_deal_pwd:
                Bundle bundle = new Bundle();
                bundle.putString("resetPwd", Constant.PAY_PWD);
                startActivity(ResetPayPwdActivity.class, bundle);
                break;
            case R.id.tv_user_bank_cssount:  //模拟测试数据
                ToastUtils.showShort("测试数据");
                startActivity(CashHistoryActivity_Test.class);

                break;
        }
    }


    private void requestBankInfo() {
        String cardNo = SharePrefUtil.getInstance().getCardNo();
        NetworkAPIFactoryImpl.getDealAPI().bankCardInfo(cardNo, new OnAPIListener<BankInfoBean>() {
            @Override
            public void onSuccess(BankInfoBean bankInfoBean) {

                if (TextUtils.isEmpty(bankInfoBean.getCardNO()) || TextUtils.isEmpty(bankInfoBean.getBankName())) {
//                    LogUtils.loge("银行卡列表失败----------------------------------------------");
//                    ToastUtils.showShort("请先绑定银行卡");
//                    startActivity(BankCardInfoActivity.class);
                } else {
                    LogUtils.loge("银行卡信息----------------成功");
                    tvUserBankCssount.setText(String.format(getString(R.string.name_code), bankInfoBean.getBankName(), FormatUtil.getCardEnd(bankInfoBean.getCardNO())));
                }

            }

            @Override
            public void onError(Throwable ex) {
                LogUtils.loge("银行卡错误------------------------");
//                ToastUtils.showShort("请先绑定银行卡");
//                startActivity(BankCardInfoActivity.class);
            }
        });
    }

    private void requestCash() {
        inputPrice = Double.parseDouble(etInputCashMoney.getText().toString().trim());
        double balance = Double.parseDouble(SharePrefUtil.getInstance().getBalance());
        if (inputPrice > balance) {
            ToastUtils.showShort("余额不足");
            return;
        } else if (inputPrice <= 0) {
            ToastUtils.showShort("输入金额有误");
            return;
        }

//        if (inputPrice < 2) {
//            ToastUtils.showShort("提现金额必须大于等于2元");
//            return;
//        } else
        if (inputPrice > 50000) {
            ToastUtils.showShort("提现金额超出范围");
            return;
        }
        //吊起支付
        passwordView.setVisibility(View.VISIBLE);

    }


    private void doCash(String pwd) {
        startProgressDialog("正在处理...");
        NetworkAPIFactoryImpl.getDealAPI().cashOut(inputPrice, pwd, new OnAPIListener<WithDrawCashReturnBean>() {
            @Override
            public void onError(Throwable ex) {
                stopProgressDialog();
                ToastUtils.showStatusView("提现失败", false);
            }

            @Override
            public void onSuccess(WithDrawCashReturnBean withDrawCashReturnBean) {
                stopProgressDialog();
                if (withDrawCashReturnBean.getResult() == 1) {
                    ToastUtils.showStatusView("提现成功", true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivityForResult(CashHistoryActivity.class, 100);
                        }
                    },1000);

                } else {
                    ToastUtils.showStatusView("提现失败", false);
                }

            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (passwordView.getVisibility() == View.VISIBLE) {
                    passwordView.setVisibility(View.GONE);
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }
        }
        return super.onKeyDown(keyCode, event);
    }

}