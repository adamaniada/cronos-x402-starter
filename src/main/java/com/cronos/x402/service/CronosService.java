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
    // Pour le hackathon, on stocke les hashs utilisés en mémoire pour éviter le "Replay Attack"
    private final ConcurrentHashMap<String, Boolean> usedHashes = new ConcurrentHashMap<>();

    public CronosService(String rpcUrl) {
        this.web3j = Web3j.build(new HttpService(rpcUrl));
    }

    public boolean verifyTransaction(String txHash, double expectedAmount, String expectedReceiver) {
        if (usedHashes.containsKey(txHash)) {
            LOGGER.warn("❌ Ce hash a déjà été utilisé ! TxHash: {}", txHash);
            return false;
        }

        try {
            // 1. Récupérer la transaction
            Optional<Transaction> txOptional = web3j.ethGetTransactionByHash(txHash).send().getTransaction();
            if (txOptional.isEmpty()) {
                LOGGER.warn("❌ Transaction non trouvée. TxHash: {}", txHash);
                return false;
            }

            Transaction tx = txOptional.get();

            // 2. Vérifier le destinataire
            if (!tx.getTo().equalsIgnoreCase(expectedReceiver)) {
                LOGGER.warn("❌ Mauvais destinataire. Attendu: {}, Reçu: {}, TxHash: {}", 
                    expectedReceiver, tx.getTo(), txHash);
                return false;
            }

            // 3. Vérifier le montant (Conversion Wei -> CRO)
            BigDecimal valueInCro = Convert.fromWei(new BigDecimal(tx.getValue()), Convert.Unit.ETHER);
            if (valueInCro.doubleValue() < expectedAmount) {
                LOGGER.warn("❌ Montant insuffisant: {}. Attendu: {}, TxHash: {}", 
                    valueInCro, expectedAmount, txHash);
                return false;
            }

            // 4. Vérifier que la transaction est bien minée (Succès)
            Optional<TransactionReceipt> receipt = web3j.ethGetTransactionReceipt(txHash).send().getTransactionReceipt();
            if (receipt.isEmpty() || !receipt.get().isStatusOK()) {
                LOGGER.warn("❌ Transaction échouée ou en attente. TxHash: {}", txHash);
                return false;
            }

            // Tout est bon, on marque le hash comme utilisé
            usedHashes.put(txHash, true);
            LOGGER.info("✅ Transaction vérifiée avec succès. TxHash: {}, Montant: {}, Destinataire: {}", 
                txHash, valueInCro, expectedReceiver);
            return true;

        } catch (Exception e) {
            LOGGER.error("❌ Erreur lors de la vérification de la transaction. TxHash: {}", txHash, e);
            return false;
        }
    }
}
