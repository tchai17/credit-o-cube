package com.fdmgroup.creditocube.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.fdmgroup.creditocube.model.Customer;
import com.fdmgroup.creditocube.model.DebitAccount;
import com.fdmgroup.creditocube.service.CustomerService;
import com.fdmgroup.creditocube.service.DebitAccountService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Web Controller for managing debit account-related requests
 *
 * @author timothy.chai
 */
@Controller
public class DebitAccountController {

	@Autowired
	private DebitAccountService debitAccountService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private HttpSession session;

	@GetMapping("/account-dashboard")
	public String goToAccountDashboard(Principal principal) {
		// Find the user associated with the provided customer ID.
		Optional<Customer> optionalCustomer = customerService.findCustomerByUsername(principal.getName());

		// If the user is not found, redirect to the login page.
		if (optionalCustomer.isEmpty()) {
			return "redirect:/login";
		}
		Customer sessionCustomer = optionalCustomer.get();
		session.setAttribute("customer", sessionCustomer);

		List<DebitAccount> accounts = debitAccountService.findAllDebitAccountsForCustomer(sessionCustomer);

		session.setAttribute("accounts", accounts);

		return ("account-dashboard");
	}

	@GetMapping("/createDebitAccount")
	public String goToCreateDebitAccountPage() {
		return ("createDebitAccount");
	}

	/**
	 * Creates a new debit account for the authenticated customer.
	 *
	 * @param customer The authenticated customer object.
	 * @return A redirect to the dashboard page after creating the new debit
	 *         account.
	 */
	@PostMapping("/createDebitAccount")
	public String createDebitAccount(Principal principal, HttpServletRequest request) {

		// Get request parameters
		String accountName = request.getParameter("account-name");
		String stringBalance = request.getParameter("account-balance");
		double balance = Double.parseDouble(stringBalance);

		// Find the user associated with the provided customer ID.
		Optional<Customer> optionalCustomer = customerService.findCustomerByUsername(principal.getName());

		// If the user is not found, redirect to the login page.
		if (optionalCustomer.isEmpty()) {
			return "redirect:/login";
		}

		// Get the authenticated customer session object.
		Customer sessionCustomer = optionalCustomer.get();

		// Create a new debit account for the customer.
		DebitAccount newAccount = new DebitAccount(sessionCustomer);
		newAccount.setAccountName(accountName);
		newAccount.setAccountBalance(balance);

		debitAccountService.createAccount(newAccount);

		// Return a redirect to the dashboard page.
		return "redirect:/account-dashboard";
	}

	/**
	 * Closes a debit account for the authenticated customer.
	 *
	 * @param customer The authenticated customer object.
	 * @param account  The debit account to be closed.
	 * @return A redirect to the dashboard page after closing the debit account.
	 */
	@PostMapping("/deleteDebitAccount")
	public String closeDebitAccount(@SessionAttribute Customer customer, @RequestParam int accountNumber) {

		// Find the user associated with the provided customer ID.
		Optional<Customer> optionalCustomer = customerService.findCustomerById(customer.getUser_id());

		// If the user is not found, redirect to the login page.
		if (optionalCustomer.isEmpty()) {
			return "redirect:/login";
		}

		// Find the debit account associated with the provided account number.
		Optional<DebitAccount> optionalDebitAccount = debitAccountService
				.findDebitAccountByAccountNumber(accountNumber);

		// If the debit account is not found, redirect to the login page.
		if (optionalDebitAccount.isEmpty()) {
			return "redirect:/login";
		}

		DebitAccount targetDebitAccount = optionalDebitAccount.get();

		debitAccountService.closeDebitAccount(targetDebitAccount);

		// Return a redirect to the dashboard page.
		return "redirect:/account-dashboard";
	}

	@GetMapping("/deposit-withdraw")
	public String goToDepositWithdrawPage(@RequestParam int accountNumber) {
		return ("deposit-withdraw");
	}

	@PostMapping("/deposit-into-account")
	public String depositIntoAccount(@SessionAttribute Customer customer, @RequestParam int accountNumber,
			@RequestParam double amount) {

		// Return a redirect to the dashboard page.
		return "redirect:/account-dashboard";
	}

}
