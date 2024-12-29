package br.com.isissouzaguimaraes.picpay_desafio_backend.authorization;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.isissouzaguimaraes.picpay_desafio_backend.transaction.Transaction;

@Service
public class AuthorizerService {
    private RestClient RestClient;

    public AuthorizerService(RestClient.Builder builder) {
        this.RestClient = builder
                .baseUrl("https://run.mocky.io/v3/46e0a086-48ab-4d9a-a373-56a53c3f47cd")
                .build();
    }

    public void authorize(Transaction transaction) {
        var response = RestClient.get()
                .retrieve()
                .toEntity(Authorization.class);

        if (response.getStatusCode().isError() || !response.getBody().isAuthorized())
            throw new UnauthorizedTransactionException("Unauthorized transaction!");
    }
}
