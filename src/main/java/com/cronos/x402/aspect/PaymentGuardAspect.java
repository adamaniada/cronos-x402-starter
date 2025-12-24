package com.cronos.x402.aspect;

import com.cronos.x402.annotation.X402Paywall;
import com.cronos.x402.config.CronosProperties;
import com.cronos.x402.exception.PaymentRequiredException;
import com.cronos.x402.service.CronosService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class PaymentGuardAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentGuardAspect.class);
    private final CronosService cronosService;
    private final CronosProperties properties;

    public PaymentGuardAspect(CronosService cronosService, CronosProperties properties) {
        this.cronosService = cronosService;
        this.properties = properties;
    }

    @Around("@annotation(paywall)")
    public Object checkPayment(ProceedingJoinPoint joinPoint, X402Paywall paywall) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed(); 
        }

        // 1. Récupérer la requête HTTP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return joinPoint.proceed();
        HttpServletRequest request = attributes.getRequest();

        // 2. Chercher le Header du Hash de transaction
        String txHash = request.getHeader("X-Payment-Hash");
        
        // Déterminer le wallet destinataire
        String targetWallet = paywall.destinationWallet().isEmpty() ? properties.getReceiverWallet() : paywall.destinationWallet();

        // 3. Cas 1 : Pas de hash fourni -> Erreur 402 classique
        if (txHash == null || txHash.isEmpty()) {
            throw new PaymentRequiredException(paywall.amount(), targetWallet);
        }

        // 4. Cas 2 : Hash fourni -> Vérification Blockchain
        // Note: verifyTransaction renvoie maintenant une String (null si OK, message d'erreur sinon) 
        // ou on garde le boolean et on gère l'erreur ici. Gardons le boolean pour simplifier la logique existante.
        
        boolean isValid = cronosService.verifyTransaction(txHash, paywall.amount(), targetWallet);

        if (!isValid) {
            LOGGER.warn("Paiement rejeté pour le hash: {}", txHash);
            // ICI: On lance la même exception, mais avec un message d'erreur précis
            throw new PaymentRequiredException(
                paywall.amount(),
                targetWallet,
                "Paiement invalide ou montant incorrect sur la blockchain Cronos."
            );
        }

        // 5. Succès -> On laisse passer
        return joinPoint.proceed();
    }
}
