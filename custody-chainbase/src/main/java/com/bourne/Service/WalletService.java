package com.bourne.Service;

import lombok.extern.slf4j.Slf4j;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.websocket.WebSocketClient;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class WalletService {

    private static WebSocketService webSocketService;
    private static String walletFilePath;
    private static String emptyAddress;

    static {
        emptyAddress = "0x0000000000000000000000000000000000000000";
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
        BigInteger balance = getTokenBalance(address, contractAddress);
        System.out.println(balance);

        // ETH
        System.out.println(getBalance(address));

       // System.out.println(transfer(walletName, password, "0x30635bA975d8e9116b79d8CCd8c6250C05328f3d", BigDecimal.valueOf(0.001)));

        System.out.println(transferToken(walletName, password, "0x30635bA975d8e9116b79d8CCd8c6250C05328f3d", contractAddress, BigDecimal.valueOf(3.99999999999999899)));
    }

    public static String createWallet(String walletName, String password) throws InvalidAlgorithmParameterException, CipherException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
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

    private static BigInteger getBalance(String address) throws IOException {
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
     * ETH 交易
     *
     * @param walletName
     * @param password
     * @param toAddress
     * @param value
     * @return
     * @throws CipherException
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String transfer(String walletName, String password, String toAddress, BigDecimal value) throws CipherException, IOException, ExecutionException, InterruptedException {
        Web3j web3j = Web3j.build(webSocketService);

        //加载转账所需的凭证，用私钥
        String walleFilePath = walletFilePath + File.separator + walletName;
        Credentials credentials = WalletUtils.loadCredentials(password, walleFilePath);
        String fromAddress = getWalletAddress(walletName, password);
        //获取nonce，交易笔数
        BigInteger nonce = getNonce(web3j, fromAddress);

        // TODO
        //gasPrice和gasLimit 都可以手动设置
        BigInteger gasPrice = getGasPrice(web3j);
        if (gasPrice == null) {
            log.error("GetGasPriceError from:[{}] to:[{}] amount:[{}]", fromAddress, toAddress, value.toString());
            return null;
        }
        //BigInteger.valueOf(4300000L) 如果交易失败 很可能是手续费的设置问题
        BigInteger gasLimit = BigInteger.valueOf(60000L);

        //创建RawTransaction交易对象
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, toAddress, Convert.toWei(value, Convert.Unit.ETHER).toBigIntegerExact());
        //签名Transaction，这里要对交易做签名
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发送交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        return ethSendTransaction.getTransactionHash();
    }

    /**
     * ERC20 交易
     *
     * @param walletName
     * @param password
     * @param toAddress
     * @param contractAddress
     * @param value
     * @return
     */
    public static String transferToken(String walletName, String password, String toAddress, String contractAddress, BigDecimal value) throws CipherException, IOException, ExecutionException, InterruptedException {
        Web3j web3j = Web3j.build(webSocketService);

        value = value.multiply(BigDecimal.TEN.pow(getTokenDecimals(web3j, contractAddress)));

        //加载转账所需的凭证，用私钥
        String walleFilePath = walletFilePath + File.separator + walletName;
        Credentials credentials = WalletUtils.loadCredentials(password, walleFilePath);

        String fromAddress = getWalletAddress(walletName, password);
        //获取nonce，交易笔数
        BigInteger nonce = getNonce(web3j, fromAddress);
        if (nonce == null) {
            log.error("getNonceError from:[{}] to:[{}] amount:[{}] contract [{}]", fromAddress, toAddress, value, contractAddress);
            return null;
        }
        // TODO
        //gasPrice和gasLimit 都可以手动设置
        BigInteger gasPrice = getGasPrice(web3j);
        if (gasPrice == null) {
            log.error("getGasPriceError from:[{}] to:[{}] amount:[{}] contract [{}]", fromAddress, toAddress, value, contractAddress);
            return null;
        }
        BigInteger.valueOf(6000L);
        //如果交易失败 很可能是手续费的设置问题
        BigInteger gasLimit = BigInteger.valueOf(4300000L);
        //ERC20代币合约方法
        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(value.toBigIntegerExact())),
                Collections.singletonList(new TypeReference<Type>() {
                }));
        //创建RawTransaction交易对象
        String encodedFunction = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                contractAddress, encodedFunction);

        //签名Transaction
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发送交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String hash = ethSendTransaction.getTransactionHash();
        return hash;
    }

    /**
     * 获取gas-price
     *
     * @param web3j
     * @return
     */
    private static BigInteger getGasPrice(Web3j web3j) {
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            if (ethGasPrice == null) {
                log.error("GetGasPriceError");
                return null;
            }
            return ethGasPrice.getGasPrice();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }

    /**
     * 获取nonce
     *
     * @param web3j
     * @param address
     * @return
     */
    private static BigInteger getNonce(Web3j web3j, String address) {
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
            if (ethGetTransactionCount == null) {
                log.error("GetNonceError:" + address);
                return null;
            }
            return ethGetTransactionCount.getTransactionCount();
        } catch (Throwable t) {
            log.error("GetNonceError:" + address);
        }
        return null;
    }

    /**
     * 查询代币精度
     *
     * @param web3j
     * @param contractAddress
     * @return
     */
    public static int getTokenDecimals(Web3j web3j, String contractAddress) {
        String methodName = "decimals";
        String fromAddr = emptyAddress;
        int decimal = 0;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            decimal = Integer.parseInt(results.get(0).getValue().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return decimal;
    }
}

