package com.cronos.x402.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CronosService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronosService.class);
    
    private final Web3j web3j;
    // Cache simple pour éviter le "Replay Attack" (Note: Redis serait mieux pour la prod)
    private final ConcurrentHashMap<String, Boolean> usedHashes = new ConcurrentHashMap<>();

    public CronosService(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public boolean verifyTransaction(String txHash, double expectedAmount, String expectedReceiver) {
        // 1. Vérifier si le hash a déjà été utilisé localement
        if (usedHashes.containsKey(txHash)) {
            LOGGER.warn("❌ REPLAY ATTACK: Ce hash a déjà été utilisé ! TxHash: {}", txHash);
            return false;
        }

        try {
            // 2. Récupérer la transaction sur la blockchain
            Optional<Transaction> txOptional = web3j.ethGetTransactionByHash(txHash).send().getTransaction();
            if (txOptional.isEmpty()) {
                LOGGER.warn("❌ Transaction introuvable sur Cronos. TxHash: {}", txHash);
                return false;
            }

            Transaction tx = txOptional.get();

            // 3. Vérifier le destinataire (Case insensitive)
            if (!tx.getTo().equalsIgnoreCase(expectedReceiver)) {
                LOGGER.warn("❌ Mauvais wallet destinataire. Reçu: {}, Attendu: {}", tx.getTo(), expectedReceiver);
                return false;
            }

            // 4. Vérifier le montant (Conversion précise Wei -> CRO)
            BigDecimal valueInWei = new BigDecimal(tx.getValue());
            BigDecimal valueInCro = Convert.fromWei(valueInWei, Convert.Unit.ETHER);
            
            // On utilise compareTo pour la précision BigDecimal (valeur >= attendu)
            if (valueInCro.compareTo(BigDecimal.valueOf(expectedAmount)) < 0) {
                LOGGER.warn("❌ Montant insuffisant: {} CRO. Attendu: {} CRO", valueInCro, expectedAmount);
                return false;
            }

            // 5. Vérifier que la transaction est bien un SUCCÈS (minée)
            Optional<TransactionReceipt> receiptCheck = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            
            if (receiptCheck.isEmpty()) {
                LOGGER.warn("❌ Reçu de transaction introuvable (en attente ?). TxHash: {}", txHash);
                return false;
            }
            
            TransactionReceipt receipt = receiptCheck.get();
            if (!receipt.isStatusOK()) {
                LOGGER.warn("❌ La transaction a échoué (Gas error ou Revert). TxHash: {}", txHash);
                return false;
            }

            // TOUT EST BON !
            usedHashes.put(txHash, true);
            LOGGER.info("✅ PAIEMENT VALIDÉ : {} CRO reçus. TxHash: {}", valueInCro, txHash);
            return true;

        } catch (Exception e) {
            LOGGER.error("❌ Erreur technique lors de la vérification Web3j. TxHash: {}", txHash, e);
            return false;
        }
    }
}
