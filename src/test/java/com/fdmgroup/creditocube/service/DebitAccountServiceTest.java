package com.fdmgroup.creditocube.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fdmgroup.creditocube.model.Customer;
import com.fdmgroup.creditocube.model.DebitAccount;
import com.fdmgroup.creditocube.repository.CustomerRepository;
import com.fdmgroup.creditocube.repository.DebitAccountRepository;

@ExtendWith(MockitoExtension.class)
public class DebitAccountServiceTest {

	@Mock
	private DebitAccountRepository debitAccountRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private DebitAccountService debitAccountService;

	DebitAccount account;
	Optional<DebitAccount> optionalAccount;
	Customer customer;
	Optional<Customer> optionalCustomer;

	@BeforeEach
	public void init() {
		debitAccountService = new DebitAccountService(debitAccountRepository, customerRepository);
		account = new DebitAccount();
		account.setAccountId(123456);
		customer = new Customer();
		customer.setUser_id(1);
		account.setCustomer(customer);
	}

	@Test
	@DisplayName("1. Test that createAccount does not create a new account if an existing account is present")
	public void testCreateAccount_AccountExists() {

		// Arrange
		optionalAccount = Optional.of(account);
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);

		// Act
		debitAccountService.createAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(customerRepository, never()).findById(1);

