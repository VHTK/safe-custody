package com.bourne.Service;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WalletService {

    private static WebSocketService webSocketService;
    private static String walletFilePath;

    static {
        walletFilePath = "/Users/zhangtao/Documents/walletfile";
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://goerli.infura.io/ws/v3/b497e3a7cb6649128a25aa1653fbb70e"));
            boolean includeRawResponses = false;
             webSocketService = new WebSocketService(webSocketClient, includeRawResponses);
            // 注意該程式碼在官方文檔沒說，但必须要加上，否則會出現 WebsocketNotConnectedException。
            webSocketService.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        String walletName = "UTC--2022-11-17T02-37-00.401000000Z--fd116e76b630ea57dbca08170bb52c9f2bfacd72.json";
        String password = "123456";
        String address = getWalletAddress(walletName, password);
        System.out.println(address);
        // LINK
        String contractAddress = "0x326C977E6efc84E512bB9C30f76E30c160eD06FB";
        BigInteger balance = getTokenBalance(address,contractAddress);
        System.out.println(balance);

        // ETH
        System.out.println(getBalance(address));

        String hash = transfer(walletName,password,"0x30635bA975d8e9116b79d8CCd8c6250C05328f3d",BigDecimal.valueOf(0.01D));
        System.out.println(hash);
    }

    public  static String createWallet(String walletName, String password) throws InvalidAlgorithmParameterException, CipherException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        // TODO walletName should save database bind with walletFileName;
        String walletFileName = WalletUtils.generateNewWalletFile(password, new File(walletFilePath), false);
        return walletFileName;
    }

    public static String getWalletAddress(String walletName, String password) throws CipherException, IOException {
        String walleFilePath = walletFilePath + File.separator + walletName;
        Credentials credentials = WalletUtils.loadCredentials(password, walleFilePath);
        String address = credentials.getAddress();
        return address;
    }

    private static  BigInteger getBalance(String address) throws IOException {
        Web3j web3j = Web3j.build(webSocketService);
        DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(web3j.ethBlockNumber().send().getBlockNumber());
        EthGetBalance ethGetBalance = web3j.ethGetBalance(address, defaultBlockParameter).send();
        return ethGetBalance.getBalance();
    }

    public static BigInteger getTokenBalance(String fromAddress, String contractAddress) {
        Web3j web3j = Web3j.build(webSocketService);
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address address = new Address(fromAddress);
        inputParameters.add(address);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);

        EthCall ethCall;
        BigInteger balanceValue = BigInteger.ZERO;
        try {
            DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(web3j.ethBlockNumber().send().getBlockNumber());
            ethCall = web3j.ethCall(transaction, defaultBlockParameter).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            System.out.println(results.toString());
            balanceValue = (BigInteger) results.get(0).getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return balanceValue;
    }

    /**
     * 发送ETH
     * @param walletName
     * @param password
     * @param toAddress
     * @param amount
     * @throws Exception
     */
    public static String transfer(String walletName, String password, String toAddress, BigDecimal amount) throws Exception {
        String walleFilePath = walletFilePath + File.separator + walletName;
        Credentials credentials = WalletUtils.loadCredentials(password, walleFilePath);
        Web3j web3j = Web3j.build(webSocketService);
        TransactionReceipt send = Transfer.sendFunds(web3j, credentials, toAddress, amount, Convert.Unit.ETHER).send();
        log.info("Transaction complete:");
        log.info("trans hash=" + send.getTransactionHash());
        log.info("from :" + send.getFrom());
        log.info("to:" + send.getTo());
        log.info("gas used=" + send.getGasUsed());
        log.info("status: " + send.getStatus());
        return send.getTransactionHash();
    }

    /**
     * 发送ETH
     * @param walletName
     * @param password
     * @param toAddress
     * @param amount
     * @throws Exception
     */
    public static String transferToken(String walletName, String password, String toAddress, BigInteger amount, String contractAddress) throws Exception {
        Web3j web3j = Web3j.build(webSocketService);

        String methodName = "transfer";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        Address tAddress = new Address(toAddress);

        Uint256 value = new Uint256(amount);
        inputParameters.add(tAddress);
        inputParameters.add(value);

        TypeReference<Bool> typeReference = new TypeReference<Bool>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);

        String fromAddress = getWalletAddress(walletName, password);
        EthGetTransactionCount ethGetTransactionCount = web3j
                .ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        BigInteger gasPrice = Convert.toWei(BigDecimal.valueOf(5), Convert.Unit.GWEI).toBigInteger();

        Transaction transaction = Transaction.createFunctionCallTransaction(fromAddress, nonce, gasPrice, BigInteger.valueOf(60000), contractAddress, data);

        EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).sendAsync().get();
        String txHash = ethSendTransaction.getTransactionHash();
        return txHash;
    }
}

