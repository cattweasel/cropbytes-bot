package net.cattweasel.cropbytes.weasel;

import java.util.List;

public class Portfolio {

	private List<Deposit> deposits;
	private List<Withdrawal> withdrawals;
	private List<FiatTrade> fiatTrades;
	private List<MarketTrade> marketTrades;
	private List<Reward> rewards;
	private List<Snapshot> snapshots;
	
	@Override
	public String toString() {
		return String.format("Portfolio[deposits: %s - withdrawals: %s - fiatTrades: %s - marketTrades: %s - rewards: %s]",
				deposits.size(), withdrawals.size(), fiatTrades.size(), marketTrades.size(), rewards.size());
	}

	public List<Deposit> getDeposits() {
		return deposits;
	}

	public void setDeposits(List<Deposit> deposits) {
		this.deposits = deposits;
	}

	public List<Withdrawal> getWithdrawals() {
		return withdrawals;
	}

	public void setWithdrawals(List<Withdrawal> withdrawals) {
		this.withdrawals = withdrawals;
	}

	public List<FiatTrade> getFiatTrades() {
		return fiatTrades;
	}

	public void setFiatTrades(List<FiatTrade> fiatTrades) {
		this.fiatTrades = fiatTrades;
	}

	public List<MarketTrade> getMarketTrades() {
		return marketTrades;
	}

	public void setMarketTrades(List<MarketTrade> marketTrades) {
		this.marketTrades = marketTrades;
	}

	public List<Reward> getRewards() {
		return rewards;
	}

	public void setRewards(List<Reward> rewards) {
		this.rewards = rewards;
	}

	public List<Snapshot> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(List<Snapshot> snapshots) {
		this.snapshots = snapshots;
	}
}
