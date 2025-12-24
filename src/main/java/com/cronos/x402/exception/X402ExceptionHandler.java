package com.cronos.x402.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class X402ExceptionHandler {

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentRequired(PaymentRequiredException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Payment Required");
        body.put("code", 402);
        body.put("message", "Veuillez effectuer un paiement sur Cronos pour accéder à cette ressource.");
        
        // Infos cruciales pour l'Agent IA
        Map<String, Object> paymentDetails = new HashMap<>();
        paymentDetails.put("chain", "CRONOS_EVM");
        paymentDetails.put("currency", "CRO");
        paymentDetails.put("amount", ex.getAmount());
        paymentDetails.put("receiver_address", ex.getWalletAddress());
        
        body.put("x402_details", paymentDetails);

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }
}
