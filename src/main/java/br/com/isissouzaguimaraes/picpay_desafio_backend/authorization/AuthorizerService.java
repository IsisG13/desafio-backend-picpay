package br.com.isissouzaguimaraes.picpay_desafio_backend.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.isissouzaguimaraes.picpay_desafio_backend.transaction.Transaction;

@Service
public class AuthorizerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizerService.class);
    private RestClient RestClient;

    public AuthorizerService(RestClient.Builder builder) {
        this.RestClient = builder
                .baseUrl("https://run.mocky.io/v3/46e0a086-48ab-4d9a-a373-56a53c3f47cd")
                .build();
    }

    public void authorize(Transaction transaction) {
        LOGGER.info("Authorizing transaction: {}", transaction);

        var response = RestClient.get()
                .retrieve()
                .toEntity(Authorization.class);

        if (response.getStatusCode().isError() || !response.getBody().isAuthorized())
            throw new UnauthorizedTransactionException("Unauthorized transaction!");

        LOGGER.info("Transaction authorized: {}", transaction);
    }
}
