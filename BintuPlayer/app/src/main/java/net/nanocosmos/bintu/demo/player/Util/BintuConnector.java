package net.nanocosmos.bintu.demo.player.Util;

import net.nanocosmos.bintu.bintusdk.BintuSDK;

/**
 * Created by nanocosmos GmbH (c) 2016
 */
public class BintuConnector {
    private static BintuConnector instance = null;
    private String bintuApiKey = "";
    private BintuSDK bintu;

    public static BintuConnector getInstance() {
        if(null == instance) {
            instance = new BintuConnector();
        }

        return instance;
    }

    private BintuConnector(){

    }

    public void setBintuApiKey(String bintuApiKey) {
        this.bintuApiKey = bintuApiKey;
        bintu = new BintuSDK(this.bintuApiKey);
    }

    public BintuSDK getBintu() {
        return bintu;
    }
}
