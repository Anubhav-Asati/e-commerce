package com.example.majorpoject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Service
public class TransactionService {

    private static final String TRANSACTION_CREATE_TOPIC = "transaction_create";
    private static final String WALLET_UPDATE_TOPIC = "wallet_update";

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public String doTransaction(TransactionRequest transactionRequest) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .transactionStatus(TransactionStatus.PENDING)
                .amount(transactionRequest.getAmount())
                .sender(transactionRequest.getSender())
                .receiver(transactionRequest.getReceiver())
                .build();

        transactionRepository.save(transaction);

        JSONObject transactionCreate = new JSONObject();
        transactionCreate.put("transactionId",transaction.getTransactionId());
        transactionCreate.put("sender",transaction.getSender());
        transactionCreate.put("receiver",transaction.getReceiver());
        transactionCreate.put("amount",transaction.getAmount());

        kafkaTemplate.send(TRANSACTION_CREATE_TOPIC,objectMapper.writeValueAsString(transactionCreate));

        return transaction.getTransactionId();

    }

    @KafkaListener(topics = {WALLET_UPDATE_TOPIC}, groupId = "example")
    public void updateTransaction(String msg) throws JsonProcessingException {
        JSONObject walletUpdate = objectMapper.readValue(msg,JSONObject.class);
        String transactionId = (String) walletUpdate.get("transactionId");
        String status = (String) walletUpdate.get("status");

        transactionRepository.updateTransaction(transactionId,status);

    }



}
