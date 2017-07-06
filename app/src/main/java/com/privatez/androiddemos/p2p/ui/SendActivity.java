package com.privatez.androiddemos.p2p.ui;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.base.CommonAdapter;
import com.privatez.androiddemos.base.ViewHolder;
import com.privatez.androiddemos.p2p.bean.UserServiceInfo;
import com.privatez.androiddemos.p2p.helper.FileSendHelper;
import com.privatez.androiddemos.p2p.helper.NsdHelper;
import com.privatez.androiddemos.p2p.send.SendHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by private on 2017/5/4.
 */

public class SendActivity extends BaseActivity {

    private TextView tvDiscover;
    private ListView lvNsd;

    private List<UserServiceInfo> mUserServiceInfos;

    private CommonAdapter<UserServiceInfo> mAdapter;

    private FileSendHelper mFileSendHelper;

    private NsdHelper mNsdHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_send);

        tvDiscover = (TextView) findViewById(R.id.tv_discover);
        lvNsd = (ListView) findViewById(R.id.lv_nsd);

        mUserServiceInfos = new ArrayList<>();

        lvNsd.setAdapter(mAdapter = new CommonAdapter<UserServiceInfo>(
                mContext, mUserServiceInfos, R.layout.item_nsd) {
            @Override
            public void convert(ViewHolder holder, UserServiceInfo item) {
                holder.setText(R.id.tv_name, item.getDisplayName());
            }
        });

        lvNsd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NsdServiceInfo serviceInfo = mUserServiceInfos.get(position).mNsdServiceInfo;
                mFileSendHelper.connectToServer(serviceInfo.getPort(), serviceInfo.getHost());
            }
        });

        mFileSendHelper = new FileSendHelper(new SendHandler(mContext) {
            @Override
            public void lostService(UserServiceInfo serviceInfo) {

            }

            @Override
            public void resolveService(UserServiceInfo serviceInfo) {

            }
        });

        initializeNsdHelper();

        tvDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNsdHelper.discoverServices();
            }
        });
    }

    private void initializeNsdHelper() {
        mNsdHelper = new NsdHelper(mContext, new SendHandler(mContext) {
            @Override
            public void lostService(UserServiceInfo serviceInfo) {
                final int position = getPosition(serviceInfo);
                if (position != -1) {
                    mUserServiceInfos.remove(position);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void resolveService(UserServiceInfo serviceInfo) {
                final int position = getPosition(serviceInfo);
                if (position == -1) {
                    mUserServiceInfos.add(serviceInfo);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        mNsdHelper.discoverServices();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNsdHelper.stopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private int getPosition(UserServiceInfo serviceInfo) {
        for (int i = 0; i < mUserServiceInfos.size(); i++) {
            if (mUserServiceInfos.get(i).equals(serviceInfo)) {
                return i;
            }
        }

        return -1;
    }

}
