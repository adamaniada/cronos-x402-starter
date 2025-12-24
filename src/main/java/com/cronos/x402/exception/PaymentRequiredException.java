package com.cronos.x402.exception;

public class PaymentRequiredException extends RuntimeException {
    private final double amount;
    private final String walletAddress;

    public PaymentRequiredException(double amount, String walletAddress) {
        super("Payment Required: " + amount + " CRO to " + walletAddress);
        this.amount = amount;
        this.walletAddress = walletAddress;
    }

    public double getAmount() { return amount; }
    public String getWalletAddress() { return walletAddress; }
}
