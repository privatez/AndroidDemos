package com.privatez.androiddemos.bitcoin;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

/**
 * Created by private on 2017/7/24.
 */

public class Util {

    public static final class NetworkModel {
        public static final int MAIN = 0;
        public static final int TEST_NET = 1;
        public static final int REG_TEST = 2;
    }

    public static final NetworkParameters getNetworkParameters(int model) {
        NetworkParameters params = null;
        String filePrefix;

        switch (model) {
            case NetworkModel.MAIN:
                params = MainNetParams.get();
                filePrefix = "forwarding-service";
                break;
            case NetworkModel.TEST_NET:
                params = TestNet3Params.get();
                filePrefix = "forwarding-service-testnet";
                break;
            case NetworkModel.REG_TEST:
                params = RegTestParams.get();
                filePrefix = "forwarding-service-regtest";
                break;
            default:

                break;
        }

        return params;
    }
}
