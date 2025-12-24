package com.cronos.x402.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface X402Paywall {
    // Montant requis en CRO (ex: 1.5)
    double amount();
    
    // Adresse spécifique pour ce endpoint (optionnel, sinon utilise celle par défaut)
    String destinationWallet() default "";
}
