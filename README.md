🚀 Cronos x402 Spring Boot Starter

The first Java Spring Boot Starter enabling programmable x402 payments (HTTP 402) for the Agentic Economy on Cronos.

🏆 Built for the Cronos x402 Paytech Hackathon

📖 The Problem

AI Agents need to consume data and services (APIs), but they don't have credit cards. They have crypto wallets.
Traditional APIs use subscriptions (Stripe/PayPal) which are "Human-centric".
We need "Agent-centric" APIs that accept per-request micropayments securely, instantly, and programmatically.

💡 The Solution

Cronos x402 Starter allows any Java Spring Boot developer to monetize their API endpoints instantly using the HTTP 402 Payment Required standard.

By simply adding one annotation @X402Paywall, your API:

Blocks unpaid requests automatically.

Negotiates payment via standard HTTP 402 headers.

Verifies on-chain transactions on Cronos EVM (Testnet/Mainnet).

Unlocks the resource automatically upon payment validation.

🛠 Installation

This library is hosted on JitPack, making it universally accessible.

Maven

Step 1. Add the JitPack repository to your pom.xml:

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>[https://jitpack.io](https://jitpack.io)</url>
    </repository>
</repositories>


Step 2. Add the dependency:

<dependency>
    <groupId>com.github.adamaniada</groupId>
    <artifactId>cronos-x402-starter</artifactId>
    <version>v1.0.0</version>
</dependency>


Gradle

repositories { 
    mavenCentral()
    maven { url '[https://jitpack.io](https://jitpack.io)' }
}

dependencies {
    implementation 'com.github.adamaniada:cronos-x402-starter:v1.0.0'
}


⚙️ Configuration

Configure your wallet and the blockchain node in src/main/resources/application.properties:

# 1. Enable the x402 Guard
cronos.x402.enabled=true

# 2. Your Wallet Address (Where the CRO payments will be sent)
cronos.x402.receiver-wallet=0xYOUR_WALLET_ADDRESS_HERE

# 3. Cronos RPC Node
# For Hackathon/Dev: Use Testnet
cronos.x402.rpc-url=[https://evm-t3.cronos.org/](https://evm-t3.cronos.org/)
# For Production: Use Mainnet ([https://evm.cronos.org](https://evm.cronos.org))


💻 Usage

1. Protect an Endpoint

Just add the @X402Paywall annotation to any Controller method.

import com.cronos.x402.annotation.X402Paywall;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PremiumDataController {

    @GetMapping("/api/premium/market-data")
    @X402Paywall(amount = 5.0) // Requires 5.0 CRO to access
    public Map<String, String> getMarketAnalysis() {
        // This code is NOT executed unless payment is verified
        return Map.of("analysis", "Bullish on Cronos!");
    }
}


2. Override Destination Wallet

You can specify a different wallet for specific endpoints (useful for marketplaces or multi-tenant apps).

@X402Paywall(amount = 10.0, destinationWallet = "0xAnotherWalletAddress...")
public String getSpecialData() { ... }


🔄 How it Works (The Flow)

The library implements a strict Payment-First protocol:

Client (or AI Agent) requests GET /api/premium.

Starter intercepts request -> Checks for X-Payment-Hash header.

If missing: Returns 402 Payment Required with JSON payment details (Amount, Wallet, Currency).

Client performs the transaction on Cronos Blockchain.

Client retries request with header: X-Payment-Hash: 0xTransactionHash....

Starter connects to Cronos Node (Web3j) and verifies:

Transaction Status (Success)

Receiver Address (Matches API Config)

Amount (Matches Annotation)

Uniqueness (Prevents Replay Attacks)

Access Granted -> Controller logic executes.

🏗 Architecture

Spring Boot 3.x: Auto-configuration & AOP integration.

Web3j: Robust Java integration with Ethereum-compatible nodes (Cronos).

Spring AOP: Non-intrusive interception of API calls.

🤝 Contributing

Contributions are welcome! Please open an issue or submit a pull request for any improvements.

Fork the Project

Create your Feature Branch (git checkout -b feature/AmazingFeature)

Commit your Changes (git commit -m 'Add some AmazingFeature')

Push to the Branch (git push origin feature/AmazingFeature)

Open a Pull Request

📜 License

Distributed under the MIT License. See LICENSE for more information.
