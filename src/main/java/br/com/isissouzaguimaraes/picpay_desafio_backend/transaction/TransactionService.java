package br.com.isissouzaguimaraes.picpay_desafio_backend.transaction;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.isissouzaguimaraes.picpay_desafio_backend.authorization.AuthorizerService;
import br.com.isissouzaguimaraes.picpay_desafio_backend.notification.NotificationService;
import br.com.isissouzaguimaraes.picpay_desafio_backend.wallet.Wallet;
import br.com.isissouzaguimaraes.picpay_desafio_backend.wallet.WalletRepository;
import br.com.isissouzaguimaraes.picpay_desafio_backend.wallet.WalletType;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final AuthorizerService authorizerService;
    private final NotificationService notificationService;

    public TransactionService(TransactionRepository transactionRepository, WalletRepository walletRepository,
            AuthorizerService authorizerService, NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.authorizerService = authorizerService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Transaction create(Transaction transaction) {
        // 1- validar
        validate(transaction);

        // 2- criar a transição
        var newTransaction = transactionRepository.save(transaction);

        // 3- debitar da carteira
        var walletPayer = walletRepository.findById(transaction.payer()).get();
        var walletPayee = walletRepository.findById(transaction.payee()).get();
        walletRepository.save(walletPayer.debit(transaction.value()));
        walletRepository.save(walletPayee.credit(transaction.value()));

        // 4- chamar serviços externos
        // authorize transaction
        authorizerService.authorize(transaction);

        // notificação
        notificationService.notify(transaction);

        return newTransaction;
    }

    private void validate(Transaction transaction) {
        walletRepository.findById(transaction.payee())
                .map(payee -> walletRepository.findById(transaction.payer())
                        .map(payer -> IsTransnactionValid(transaction, payer) ? transaction : null)
                        .orElseThrow(() -> new InvalidTransactionException(
                                "Invalid transaction -%s".formatted(transaction))))
                .orElseThrow(() -> new InvalidTransactionException("Invalid transaction -%s".formatted(transaction)));
    }

    private boolean IsTransnactionValid(Transaction transaction, Wallet payer) {
        return payer.type() == WalletType.COMUM.getValue() &&
                payer.balance().compareTo(transaction.value()) >= 0 &&
                !payer.id().equals(transaction.payee());
    }

    public List<Transaction> list() {
        return transactionRepository.findAll();
    }
}