		// Explanation: This test case simulates a scenario where an account with the
		// provided account number already exists in the database.
		// The service should check for existing accounts first and prevent duplicate
		// accounts from being created.
		// We verify that only the findByAccountNumber method from
		// debitAccountRepository is called,
		// and no interactions occur with the customerRepository since the account
		// already exists.
	}

	@Test
	@DisplayName("2. Test that createAccount does not create a new account if customer does not exist")
	public void testCreateAccount_CustomerNotFound() {

		// Arrange
		optionalAccount = Optional.empty();
		optionalCustomer = Optional.empty();
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		debitAccountService.createAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(customerRepository).findById(customer.getUser_id());
		Mockito.verify(debitAccountRepository, never()).save(account);

		// Explanation: This test case simulates a scenario where the provided account
		// number doesn't correspond to an existing account,
		// but the customer ID linked to the account is not found in the customer
		// repository.
		// The service should check for existing accounts first and then find the
		// customer by ID.
		// We verify that both findByAccountNumber and findById methods are called from
		// their respective repositories,
		// but no save methods are called since the customer is not found.
	}

	@Test
	@DisplayName("3. Test that createAccount does not create a new account if max accounts are reached")
	public void testCreateAccount_MaxAccountsReached() {
		// Arrange
		List<DebitAccount> accountList = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			accountList.add(new DebitAccount());
		}
		customer.setDebitAccounts(accountList);
		optionalAccount = Optional.empty();
		optionalCustomer = Optional.of(customer);
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		debitAccountService.createAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(customerRepository).findById(customer.getUser_id());
		Mockito.verify(debitAccountRepository, never()).save(account);

		// Explanation: This test case simulates a scenario where the customer has
		// already reached the maximum allowed number of debit accounts (5).
		// The service should check the customer's existing accounts before creating a
		// new one.
		// We verify that both findByAccountNumber and findById methods are called,
		// but no save methods are called since the customer cannot have more than 5
		// accounts.
	}

	@Test
	@DisplayName("4. Test that updateAccount updates existing account")
	public void testUpdateAccount_AccountExists() {
		// Arrange
		optionalAccount = Optional.of(account);
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);

		// Act
		debitAccountService.updateAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository, Mockito.times(1)).findById(account.getAccountId());
		Mockito.verify(debitAccountRepository, Mockito.times(1)).save(account);

		// Explanation: This test case simulates a scenario where an account with the
		// provided account number exists in the database.
		// The service should find the account by number and then persist the updated
		// account details.
		// We verify that both findByAccountNumber and save methods are called from the
		// debitAccountRepository.
	}

	@Test
	@DisplayName("5. Test that updateAccount does not update when account is not found")
	public void testUpdateAccount_AccountNotFound() {
		// Arrange
		optionalAccount = Optional.empty();
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);

		// Act
		debitAccountService.updateAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(debitAccountRepository, never()).save(account);

		// Explanation: This test case simulates a scenario where the provided account
		// number doesn't correspond to an existing account.
		// The service should check for the account by number before attempting to
		// update.
		// We verify that only the findByAccountNumber method is called, and no save
		// methods are called since the account is not found.
	}

	@Test
	@DisplayName("6. Test that closeDebitAccount closes an existing account with zero balance")
	public void testCloseDebitAccount_AccountExists_BalanceZero() {
		// Arrange
		account.setAccountBalance(0.0);
		List<DebitAccount> accountList = new ArrayList<>();
		accountList.add(account);
		customer.setDebitAccounts(accountList);
		optionalAccount = Optional.of(account);
		optionalCustomer = Optional.of(customer);

		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		debitAccountService.closeDebitAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(customerRepository).findById(customer.getUser_id());
		Mockito.verify(debitAccountRepository).delete(account);
		Mockito.verify(customerRepository).save(customer);

		// Explanation: This test case simulates a successful close account scenario.
		// The account exists, the balance is zero, and the customer information is
		// found.
		// We verify that all necessary methods are called:
		// - findByAccountNumber to find the account
		// - findById to find the customer
		// - delete to remove the account from the database
		// - save to update the customer with the removed account
	}

	@Test
	@DisplayName("7. Test that closeDebitAccount does not close existing account with non-zero balance")
	public void testCloseDebitAccount_AccountExists_BalanceNotZero() {
		// Mock data

		account.setAccountBalance(100.0);
		optionalAccount = Optional.of(account);
		optionalCustomer = Optional.of(customer);

		// Mock behavior
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Call method
		debitAccountService.closeDebitAccount(account);

		// Verify interaction
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(customerRepository).findById(customer.getUser_id());
		Mockito.verify(debitAccountRepository, never()).delete(account);
		Mockito.verify(customerRepository, never()).save(customer);

		// Explanation: This test case simulates a scenario where the account exists but
		// the balance is not zero.
		// The close account should not proceed, and no updates should be made.
		// We verify that the findByAccountNumber and findById methods are called to
		// find the account and customer,
		// but no delete or save methods are called since the balance check fails.
	}

	@Test
	@DisplayName("8. Test that closeDebitAccount does not close an account which does not exist")
	public void testCloseDebitAccount_AccountNotFound() {
		// Arrange
		optionalAccount = Optional.empty();
		Mockito.when(debitAccountRepository.findById(account.getAccountId())).thenReturn(optionalAccount);

		// Act
		debitAccountService.closeDebitAccount(account);

		// Assert
		Mockito.verify(debitAccountRepository).findById(account.getAccountId());
		Mockito.verify(debitAccountRepository, never()).delete(account);

		// Explanation: This test case simulates a scenario where the provided account
		// number doesn't correspond to an existing account.
		// The close account should not proceed since no account is found.
		// We verify that only the findByAccountNumber method is called to check for the
		// account.
	}

	@Test
	@DisplayName("9. Test that findDebitAccountByAccountNumber returns the account with the right number")
	public void testFindDebitAccountByAccountNumber_AccountExists() {
		// Arrange

		String accountNumber = "123456";
		DebitAccount account = new DebitAccount();
		account.setAccountNumber(accountNumber);
		Optional<DebitAccount> optionalAccount = Optional.of(account);
		Mockito.when(debitAccountRepository.findByAccountNumber(accountNumber)).thenReturn(optionalAccount);

		// Act
		Optional<DebitAccount> returnedAccount = debitAccountService.findDebitAccountByAccountNumber(accountNumber);

		// Assert
		Mockito.verify(debitAccountRepository).findByAccountNumber(accountNumber);
		assertEquals(optionalAccount, returnedAccount);

		// Explanation: This test case simulates a scenario where an account with the
		// provided account number exists in the database.
		// The service should call the findByAccountNumber method from the
		// debitAccountRepository.
		// We verify that the method is called with the correct argument and the
		// returned Optional object is the same as the one mocked.
	}

	@Test
	@DisplayName("10. Test that findDebitAccountByAccountNumber does not return an account that does not exist")
	public void testFindDebitAccountByAccountNumber_AccountNotFound() {
		// Arrange
		String accountNumber = "123456";
		Optional<DebitAccount> optionalAccount = Optional.empty();
		Mockito.when(debitAccountRepository.findByAccountNumber(accountNumber)).thenReturn(optionalAccount);

		// Act
		Optional<DebitAccount> returnedAccount = debitAccountService.findDebitAccountByAccountNumber(accountNumber);

		// Assert
		Mockito.verify(debitAccountRepository).findByAccountNumber(accountNumber);
		assertEquals(Optional.empty(), returnedAccount);

		// Explanation: This test case simulates a scenario where no account with the
		// provided account number exists in the database.
		// The service should call the findByAccountNumber method from the
		// debitAccountRepository.
		// We verify that the method is called with the correct argument and an empty
		// Optional object is returned.
	}

	@Test
	@DisplayName("11. Test that findAllDebitAccountsForCustomer returns all customer accounts of an existing customer")
	public void testFindAllDebitAccountsForCustomer_CustomerExists_AccountsFound() {
		// Arrange
		Customer customer = new Customer();
		customer.setUser_id(1);
		List<DebitAccount> allAccounts = new ArrayList<>();
		DebitAccount account1 = new DebitAccount();
		account1.setAccountNumber("123456");
		account1.setCustomer(customer);
		DebitAccount account2 = new DebitAccount();
		account2.setAccountNumber("654321");
		account2.setCustomer(customer);
		allAccounts.add(account1);
		allAccounts.add(account2);
		List<DebitAccount> expectedAccounts = new ArrayList<>();
		expectedAccounts.add(account1);
		expectedAccounts.add(account2);
		Optional<Customer> optionalCustomer = Optional.of(customer);

		Mockito.when(debitAccountRepository.findAll()).thenReturn(allAccounts);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		List<DebitAccount> returnedAccounts = debitAccountService.findAllDebitAccountsForCustomer(customer);

		// Assert
		Mockito.verify(debitAccountRepository).findAll();
		Mockito.verify(customerRepository).findById(customer.getUser_id());
		assertEquals(expectedAccounts, returnedAccounts);

		// Explanation: This test simulates a scenario where the customer exists and has
		// multiple accounts in the allAccounts list.
		// The service should find all accounts, filter them based on the customer, and
		// return the matching accounts.
		// We verify that both repositories are called with their respective methods.
		// We also assert that the returned list contains the expected accounts
		// (account1 and account2).
	}

	@Test
	@DisplayName("12. Test that findAllDebitAccountsForCustomer returns no accounts if a customer does not have an account")
	public void testFindAllDebitAccountsForCustomer_CustomerExists_NoAccountsFound() {

		// Arrange
		List<DebitAccount> allAccounts = new ArrayList<>();
		DebitAccount account1 = new DebitAccount();
		account1.setAccountNumber("123456");
		account1.setCustomer(new Customer()); // Different customer
		DebitAccount account2 = new DebitAccount();
		account2.setAccountNumber("654321");
		account2.setCustomer(new Customer()); // Different customer
		allAccounts.add(account1);
		allAccounts.add(account2);
		optionalCustomer = Optional.of(customer);

		Mockito.when(debitAccountRepository.findAll()).thenReturn(allAccounts);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		List<DebitAccount> returnedAccounts = debitAccountService.findAllDebitAccountsForCustomer(customer);

		// Assert
		Mockito.verify(debitAccountRepository).findAll();
		Mockito.verify(customerRepository).findById(customer.getUser_id());

		assertEquals(new ArrayList<>(), returnedAccounts);

		// Explanation: This test simulates a scenario where the customer exists but
		// none of the accounts in the allAccounts list belong to that customer.
		// The service should find all accounts but filter out any that don't match the
		// customer.
		// We verify that both repositories are called with their respective methods.
		// We also assert that the returned list is empty since no matching accounts
		// were found.
	}

	@Test
	@DisplayName("13. Test that findAllDebitAccountsForCustomer returns no accounts if a customer is not found")
	public void testFindAllDebitAccountsForCustomer_CustomerNotFound() {
		// Arrange
		List<DebitAccount> allAccounts = new ArrayList<>();
		optionalCustomer = Optional.empty();

		Mockito.when(debitAccountRepository.findAll()).thenReturn(allAccounts);
		Mockito.when(customerRepository.findById(customer.getUser_id())).thenReturn(optionalCustomer);

		// Act
		List<DebitAccount> returnedAccounts = debitAccountService.findAllDebitAccountsForCustomer(customer);

		// Assert
		Mockito.verify(debitAccountRepository, Mockito.times(1)).findAll();
		Mockito.verify(customerRepository, Mockito.times(1)).findById(customer.getUser_id());
		assertEquals(allAccounts, returnedAccounts);
	}

}
