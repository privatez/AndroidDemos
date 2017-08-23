package com.privatez.androiddemos.bitcoin;

import com.privatez.androiddemos.util.LogHelper;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.MemoryBlockStore;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;

/**
 * Created by private on 2017/7/31.
 */

public class Test {

    public static void refreshWallet(Wallet wallet, File file) throws BlockStoreException, IOException {
        LogHelper.log(wallet.toString());

        // Set up the components and link them together.
        final NetworkParameters params = TestNet3Params.get();
        BlockStore blockStore = new MemoryBlockStore(params);
        BlockChain chain = new BlockChain(params, wallet, blockStore);

        final PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.startAsync();

        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public synchronized void onCoinsReceived(Wallet w, Transaction tx, Coin prevBalance, Coin newBalance) {
                LogHelper.log("\nReceived tx " + tx.getHashAsString());
                LogHelper.log(tx.toString());
            }
        });

        // Now download and process the block chain.
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();
        wallet.saveToFile(file);
        LogHelper.log("\nDone!\n");
        LogHelper.log(wallet.toString());
    }

}
