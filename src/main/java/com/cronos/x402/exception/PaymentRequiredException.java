package com.cronos.x402.exception;

public class PaymentRequiredException extends RuntimeException {
    private final double amount;
    private final String walletAddress;

    // Constructeur par défaut (Pas de paiement fourni)
    public PaymentRequiredException(double amount, String walletAddress) {
        super("Payment Required: " + amount + " CRO to " + walletAddress);
        this.amount = amount;
        this.walletAddress = walletAddress;
    }

    // Constructeur avec message d'erreur spécifique (Paiement invalide)
    public PaymentRequiredException(double amount, String walletAddress, String message) {
        super(message);
        this.amount = amount;
        this.walletAddress = walletAddress;
    }

    public double getAmount() { return amount; }
    public String getWalletAddress() { return walletAddress; }
}
