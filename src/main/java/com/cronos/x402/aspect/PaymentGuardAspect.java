package com.cronos.x402.aspect;

import com.cronos.x402.annotation.X402Paywall;
import com.cronos.x402.config.CronosProperties;
import com.cronos.x402.exception.PaymentRequiredException;
import com.cronos.x402.service.CronosService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class PaymentGuardAspect {

    private final CronosService cronosService;
    private final CronosProperties properties;

    public PaymentGuardAspect(CronosService cronosService, CronosProperties properties) {
        this.cronosService = cronosService;
        this.properties = properties;
    }

    @Around("@annotation(paywall)")
    public Object checkPayment(ProceedingJoinPoint joinPoint, X402Paywall paywall) throws Throwable {
        if (!properties.isEnabled()) {
            return joinPoint.proceed(); // Si le plugin est désactivé, on laisse passer
        }

        // 1. Récupérer la requête HTTP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return joinPoint.proceed();
        HttpServletRequest request = attributes.getRequest();

        // 2. Chercher le Header du Hash de transaction
        String txHash = request.getHeader("X-Payment-Hash");
        
        // Déterminer le wallet destinataire (celui de l'annotation OU celui par défaut)
        String targetWallet = paywall.destinationWallet().isEmpty() ? properties.getReceiverWallet() : paywall.destinationWallet();

        // 3. Cas 1 : Pas de paiement fourni -> Erreur 402
        if (txHash == null || txHash.isEmpty()) {
            throw new PaymentRequiredException(paywall.amount(), targetWallet);
        }

        // 4. Cas 2 : Paiement fourni -> Vérification Blockchain
        boolean isValid = cronosService.verifyTransaction(txHash, paywall.amount(), targetWallet);

        if (!isValid) {
            throw new IllegalArgumentException("Paiement invalide ou montant incorrect !");
        }

        // 5. Succès -> On laisse passer
        return joinPoint.proceed();
    }
}
