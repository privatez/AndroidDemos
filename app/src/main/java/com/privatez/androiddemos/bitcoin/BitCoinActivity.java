package com.privatez.androiddemos.bitcoin;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.util.LogHelper;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by private on 2017/7/24.
 */

public class BitCoinActivity extends BaseActivity {
    private TextView tvGenerater;

    private Wallet mWallet;

    private File walletFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcoin);

        tvGenerater = (TextView) findViewById(R.id.tv_generater);

        tvGenerater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWallet == null) {
                    loadWalletFromProtobuf();
                    getEtAddress().setText(mWallet.freshReceiveAddress().toBase58());
                    mWallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
                        @Override
                        public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                            LogHelper.log("onCoinsReceived");
                        }
                    });
                    //checkBalance();
                }
            }
        });

        findViewById(R.id.tv_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.AVAILABLE));
                LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE));
                LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.ESTIMATED));
                LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE));
            }
        });

        walletFile = getFileStreamPath(Constants.Files.WALLET_FILENAME_PROTOBUF);
    }

    private EditText getEtAddress() {
        return (EditText) findViewById(R.id.et_address);
    }

    Handler mHandler = new Handler();

    private void checkBalance() {
        ExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }


    private void loadWalletFromProtobuf() {

        if (walletFile.exists()) {
            FileInputStream walletStream = null;

            try {
                final Stopwatch watch = Stopwatch.createStarted();
                walletStream = new FileInputStream(walletFile);
                mWallet = new WalletProtobufSerializer().readWallet(walletStream);
                watch.stop();

                if (!mWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
                    throw new UnreadableWalletException("bad mWallet network parameters: " + mWallet.getParams().getId());

                LogHelper.log("mWallet loaded from:" + walletFile);
            } catch (final FileNotFoundException x) {
                LogHelper.log("problem loading mWallet" + x);

                //mWallet = restoreWalletFromBackup();
            } catch (final UnreadableWalletException x) {
                LogHelper.log("problem loading mWallet" + x);

                //mWallet = restoreWalletFromBackup();
            } finally {
                if (walletStream != null) {
                    try {
                        walletStream.close();
                    } catch (final IOException x) {
                        // swallow
                    }
                }
            }

            if (!mWallet.isConsistent()) {
                Toast.makeText(this, "inconsistent mWallet: " + walletFile, Toast.LENGTH_LONG).show();

                //mWallet = restoreWalletFromBackup();
            }

            if (!mWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
                throw new Error("bad mWallet network parameters: " + mWallet.getParams().getId());
        } else {
            mWallet = new Wallet(Constants.NETWORK_PARAMETERS);

            saveWallet();
            backupWallet();

            //config.armBackupReminder();

            LogHelper.log("new mWallet created");
        }

        List<String> keys = mWallet.getKeyChainSeed().getMnemonicCode();
        for (String key : keys) {
            LogHelper.log("key: " + key);
        }

    }

    public void saveWallet() {
        try {
            protobufSerializeWallet(mWallet);
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    private void protobufSerializeWallet(final Wallet wallet) throws IOException {
        final Stopwatch watch = Stopwatch.createStarted();
        wallet.saveToFile(walletFile);
        watch.stop();

        LogHelper.log("wallet saved to: '{}', took {}" + walletFile);
    }

    public void backupWallet() {
        final Stopwatch watch = Stopwatch.createStarted();
        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(mWallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        OutputStream os = null;

        try {
            os = openFileOutput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF, Context.MODE_PRIVATE);
            walletProto.writeTo(os);
            watch.stop();
            LogHelper.log("wallet backed up to: '{}', took {}" + Constants.Files.WALLET_KEY_BACKUP_PROTOBUF);
        } catch (final IOException x) {
            LogHelper.log("problem writing wallet backup" + x);
        } finally {
            try {
                os.close();
            } catch (final IOException x) {
                // swallow
            }
        }
    }

}
