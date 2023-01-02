package net.cattweasel.cropbytes.weasel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.tools.HibernateUtil;

public class WeaselSchnipsel {

	private static final Logger LOG = Logger.getLogger(WeaselSchnipsel.class);
	
	private Session session;
	
	@Before
	public void setUp() {
		session = HibernateUtil.openSession();
	}
	
	@After
	public void tearDown() {
		session.close();
	}
	
	@Test
	public void test() throws Exception {
		
		Portfolio portfolio = createPortfolio();
		
		System.out.println("** debug: portfolio: " + portfolio); // TODO
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date timestamp = sdf.parse("2022-07-09"); // TODO -> must be first deposit date
		Snapshot snapshot = null;
		while (timestamp.before(new Date())) {
			
			System.out.println(timestamp); // TODO
			
			/****************************************************************/
			
			Date future = new Date(timestamp.getTime() + 86400000L);
			
			Snapshot snap = new Snapshot(snapshot);
			snap.setPortfolio(portfolio);
			snap.setTimestamp(timestamp);
			
			Map<String, Double> currencies = snap.getCurrencies();
			
			for (Deposit deposit : portfolio.getDeposits()) {
				if (deposit.getTimestamp().after(timestamp) && deposit.getTimestamp().before(future)) {
					//System.out.println(deposit); // TODO
					
					Double current = currencies.get(deposit.getCurrency().getCode());
					current = current == null ? deposit.getAmount() : current + deposit.getAmount();
					currencies.put(deposit.getCurrency().getCode(), current);
				}
			}
			
			System.out.println("** debug: currencies: " + currencies); // TODO
			
			for (FiatTrade trade : portfolio.getFiatTrades()) {
				if (trade.getTimestamp().after(timestamp) && trade.getTimestamp().before(future)) {
					
					System.out.println("** debug: fiat trade: " + trade); // TODO
					
					Double current = currencies.get(trade.getBaseCurrency().getCode());
					current -= trade.getTotal();
					currencies.put(trade.getBaseCurrency().getCode(), current);
					
					current = currencies.get(trade.getTargetCurrency().getCode());
					current = current == null ? trade.getAmount() : current + trade.getAmount();
					currencies.put(trade.getTargetCurrency().getCode(), current);
				}
			}
			
			for (Withdrawal withdrawal : portfolio.getWithdrawals()) {
				if (withdrawal.getTimestamp().after(timestamp) && withdrawal.getTimestamp().before(future)) {
					System.out.println(withdrawal); // TODO
					
					Double current = currencies.get(withdrawal.getCurrency().getCode());
					current -= withdrawal.getAmount();
					currencies.put(withdrawal.getCurrency().getCode(), current);
				}
			}
			
			timestamp = new Date(timestamp.getTime() + 86400000L);
			snap.setCurrencies(currencies);
			snapshot = snap;
			
			/****************************************************************/
			
			System.out.println("** debug: snapshot: " + snap); // TODO
			
		}
		
	}

	private Portfolio createPortfolio() {
		Portfolio portfolio = new Portfolio();
		portfolio.setDeposits(parseDeposits(portfolio, "C:\\Users\\bwesp\\Documents\\cb_history\\deposit_history.csv"));
		portfolio.setFiatTrades(parseFiatTrades(portfolio, "C:\\Users\\bwesp\\Documents\\cb_history\\trade_history.csv"));
		portfolio.setMarketTrades(parseMarketTrades(portfolio, "C:\\Users\\bwesp\\Documents\\cb_history\\trade_history.csv"));
		portfolio.setRewards(parseRewards(portfolio, "C:\\Users\\bwesp\\Documents\\cb_history\\reward_history.csv"));
		portfolio.setWithdrawals(parseWithdrawals(portfolio, "C:\\Users\\bwesp\\Documents\\cb_history\\withdrawal_history.csv"));
		return portfolio;
	}

	private List<Withdrawal> parseWithdrawals(Portfolio portfolio, String filePath) {
		List<Withdrawal> result = new ArrayList<Withdrawal>();
		JSONArray json = new JSONArray(readFile(filePath));
		for (int i=0; i<json.length(); i++) {
			JSONObject obj = (JSONObject) json.get(i);
			String state = obj.getString("state");
			if ("succeed".equals(state)) {Withdrawal w = new Withdrawal();
				w.setAmount(obj.getDouble("amount"));
				w.setCurrency(session.get(Currency.class, obj.getString("currency").toUpperCase()));
				w.setFees(obj.getDouble("fee"));
				w.setId(obj.getLong("id"));
				w.setPortfolio(portfolio);
				w.setTimestamp(parseTimestamp(obj.getString("done_at")));
				result.add(w);
			} else if (!"canceled".equals(state)) {
				LOG.warn("Skipping withdrawal with state: " + state);
			}
		}
		return result;
	}

