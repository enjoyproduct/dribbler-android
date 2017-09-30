package com.mg.dribbler.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.mg.dribbler.R;
import com.mg.dribbler.activities.MainActivity;
import com.mg.dribbler.models.User;
import com.mg.dribbler.utils.UIUtil;

import me.relex.circleindicator.CircleIndicator;

/**
 * Created by KCB on 4/9/2017.
 */

public class PremiumUpgradeFragment extends Fragment implements BillingProcessor.IBillingHandler {

    // Fragment initialization parameters
    private int mPremiumPageIndex;

    private View contentView;
    private MainActivity activity;

    BillingProcessor bp;

    private Button btnSubscribe;
    /**
     * Life Cycle
     */

    public PremiumUpgradeFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mPremiumPageIndex = getArguments().getInt("position");
        }

        bp = new BillingProcessor(activity, "YOUR LICENSE KEY FROM GOOGLE PLAY CONSOLE HERE", this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        switch (mPremiumPageIndex) {
            case 0:
                contentView = inflater.inflate(R.layout.fragment_premium1, container, false);
                contentView.findViewById(R.id.btn_train).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.switchFragment(1);
                    }
                });
                break;
            case 1:
                if (User.currentUser().subscribe == 1 || User.currentUser().subscribe == 3) {
                    contentView = inflater.inflate(R.layout.fragment_premium3, container, false);
                    btnSubscribe = (Button) contentView.findViewById(R.id.btn_train);
                    btnSubscribe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.switchFragment(1);
                        }
                    });
                } else {
                    contentView = inflater.inflate(R.layout.fragment_premium2, container, false);
                    btnSubscribe = (Button) contentView.findViewById(R.id.btn_subscribe);
                    btnSubscribe.setText(getResources().getString(R.string.I_want_all_advanced_tricks));
                    btnSubscribe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscribe(1);
                        }
                    });
                }
                break;
            case 2:
                if (User.currentUser().subscribe == 2 || User.currentUser().subscribe == 3) {
                    contentView = inflater.inflate(R.layout.fragment_premium3, container, false);
                    btnSubscribe = (Button) contentView.findViewById(R.id.btn_train);
                    btnSubscribe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.switchFragment(1);
                        }
                    });
                } else {
                    contentView = inflater.inflate(R.layout.fragment_premium2, container, false);
                    btnSubscribe = (Button) contentView.findViewById(R.id.btn_subscribe);
                    btnSubscribe.setText(getResources().getString(R.string.I_want_all_pro_tricks));
                    btnSubscribe.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            subscribe(2);
                        }
                    });
                }

                break;
        }

        return contentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }

        super.onDestroy();
    }

    /**
     * Subscribe Methods
     */
    private void subscribe(int type) {
        bp.purchase(activity, "YOUR PRODUCT ID FROM GOOGLE PLAY CONSOLE HERE");
        bp.purchase(activity, "YOUR PRODUCT ID FROM GOOGLE PLAY CONSOLE HERE", "DEVELOPER PAYLOAD HERE");
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        UIUtil.showToast(activity, "You have purchased successfully");
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        UIUtil.showToast(activity, "Something went wrong");
    }

    @Override
    public void onBillingInitialized() {

    }
}
