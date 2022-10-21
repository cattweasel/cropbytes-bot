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
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;

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
						createMarketQuote("EGG", "CBX", 0.075D),
						createMarketQuote("FRF", "CBX", 1.3D),
						createMarketQuote("FTR", "CBX", 0.558D),
						createMarketQuote("GR", "CBX", 10D),
						createMarketQuote("ORT", "CBX", 975D),
						createMarketQuote("PMIX", "CBX", 3.1D),
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
		
		Asset asset = session.get(Asset.class, "EGR");
		Double extracts = calculator.calculateExtracts(asset);
		Assert.assertEquals(0.3D, extracts, 0.00005D);
		
		asset = session.get(Asset.class, "BR");
		extracts = calculator.calculateExtracts(asset);
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
		
		Asset asset = session.get(Asset.class, "EGR");
		Double requirements = calculator.calculateRequirements(asset);
		Assert.assertEquals(0.45D, requirements, 0.00005D);
		
		asset = session.get(Asset.class, "BR");
		requirements = calculator.calculateRequirements(asset);
		Assert.assertEquals(0.5D, requirements, 0.00005D);
		
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
		
		Asset asset = session.get(Asset.class, "EGR");
		Double profit = calculator.calculateProfit(asset);
		Assert.assertEquals(-1.05D, profit, 0.00005D);
		
		asset = session.get(Asset.class, "BR");
		profit = calculator.calculateProfit(asset);
		Assert.assertEquals(-0.896D, profit, 0.00005D);
		
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
	public void testCalculateBalance() throws GeneralException {
		Farm farm = new Farm();
		farm.addFarmAsset(createFarmAsset(farm, "EGR", null, 1));
		farm.addFarmAsset(createFarmAsset(farm, "BR", null, 1));
		farm.addFarmAsset(createFarmAsset(farm, "SPW", null, 1));
		farm.addFarmAsset(createFarmAsset(farm, "BT", null, 1));
		farm.addFarmAsset(createFarmAsset(farm, "OCL", "COS", 1));
		Double balance = calculator.calculateBalance(farm);
		Assert.assertEquals(22.792D, balance, 0.00005D);
	}
	
	@Test
	public void testCalculateMiningProfit() throws GeneralException {
		Asset asset = session.get(Asset.class, "ORT");
		Double profit = calculator.calculateMiningProfit(asset);
		Assert.assertEquals(32.773D, profit, 0.00005D);
	}
	
	private FarmAsset createFarmAsset(Farm farm, String assetCode, String seedsCode, int amount) {
		Asset asset = session.get(Asset.class, assetCode);
		FarmAsset farmAsset = new FarmAsset();
		farmAsset.setAmount(amount);
		farmAsset.setFarm(farm);
		farmAsset.setTarget(asset);
		if (seedsCode != null) {
			Asset seeds = session.get(Asset.class, seedsCode);
			farmAsset.setSeeds(seeds);
		}
		return farmAsset;
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
