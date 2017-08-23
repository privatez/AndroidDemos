package com.privatez.androiddemos.bitcoin;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.util.LogHelper;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
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

import javax.annotation.Nullable;

/**
 * Created by private on 2017/7/24.
 */

public class BitCoinActivity extends BaseActivity {
    private TextView tvGenerater;

    private Wallet mWallet;

    private File mWalletFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitcoin);

        LogHelper.log(Environment.getDataDirectory().getAbsolutePath());
        LogHelper.log(getApplicationContext().getFilesDir().getAbsolutePath());

        tvGenerater = (TextView) findViewById(R.id.tv_generater);

        tvGenerater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWallet == null) {

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

        mWalletFile = getFileStreamPath(Constants.Files.WALLET_FILENAME_PROTOBUF);

        //test();

        loadWalletFromProtobuf();
        getEtAddress().setText(mWallet.freshReceiveAddress().toBase58());
        /*new Runnable() {
            @Override
            public void run() {
                try {
                    Test.refreshWallet(mWallet, mWalletFile);
                } catch (BlockStoreException e) {
                    LogHelper.log("BlockStoreException" + e);
                } catch (IOException e) {
                    LogHelper.log("BlockStoreException" + e);
                }
            }
        }.run();*/

        try {
            MnemonicCode.INSTANCE = new MnemonicCode(getAssets().open("chinese.txt"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Wallet wallet = new Wallet(Constants.NETWORK_PARAMETERS);
        List<String> keys = wallet.getKeyChainSeed().getMnemonicCode();
        for (String key : keys) {
            LogHelper.log("key: " + key);
        }
    }

    private EditText getEtAddress() {
        return (EditText) findViewById(R.id.et_address);
    }

    private void test1() {
        final NetworkParameters params = TestNet3Params.get();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = null;
        try {
            chain = new BlockChain(params, mWallet, blockStore);
        } catch (BlockStoreException e) {
            e.printStackTrace();
        }

        final PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.addWallet(mWallet);

        mWallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                LogHelper.log("-----> coins resceived: " + tx.getHashAsString());
                LogHelper.log("received: " + tx.getValue(w));
            }
        });

        peerGroup.startAsync();
        peerGroup.startBlockChainDownload(new PeerDataEventListener() {
            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                LogHelper.log("onBlocksDownloaded");
            }

            @Override
            public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                LogHelper.log("onChainDownloadStarted");
            }

            @Nullable
            @Override
            public List<Message> getData(Peer peer, GetDataMessage m) {
                LogHelper.log("getData");
                return null;
            }

            @Override
            public Message onPreMessageReceived(Peer peer, Message m) {
                LogHelper.log("onPreMessageReceived");
                return null;
            }
        });

        try {
            mWallet.saveToFile(mWalletFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.AVAILABLE));
        LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE));
        LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.ESTIMATED));
        LogHelper.log("balance:" + mWallet.getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE));

    }

    private void test() {
        NetworkParameters parameters = Constants.NETWORK_PARAMETERS;
        WalletAppKit appKit = new WalletAppKit(parameters, getFilesDir(), "wallet-example");

        appKit.startAsync();
        appKit.awaitRunning();

        appKit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                LogHelper.log("-----> coins resceived: " + tx.getHashAsString());
                LogHelper.log("received: " + tx.getValue(wallet));
            }
        });
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

        if (mWalletFile.exists()) {
            FileInputStream walletStream = null;

            try {
                final Stopwatch watch = Stopwatch.createStarted();
                walletStream = new FileInputStream(mWalletFile);
                mWallet = new WalletProtobufSerializer().readWallet(walletStream);
                watch.stop();

                if (!mWallet.getParams().equals(Constants.NETWORK_PARAMETERS))
                    throw new UnreadableWalletException("bad mWallet network parameters: " + mWallet.getParams().getId());

                LogHelper.log("mWallet loaded from:" + mWalletFile);
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
                Toast.makeText(this, "inconsistent mWallet: " + mWalletFile, Toast.LENGTH_LONG).show();

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
        wallet.saveToFile(mWalletFile);
        watch.stop();

        LogHelper.log("wallet saved to: '{}', took {}" + mWalletFile);
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
