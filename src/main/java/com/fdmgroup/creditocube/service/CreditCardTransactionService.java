package com.fdmgroup.creditocube.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fdmgroup.creditocube.model.Bill;
import com.fdmgroup.creditocube.model.CreditCard;
import com.fdmgroup.creditocube.model.CreditCardTransaction;
import com.fdmgroup.creditocube.model.Customer;
import com.fdmgroup.creditocube.model.InstallmentPayment;
import com.fdmgroup.creditocube.model.Merchant;
import com.fdmgroup.creditocube.repository.CreditCardTransactionRepository;

@Service
public class CreditCardTransactionService {

	private static final Logger LOGGER = LogManager.getLogger(CreditCardTransactionService.class);

	@Autowired
	private CreditCardTransactionRepository repo;

	@Autowired
	private InstallmentPaymentService installmentService;

	@Autowired
	private MerchantService merchantService;

	@Autowired
	private CustomerService customerService;

	public Optional<CreditCardTransaction> createCreditCardTransaction(CreditCardTransaction transaction) {
		Optional<CreditCardTransaction> createdTransaction;

		try {
			createdTransaction = Optional.ofNullable(repo.save(transaction));
		} catch (Exception e) {
			LOGGER.error("Unable to create new card transaction.", e);
			return Optional.ofNullable(null);
		}

		LOGGER.info("Created new card transaction with id: {}", createdTransaction.get().getTransactionId());
		return createdTransaction;
	}

	public List<CreditCardTransaction> findAllCreditCardTransactions(CreditCard card) {
		return repo.findByTransactionCardIsOrderByTransactionDateDesc(card);
	}

	public List<CreditCardTransaction> findAllCreditCardTransactionsForCustomer(Customer customer) {
		List<CreditCardTransaction> transactionList = new ArrayList<>();

		Optional<Customer> optionalCustomer = customerService.findCustomerById(customer.getUser_id());
		if (optionalCustomer.isEmpty()) {
			return transactionList;
		}

		Customer targetCustomer = optionalCustomer.get();
		for (CreditCard card : customer.getCreditCards()) {
			transactionList.addAll(repo.findByTransactionCardIsOrderByTransactionDateDesc(card));
		}

		Comparator<CreditCardTransaction> latestFirst = (transaction1, transaction2) -> {
			return transaction2.getTransactionDate().compareTo(transaction1.getTransactionDate());
		};

		transactionList.sort(latestFirst);

		if (transactionList.size() < 6) {

			return transactionList;
		}

		transactionList.subList(5, transactionList.size()).clear();

		return transactionList;
	}

	public Optional<CreditCardTransaction> updateCreditCardTransaction(CreditCardTransaction transaction) {
		Optional<CreditCardTransaction> updatedTransaction;

		try {
			updatedTransaction = Optional.ofNullable(repo.save(transaction));
		} catch (Exception e) {
			LOGGER.error("Unable to update card transaction.", e);
			return Optional.empty();
		}

		LOGGER.info("Updated card transaction with id: {}", updatedTransaction.get().getTransactionId());
		return updatedTransaction;
	}

	// In case the bill is not paid on time, this function will record the charging
	// of a late payment fees for the credit card
	public void createLatePaymentTransaction(CreditCard card, double latePaymentFees) {
		CreditCardTransaction latePaymentTransaction = new CreditCardTransaction(card, LocalDateTime.now(),
				latePaymentFees, "Late bill payment fees");
		createCreditCardTransaction(latePaymentTransaction);
		LOGGER.info("Added late payment fee transaction to card number: {}", card.getCardNumber());
	}

	public void createInterestFeeTransaction(CreditCard card, double interestPaymentFees) {
		CreditCardTransaction interestFeeTransaction = new CreditCardTransaction(card, LocalDateTime.now(),
				interestPaymentFees, "Interest on bill outstanding amount");
		createCreditCardTransaction(interestFeeTransaction);
		LOGGER.info("Added interest fee on outstanding bill amount transaction to card number: {}",
				card.getCardNumber());
	}

	public void createCashbackTransaction(CreditCard card, double cashback) {
		CreditCardTransaction cashbackTransaction = new CreditCardTransaction(card, LocalDateTime.now(), cashback,
				"Cashback credited on monthly spending");
		Optional<Merchant> optionalMerchant = merchantService.findMerchantByMerchantCode("1");
		if (optionalMerchant.isEmpty()) {
			LOGGER.info("Merchant 1 not found");
			return;
		}
		LOGGER.info("Created cashback transaction for card number: {}", card.getCardNumber());
		cashbackTransaction.setMerchant(optionalMerchant.get());
		createCreditCardTransaction(cashbackTransaction);

	}

	public void createInstallmentPayment(CreditCard card, double transactionAmount) {
		InstallmentPayment installmentPayment = new InstallmentPayment(card, transactionAmount, 6);
		installmentService.createInstallmentPayment(installmentPayment);
		LOGGER.info("Created installment payment of amount {} for card number: {}", transactionAmount, card.getCardNumber());
	}

	public List<CreditCardTransaction> findByTransactionDate(LocalDateTime startDateTime, LocalDateTime endDateTime,
			CreditCard card) {
		return repo.findByTransactionDate(startDateTime, endDateTime, card);
	}

	public List<CreditCardTransaction> findTransactionsByMonth(int month, CreditCard card) {
		return repo.findTransactionsByMonth(month, card);
	}

	public List<CreditCardTransaction> findBillTransactionsBetween(Bill bill, LocalDateTime billingCycleStartTime,
			LocalDateTime billIssueTime) {

		return repo.findByTransactionCardIsAndTransactionDateAfterAndTransactionDateBeforeOrderByTransactionDateDesc(
				bill.getCard(), billingCycleStartTime, billIssueTime);
	}

	public void createCashbackCarryForwardTransaction(CreditCard card, double cashbackCarriedForwardCredited) {
		CreditCardTransaction cashbackTransaction = new CreditCardTransaction(card, LocalDateTime.now(), cashbackCarriedForwardCredited,
				"Cashback carried forward credited to account");
		Optional<Merchant> optionalMerchant = merchantService.findMerchantByMerchantCode("1");
		if (optionalMerchant.isEmpty()) {
			LOGGER.info("Merchant 1 not found");
			return;
		}
		LOGGER.info("Credited cashback carried forward for card number: {}", card.getCardNumber());
		cashbackTransaction.setMerchant(optionalMerchant.get());
		createCreditCardTransaction(cashbackTransaction);
	}

}