	private List<Reward> parseRewards(Portfolio portfolio, String filePath) {
		List<Reward> result = new ArrayList<Reward>();
		JSONObject json = new JSONObject(readFile(filePath));
		JSONObject data = json.getJSONObject("data");
		JSONArray rewards = data.getJSONArray("rewards");
		for (int i=0; i<rewards.length(); i++) {
			JSONObject obj = rewards.getJSONObject(i);
			String status = obj.getString("status");
			if ("unlocked".equals(status)) {
				Reward r = new Reward();
				r.setAsset(session.get(Asset.class, obj.getString("rewardAsset").toUpperCase()));
				r.setId(obj.getLong("id"));
				r.setPortfolio(portfolio);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					r.setTimestamp(sdf.parse(obj.getString("unlockedAt")));
				} catch (ParseException ex) {
					LOG.error(ex);
				}
				result.add(r);
			} else if (!"expired".equals(status)) {
				LOG.warn("Skipping reward with status: " + status);
			}
		}
		return result;
	}

	private List<MarketTrade> parseMarketTrades(Portfolio portfolio, String filePath) {
		List<MarketTrade> result = new ArrayList<MarketTrade>();
		JSONArray json = new JSONArray(readFile(filePath));
		for (int i=0; i<json.length(); i++) {
			JSONObject obj = (JSONObject) json.get(i);
			String market = obj.getString("market");
			if (!market.startsWith("cbx")) {
				MarketTrade t = new MarketTrade();
				t.setAmount(obj.getDouble("amount"));
				t.setAsset(session.get(Asset.class, obj.getString("market")
						.substring(0, obj.getString("market").length() - 3).toUpperCase()));
				t.setCurrency(session.get(Currency.class, "CBX"));
				t.setFees(obj.getDouble("fee_amount"));
				t.setFeeCurrency(session.get(Currency.class, obj.getString("fee_currency").toUpperCase()));
				t.setId(obj.getLong("id"));
				t.setOrderId(obj.getLong("order_id"));
				t.setPortfolio(portfolio);
				t.setPrice(obj.getDouble("price"));
				t.setTimestamp(parseTimestamp(obj.getString("created_at")));
				t.setTotal(obj.getDouble("total"));
				if ("sell".equals(obj.getString("side"))) {
					t.setTotal(t.getTotal() * -1D);
				}
				result.add(t);
			} else if (market.endsWith("cbx")) {
				LOG.warn("Skipping market trade with market: " + market);
			}
		}
		return result;
	}

	private List<FiatTrade> parseFiatTrades(Portfolio portfolio, String filePath) {
		List<FiatTrade> result = new ArrayList<FiatTrade>();
		JSONArray json = new JSONArray(readFile(filePath));
		for (int i=0; i<json.length(); i++) {
			JSONObject obj = (JSONObject) json.get(i);
			String market = obj.getString("market");
			if (market.startsWith("cbx") || market.startsWith("trx")) {
				FiatTrade t = new FiatTrade();
				t.setAmount(obj.getDouble("amount"));
				t.setTargetCurrency(session.get(Currency.class, obj.getString("market").substring(0, 3).toUpperCase()));
				t.setFees(obj.getDouble("fee_amount"));
				t.setFeeCurrency(session.get(Currency.class, obj.getString("fee_currency").toUpperCase()));
				t.setId(obj.getLong("id"));
				t.setOrderId(obj.getLong("order_id"));
				t.setPortfolio(portfolio);
				t.setPrice(obj.getDouble("price"));
				t.setBaseCurrency(session.get(Currency.class, obj.getString("market").substring(3).toUpperCase()));
				t.setTimestamp(parseTimestamp(obj.getString("created_at")));
				t.setTotal(obj.getDouble("total"));
				if ("sell".equals(obj.getString("side"))) {
					t.setTotal(t.getTotal() * -1D);
				}
				result.add(t);
			} else if (!market.endsWith("cbx")) {
				LOG.warn("Skipping fiat trade with market: " + market);
			}
		}
		return result;
	}

	private List<Deposit> parseDeposits(Portfolio portfolio, String filePath) {
		List<Deposit> deposits = new ArrayList<Deposit>();
		JSONArray json = new JSONArray(readFile(filePath));
		for (int i=0; i<json.length(); i++) {
			JSONObject obj = (JSONObject) json.get(i);
			String state = obj.getString("state");
			if ("collected".equals(state) || "accepted".equals(state)) {
				Deposit d = new Deposit();
				d.setAmount(obj.getDouble("amount"));
				d.setCurrency(session.get(Currency.class, obj.getString("currency").toUpperCase()));
				d.setFees(obj.getDouble("fee"));
				d.setId(obj.getLong("id"));
				d.setPortfolio(portfolio);
				d.setTimestamp(parseTimestamp(obj.getString("completed_at")));
				deposits.add(d);
			} else {
				LOG.warn("Skipping deposit with state: " + state);
			}
		}
		return deposits;
	}

	private String readFile(String filePath) {
		StringBuilder sb = new StringBuilder();
		File file = new File(filePath);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			sb.append(br.readLine());
		} catch (IOException ex) {
			LOG.error(ex);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ex) {
					LOG.error(ex);
				}
			}
		}
		return sb.toString();
	}
	
	private Date parseTimestamp(String input) {
		Date result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		try {
			result = sdf.parse(input);
		} catch (ParseException ex) {
			LOG.error(ex);
		}
		return result;
	}
}
