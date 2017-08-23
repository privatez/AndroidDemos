package com.privatez.androiddemos.bitcoin.examples;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.Wallet;

import java.util.List;

/**
 * Created by private on 2017/8/10.
 */

public class Test {
    public static void main(String[] args) {
        NetworkParameters params = MainNetParams.get();

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

        String pub58Key = wallet.getWatchingKey().serializePubB58(params);

        log(wallet, params);

        Wallet wallet1 = Wallet.fromWatchingKeyB58(
                params,
                pub58Key,
                DeterministicHierarchy.BIP32_STANDARDISATION_TIME_SECS);

        log(wallet1, params);

        DeterministicKey watchkey = DeterministicKey.deserializeB58(
                pub58Key,
                params);
        Wallet wallet2 = Wallet.fromWatchingKey(params, watchkey);

        log(wallet2, params);
    }

    private static void log(Wallet wallet, NetworkParameters params) {
        System.out.println(wallet.toString());

        /*DeterministicSeed seed = wallet.getKeyChainSeed();

        System.out.println("seed: " + seed.toString());

        System.out.println("creation time: " + seed.getCreationTimeSeconds());
        System.out.println("mnemonicCode: " + seed.getMnemonicCode());*/

       /* System.out.println(wallet.getWatchingKey().serializePublic(params));
        System.out.println(wallet.getWatchingKey().serializePubB58(params));
        System.out.println(wallet.getWatchingKey().serializePrivate(params));
        System.out.println(wallet.getWatchingKey().serializePrivB58(params));*/

        List<ECKey> ecKeys = wallet.getImportedKeys();
        for (ECKey ecKey : ecKeys) {
            System.out.println("im:" + ecKey.getPrivateKeyAsWiF(params));
        }

        ecKeys = wallet.getIssuedReceiveKeys();
        for (ECKey ecKey : ecKeys) {
            System.out.println("re:" + ecKey.getPrivateKeyAsWiF(params));
        }

        System.out.println(wallet.freshReceiveAddress().toString());
        System.out.println("==========================");
        System.out.println("=");
        System.out.println("=");
        System.out.println("=");
        System.out.println("=");
        System.out.println("=");
        System.out.println("=");
        System.out.println("==========================");
    }

    private static void tx(Wallet wallet) {

    }
}

