package com.cronos.x402.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cronos.x402")
public class CronosProperties {
    
    private String rpcUrl = "https://evm-t3.cronos.org/"; // Testnet par défaut
    private String receiverWallet; // L'adresse du développeur qui reçoit les fonds
    private boolean enabled = true;

    // Getters et Setters standard
    public String getRpcUrl() { return rpcUrl; }
    public void setRpcUrl(String rpcUrl) { this.rpcUrl = rpcUrl; }
    public String getReceiverWallet() { return receiverWallet; }
    public void setReceiverWallet(String receiverWallet) { this.receiverWallet = receiverWallet; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
