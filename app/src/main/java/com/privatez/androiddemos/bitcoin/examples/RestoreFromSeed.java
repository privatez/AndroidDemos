/*
 * Copyright by the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.privatez.androiddemos.bitcoin.examples;

import com.google.common.util.concurrent.ListenableFuture;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.GetDataMessage;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * The following example shows you how to restore a HD wallet from a previously generated deterministic seed.
 * In this example we manually setup the blockchain, peer group, etc. You can also use the WalletAppKit which provides a restoreWalletFromSeed function to load a wallet from a deterministic seed.
 */
public class RestoreFromSeed {

    public static void main(String[] args) throws Exception {
        NetworkParameters params = MainNetParams.get();

        // Bitcoinj supports hierarchical deterministic wallets (or "HD Wallets"): https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki
        // HD wallets allow you to restore your wallet simply from a root seed. This seed can be represented using a short mnemonic sentence as described in BIP 39: https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki

        // Here we restore our wallet from a seed with no passphrase. Also have a look at the BackupToMnemonicSeed.java example that shows how to backup a wallet by creating a mnemonic sentence.
        String seedCode = "mouse use pattern seven rotate cruise own develop speak sight eternal practice";
        String passphrase = "";
        Long creationtime = 1501567958L;

        //DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, creationtime);

        // The wallet class provides a easy fromSeed() function that loads a new wallet from a given seed.
        //Wallet wallet = Wallet.fromSeed(params, seed);

        // Decode the private key from Satoshis Base58 variant. If 51 characters long then it's from Bitcoins
        // dumpprivkey command and includes a version byte and checksum, or if 52 characters long then it has
        // compressed pub key. Otherwise assume it's a raw key.
        args = new String[]{"L2P4VAGHJqoqA9UEvzYKdZCNRYGBk6PLJhhY3Lz5PihmfnwKcnQ2"};
        ECKey key;
        if (args[0].length() == 51 || args[0].length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, args[0]);
            key = dumpedPrivateKey.getKey();
        } else {
            key = ECKey.fromPrivate(args[0].getBytes());
        }
        System.out.println("Address from private key is: " + key.toAddress(params).toString());

        // Import the private key to a fresh wallet.
        Wallet wallet = new Wallet(params);
        wallet.importKey(key);

        DeterministicSeed seed = wallet.getKeyChainSeed();

        System.out.println(wallet.toString());
        System.out.println("seed: " + seed.toString());

        System.out.println("creation time: " + seed.getCreationTimeSeconds());
        System.out.println("mnemonicCode: " + seed.getMnemonicCode());

        wallet = Wallet.fromWatchingKeyB58(params, "xpub68NvFy52DmKijh6ZfrfcR4nW7HdKC2qQ8RQmnGR8rcG3BTqoCaCeFFZqumjjV6cTvjn8Ni3BeSzDgRKxN7FydnsuTWJApdmgv6oDHt6Jbom", DeterministicHierarchy.BIP32_STANDARDISATION_TIME_SECS);

        System.out.println(wallet.getWatchingKey().serializePublic(params));
        System.out.println(wallet.getWatchingKey().serializePubB58(params));
        System.out.println(wallet.getWatchingKey().serializePrivate(params));
        System.out.println(wallet.getWatchingKey().serializePrivB58(params));

        // Because we are importing an existing wallet which might already have transactions we must re-download the blockchain to make the wallet picks up these transactions
        // You can find some information about this in the guides: https://bitcoinj.github.io/working-with-the-wallet#setup
        // To do this we clear the transactions of the wallet and delete a possible existing blockchain file before we download the blockchain again further down.
        System.out.println(wallet.toString());
        wallet.clearTransactions(0);
        File chainFile = new File("restore-from-seed.spvchain");
        if (chainFile.exists()) {
            System.out.println(chainFile.exists());
            chainFile.delete();
        }

        // Setting up the BlochChain, the BlocksStore and connecting to the network.
        SPVBlockStore chainStore = new SPVBlockStore(params, chainFile);
        BlockChain chain = new BlockChain(params, chainStore);
        PeerGroup peers = new PeerGroup(params, chain);
        peers.addPeerDiscovery(new DnsDiscovery(params));

        System.out.println("send money to: " + wallet.freshReceiveAddress().toString());

        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("onCoinsReceived" + newBalance);
            }
        });

        // Now we need to hook the wallet up to the blockchain and the peers. This registers event listeners that notify our wallet about new transactions.
        chain.addWallet(wallet);
        peers.addWallet(wallet);

        DownloadProgressTracker bListener = new DownloadProgressTracker() {
            @Override
            public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                super.onChainDownloadStarted(peer, blocksLeft);
                System.out.println("onChainDownloadStarted");
            }

            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
                //System.out.println("onBlocksDownloaded");
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                System.out.println("progress");
            }

            @Override
            protected void startDownload(int blocks) {
                super.startDownload(blocks);
                System.out.println("startDownload");
            }

            @Override
            public void await() throws InterruptedException {
                super.await();
                System.out.println("await");
            }

            @Override
            public ListenableFuture<Long> getFuture() {
                System.out.println("getFuture");
                return super.getFuture();
            }

            @Override
            public Message onPreMessageReceived(Peer peer, Message m) {
                //System.out.println("onPreMessageReceived");
                return super.onPreMessageReceived(peer, m);
            }

            @Override
            public List<Message> getData(Peer peer, GetDataMessage m) {
                System.out.println("getData");
                return super.getData(peer, m);
            }

            @Override
            public void doneDownload() {
                System.out.println("blockchain downloaded");
            }
        };

        // Now we re-download the blockchain. This replays the chain into the wallet.
        // Once this is completed our wallet should know of all its transactions and print the correct balance.
        peers.start();
        peers.startBlockChainDownload(bListener);

        bListener.await();

        // Print a debug message with the details about the wallet. The correct balance should now be displayed.
        System.out.println(wallet.toString());
        System.out.println("balance:" + wallet.getBalance(Wallet.BalanceType.AVAILABLE));
        System.out.println("balance:" + wallet.getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE));
        System.out.println("balance:" + wallet.getBalance(Wallet.BalanceType.ESTIMATED));
        System.out.println("balance:" + wallet.getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE));

        // shutting down again
        peers.stop();
    }
}
