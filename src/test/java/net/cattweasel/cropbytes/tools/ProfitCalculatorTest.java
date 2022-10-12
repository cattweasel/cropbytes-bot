package net.cattweasel.cropbytes.tools;

import java.util.Arrays;
import java.util.Date;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.MarketQuote;

public class ProfitCalculatorTest {

	private Session session;
	private ProfitCalculator calculator;
	
	@Before
	public void setUp() {
		session = HibernateUtil.openSession();
		calculator = new ProfitCalculator(session,
				Arrays.asList(), // TODO
				Arrays.asList(
						createMarketQuote("CAF", "CBX", 1.34D),
						createMarketQuote("CAS", "CBX", 2D),
						createMarketQuote("COF", "CBX", 0.125D),
						createMarketQuote("COS", "CBX", 0.1D),
						createMarketQuote("FRF", "CBX", 1.3D),
						createMarketQuote("FTR", "CBX", 0.558D),
						createMarketQuote("GR", "CBX", 10D),
						createMarketQuote("POW", "CBX", 0.623D),
						createMarketQuote("WATER", "CBX", 0.05D)
				)
		);
	}

	@After
	public void tearDown() {
		session.close();
	}
	
	@Test
	public void testCalculateExtracts() throws GeneralException {
		
		Asset asset = session.get(Asset.class, "BR");
		Double extracts = calculator.calculateExtracts(asset);
		Assert.assertEquals(0.372D, extracts, 0.00005D);
		
		asset = session.get(Asset.class, "SPW");
		extracts = calculator.calculateExtracts(asset);
		Assert.assertEquals(1.869D, extracts, 0.00005D);
		
		asset = session.get(Asset.class, "BT");
		extracts = calculator.calculateExtracts(asset);
		Assert.assertEquals(1.275D, extracts, 0.00005D);
		
		asset = session.get(Asset.class, "OCL");
		Asset seed = session.get(Asset.class, "COS");
		extracts = calculator.calculateExtracts(asset, seed);
		Assert.assertEquals(0.84D, extracts, 0.00005D);
	}
	
	@Test
	public void testCalculateRequirements() throws GeneralException {
		
		Asset asset = session.get(Asset.class, "BR");
		Double requirements = calculator.calculateRequirements(asset);
		Assert.assertEquals(0.290476D, requirements, 0.00005D);
		
		asset = session.get(Asset.class, "SPW");
		requirements = calculator.calculateRequirements(asset);
		Assert.assertEquals(0.3D, requirements, 0.00005D);
		
		asset = session.get(Asset.class, "BT");
		requirements = calculator.calculateRequirements(asset);
		Assert.assertEquals(0.05D, requirements, 0.00005D);
		
		asset = session.get(Asset.class, "OCL");
		Asset seed = session.get(Asset.class, "COS");
		requirements = calculator.calculateRequirements(asset, seed);
		Assert.assertEquals(0.1D, requirements, 0.00005D);
	}
	
	@Test
	public void testCalculateProfit() throws GeneralException {
		
		Asset asset = session.get(Asset.class, "BR");
		Double profit = calculator.calculateProfit(asset);
		Assert.assertEquals(0.5706666D, profit, 0.00005D);
		
		asset = session.get(Asset.class, "SPW");
		profit = calculator.calculateProfit(asset);
		Assert.assertEquals(10.983D, profit, 0.00005D);
		
		asset = session.get(Asset.class, "BT");
		profit = calculator.calculateProfit(asset);
		Assert.assertEquals(8.575D, profit, 0.00005D);
		
		asset = session.get(Asset.class, "OCL");
		Asset seed = session.get(Asset.class, "COS");
		profit = calculator.calculateProfit(asset, seed);
		Assert.assertEquals(5.18D, profit, 0.00005D);
	}
	
	@Test
	public void testCalculateBalance() {
		// TODO
	}
	
	private MarketQuote createMarketQuote(String assetCode, String currencyCode, Double value) {
		MarketQuote quote = new MarketQuote();
		quote.setAsset(session.get(Asset.class, assetCode));
		quote.setAvgPrice(value);
		quote.setCurrency(session.get(Currency.class, currencyCode));
		quote.setPrice(value);
		quote.setPriceChange(0D);
		quote.setTimestamp(new Date());
		quote.setVolume(0D);
		return quote;
	}
}
