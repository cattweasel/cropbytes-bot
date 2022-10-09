package net.cattweasel.cropbytes.task;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.json.JSONException;
import org.json.JSONObject;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.tools.HibernateUtil;

public class MarketDataRefreshTask extends Thread {

	private static final Logger LOG = Logger.getLogger(MarketDataRefreshTask.class);
	
	private final Session session;
	
	public MarketDataRefreshTask() {
		this.session = HibernateUtil.openSession();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				refreshMarketData();
			} catch (Exception ex) {
				LOG.error(ex);
			}
			try {
				Thread.sleep(300000L);
			} catch (InterruptedException ex) {
				LOG.error(ex);
			}
		}
	}

	private void refreshMarketData() throws JSONException, IOException {
		LOG.debug("Refreshing MarketQuotes and FiatQuotes..");
		Date timestamp = new Date();
		JSONObject json = new JSONObject(readStringFromURL("https://api.cropbytes.com/api/v2/peatio/public/markets/tickers"));
		Transaction tx = session.beginTransaction();
		for (String key : json.keySet()) {
			JSONObject parent = json.getJSONObject(key);
			if (parent.getString("market").startsWith("fm") || parent.getString("market").startsWith("house")) {
				LOG.info("Skipping FeedMill / House Asset..");
				continue;
			}
			JSONObject obj = parent.getJSONObject("ticker");
			Currency currency = resolveCurrency(parent.getString("market"));
			if (currency == null ) {
				LOG.warn("Currency could not be resolved: " + parent.getString("market") + " - Skipping Entry..");
				continue;
			}
			Asset asset = resolveAsset(parent.getString("market"), currency);
			if (asset == null) {
				Currency fiatCurrency = null;
				Query<Currency> query = session.createQuery("from Currency");
				for (Currency c : query.list()) {
					if (parent.getString("market").startsWith(c.getCode().toLowerCase())) {
						fiatCurrency = c;
					}
				}
				if (fiatCurrency != null) {
					Query<FiatQuote> q = session.createQuery("from FiatQuote where baseCurrency= :baseCurrency and targetCurrency= :targetCurrency");
					q.setParameter("baseCurrency", fiatCurrency);
					q.setParameter("targetCurrency", currency);
					FiatQuote quote = q.uniqueResult();
					quote = quote == null ? new FiatQuote() : quote;
					quote.setAvgPrice(obj.getDouble("avg_price"));
					quote.setBaseCurrency(fiatCurrency);
					quote.setPrice(obj.getDouble("last"));
					String change = obj.getString("price_change_percent");
					Double changeVal = Double.valueOf(change.substring(1, change.length() - 1));
					changeVal = "+".equals(change.substring(0, 1)) ? changeVal : changeVal * -1D;
					quote.setPriceChange(changeVal);
					quote.setTargetCurrency(currency);
					quote.setTimestamp(timestamp);
					quote.setVolume(obj.getDouble("volume"));
					session.save(quote);
				} else {
					LOG.warn("Asset could not be resolved: " + parent.getString("market") + " - Skipping Entry..");
					continue;
				}
			} else {
				Query<MarketQuote> query = session.createQuery("from MarketQuote where asset= :asset and currency= :currency");
				query.setParameter("asset", asset);
				query.setParameter("currency", currency);
				MarketQuote quote = query.uniqueResult();
				quote = quote == null ? new MarketQuote() : quote;
				quote.setAsset(asset);
				quote.setAvgPrice(obj.getDouble("avg_price"));
				quote.setCurrency(currency);
				quote.setPrice(obj.getDouble("last"));
				String change = obj.getString("price_change_percent");
				Double changeVal = Double.valueOf(change.substring(1, change.length() - 1));
				changeVal = "+".equals(change.substring(0, 1)) ? changeVal : changeVal * -1D;
				quote.setPriceChange(changeVal);
				quote.setTimestamp(timestamp);
				quote.setVolume(obj.getDouble("volume"));
				session.save(quote);
			}
		}
		tx.commit();
	}
	
	private String readStringFromURL(String requestURL) throws IOException {
	    try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
	        scanner.useDelimiter("\\A");
	        return scanner.hasNext() ? scanner.next() : "";
	    }
	}
	
	private Asset resolveAsset(String market, Currency currency) {
		String code = market.replaceAll(currency.getCode().toLowerCase(), "").toUpperCase();
		return session.get(Asset.class, code);
	}

	private Currency resolveCurrency(String market) {
		Currency result = null;
		Query<Currency> query = session.createQuery("from Currency");
		for (Currency currency : query.list()) {
			if (market.endsWith(currency.getCode().toLowerCase())) {
				result = currency;
				break;
			}
		}
		return result;
	}
}
