package br.com.isissouzaguimaraes.picpay_desafio_backend.transaction;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
