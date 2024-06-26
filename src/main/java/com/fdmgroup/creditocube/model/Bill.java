package com.fdmgroup.creditocube.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Bill {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne
	@JoinColumn(name = "fk_card_id")
	private CreditCard card;
	
	private LocalDateTime previousBillIssueTime;
	
	private LocalDateTime billIssueTime;

	private double totalAmountDue;

	private double minimumAmountDue;

	private double outstandingAmount;
	
	private double previousBillOutstandingAmount;
	
	private double monthlySpend;
	
	private double cashbackEarned;

	// private LocalDateTime dueDate;

	public double getMonthlySpend() {
		return monthlySpend;
	}

	public void setMonthlySpend(double monthlySpend) {
		this.monthlySpend = new BigDecimal(monthlySpend).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public double getCashbackEarned() {
		return cashbackEarned;
	}

	public void setCashbackEarned(double cashbackEarned) {
		this.cashbackEarned = new BigDecimal(cashbackEarned).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	private boolean paid;

	public Bill() {

	}

	public Bill(CreditCard card, double totalAmountDue, double minimumAmountDue, boolean paid) {
		this.card = card;
		this.totalAmountDue = totalAmountDue;
		this.minimumAmountDue = minimumAmountDue;
		this.outstandingAmount = totalAmountDue;
		this.paid = paid;
	}

	public double getTotalAmountDue() {
		return totalAmountDue;
	}

	public void setTotalAmountDue(double totalAmountDue) {
		this.totalAmountDue = new BigDecimal(totalAmountDue).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public double getMinimumAmountDue() {
		return minimumAmountDue;
	}

	public void setMinimumAmountDue(double minimumAmountDue) {
		this.minimumAmountDue = new BigDecimal(minimumAmountDue).setScale(2, RoundingMode.HALF_UP).doubleValue();;
	}

	public boolean isPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public CreditCard getCard() {
		return card;
	}

	public void setCard(CreditCard card) {
		this.card = card;
	}

	public double getOutstandingAmount() {
		return outstandingAmount;
	}

	public void setOutstandingAmount(double outstandingAmount) {
		this.outstandingAmount = new BigDecimal(outstandingAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();;
	}

	public LocalDateTime getBillIssueTime() {
		return billIssueTime;
	}

	public void setBillIssueTime(LocalDateTime billIssueTime) {
		this.billIssueTime = billIssueTime;
	}

	public LocalDateTime getPreviousBillIssueTime() {
		return previousBillIssueTime;
	}

	public void setPreviousBillIssueTime(LocalDateTime previousBillIssueTime) {
		this.previousBillIssueTime = previousBillIssueTime;
	}

	public double getPreviousBillOutstandingAmount() {
		return previousBillOutstandingAmount;
	}

	public void setPreviousBillOutstandingAmount(double previousBillOutstandingAmount) {
		this.previousBillOutstandingAmount = new BigDecimal(previousBillOutstandingAmount).setScale(2, RoundingMode.HALF_UP).doubleValue();;
	}
	
	public String getFormattedDate(LocalDateTime dateTime) {
		String date = dateTime.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"));	//ofLocalizedDateTime( new FormatStyle(dateStyle, timeStyle)));
//				+ transactionDate.format(DateTimeFormatter.ISO_TIME);
		return date;
	}
	
	
}
