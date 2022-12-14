package net.cattweasel.cropbytes.tools;

import org.hibernate.Session;
import org.hibernate.query.Query;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.MarketQuote;

/**
 * Utility class for providing market data (MarketQuotes and FiatQuotes).
 * 
 * @author cattweasel
 *
 */
public class MarketDataProvider {
	
	private final Session session;
	
	/**
	 * Setup a new MarketDataProvider with a given database session.
	 * 
	 * @param session The database session to be used
	 */
	public MarketDataProvider(Session session) {
		this.session = session;
	}
	
	/**
	 * Provide a MarketQuote for a given asset / currency pair.
	 * 
	 * @param asset The asset of the pair
	 * @param currency The currency of the pair
	 * @return The resolved MarketQuote
	 * @throws GeneralException If MarketQuote cannot be resolved
	 */
	@SuppressWarnings("unchecked")
	public MarketQuote provideMarketQuote(Asset asset, Currency currency) throws GeneralException {
		Query<MarketQuote> query = session.createQuery("from MarketQuote where asset= :asset and currency= :currency");
		query.setParameter("asset", asset);
		query.setParameter("currency", currency);
		MarketQuote quote = query.uniqueResult();
		if (quote == null && currency != session.get(Currency.class, "CBX")) {
			query.setParameter("asset", asset);
			query.setParameter("currency", session.get(Currency.class, "CBX"));
			MarketQuote q = query.uniqueResult();
			if (q != null) {
				FiatQuote fiat = provideFiatQuote(session.get(Currency.class, "CBX"), currency);
				quote = new MarketQuote();
				quote.setAvgPrice(q.getAvgPrice() * fiat.getPrice());
				quote.setCurrency(currency);
				quote.setPrice(q.getPrice() * fiat.getPrice());
				quote.setPriceChange(0D);
				quote.setTimestamp(q.getTimestamp());
				quote.setVolume(0D);
			}
		}
		if (quote == null) {
			throw new GeneralException("MarketQuote could not be resolved: " + asset.getCode() + "/" + currency.getCode());
		}
		return quote;
	}

	/**
	 * Provide a FiatQuote for a given currency pair.
	 * 
	 * @param baseCurrency The base currency of the pair
	 * @param targetCurrency The target currency of the pair
	 * @return The resolved FiatQuote
	 * @throws GeneralException If the FiatQuote cannot be resolved
	 */
	@SuppressWarnings("unchecked")
	public FiatQuote provideFiatQuote(Currency baseCurrency, Currency targetCurrency) throws GeneralException {
		Query<FiatQuote> query = session.createQuery("from FiatQuote where baseCurrency= :baseCurrency and targetCurrency= :targetCurrency");
		query.setParameter("baseCurrency", baseCurrency);
		query.setParameter("targetCurrency", targetCurrency);
		FiatQuote quote = query.uniqueResult();
		if (quote == null) {
			query.setParameter("baseCurrency", targetCurrency);
			query.setParameter("targetCurrency", baseCurrency);
			FiatQuote q = query.uniqueResult();
			if (q != null) {
				quote = new FiatQuote();
				quote.setAvgPrice(1D / q.getAvgPrice());
				quote.setPrice(1D / q.getPrice());
				quote.setPriceChange(q.getPriceChange() * -1D);
				quote.setTimestamp(q.getTimestamp());
				quote.setVolume(q.getVolume());
				quote.setBaseCurrency(targetCurrency);
				quote.setTargetCurrency(baseCurrency);
			}
		}
		if (quote == null) {
			throw new GeneralException("FiatQuote could not be resolved: " + baseCurrency.getCode() + "/" + targetCurrency.getCode());
		}
		return quote;
	}
}
