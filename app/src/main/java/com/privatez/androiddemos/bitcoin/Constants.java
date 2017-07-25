package com.privatez.androiddemos.bitcoin;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

/**
 * Created by private on 2017/7/25.
 */

public class Constants {
    public static final boolean TEST = true;

    /**
     * Network this wallet is on (e.g. testnet or mainnet).
     */
    public static final NetworkParameters NETWORK_PARAMETERS = TEST ? TestNet3Params.get() : MainNetParams.get();

    public static class Files {
        private static final String FILENAME_NETWORK_SUFFIX = NETWORK_PARAMETERS.getId()
                .equals(NetworkParameters.ID_MAINNET) ? "" : "-testnet";

        /** Filename of the wallet. */
        public static final String WALLET_FILENAME_PROTOBUF = "wallet-protobuf" + FILENAME_NETWORK_SUFFIX;

        public static final String WALLET_KEY_BACKUP_PROTOBUF = "key-backup-protobuf" + FILENAME_NETWORK_SUFFIX;
    }
}
