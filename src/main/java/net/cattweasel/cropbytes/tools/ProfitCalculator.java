package net.cattweasel.cropbytes.tools;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.Extract;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.object.Requirement;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;

public class ProfitCalculator {

	private static final Logger LOG = Logger.getLogger(ProfitCalculator.class);
	
	private final Session session;
	private final MarketDataProvider provider;
	private final Currency cropBytesToken;
	private final List<FiatQuote> fiatQuotes;
	private final List<MarketQuote> marketQuotes;
	
	public ProfitCalculator(Session session) {
		this(session, null, null);
	}
	
	public ProfitCalculator(Session session, List<FiatQuote> fiatQuotes, List<MarketQuote> marketQuotes) {
		this.session = session;
		this.provider = new MarketDataProvider(session);
		this.cropBytesToken = session.get(Currency.class, "CBX");
		this.fiatQuotes = fiatQuotes;
		this.marketQuotes = marketQuotes;
	}
	
	public Double calculateProfit(Asset asset) throws GeneralException {
		return calculateProfit(asset, 168); // default: 1 week
	}
	
	public Double calculateProfit(Asset asset, Asset seed) throws GeneralException {
		return calculateProfit(asset, 168, seed, cropBytesToken);
	}
	
	public Double calculateProfit(Asset asset, Integer duration) throws GeneralException {
		return calculateProfit(asset, duration, cropBytesToken);
	}
	
	public Double calculateProfit(Asset asset, Integer duration, Currency currency) throws GeneralException {
		return calculateProfit(asset, duration, null, currency);
	}
	
	public Double calculateProfit(Asset asset, Integer duration, Asset seed) throws GeneralException {
		return calculateProfit(asset, duration, seed, cropBytesToken);
	}
	
	public Double calculateProfit(Asset asset, Integer duration, Asset seed, Currency currency) throws GeneralException {
		LOG.debug(String.format("Calculating Profit for %s [%s] (Duration: %s hrs, Asset Duration: %s hrs)",
				asset.getCode(), asset.getName(), duration, asset.getDuration()));
		if (Asset.AssetType.ANIMAL != asset.getAssetType() && Asset.AssetType.BUILDING != asset.getAssetType()
				&& Asset.AssetType.CROPLAND != asset.getAssetType() && Asset.AssetType.TREE != asset.getAssetType()) {
			throw new GeneralException("Unknown AssetType for ProfitCalculator: " + asset.getAssetType());
		}
		Double profit = calculateExtracts(asset, seed);
		profit -= calculateRequirements(asset, seed);
		LOG.debug(String.format("Resulting Profit: %s CBX [24 hrs]", profit));
		// profit has to be provided on 24hrs basis here
		if (Asset.AssetType.ANIMAL != asset.getAssetType()) {
			profit = profit / 24D * duration; // apply duration factor to the profit
		}
		LOG.debug(String.format("Resulting Profit: %s CBX [%s hrs]", profit, duration));
		if (currency != null && currency.getCode() != cropBytesToken.getCode()) {
			FiatQuote quote = provideFiatQuote(cropBytesToken, currency);
			profit = profit * quote.getPrice(); // convert to the desired currency
			LOG.debug(String.format("Converting Profit from CBX to %s: %s %s [%s hrs]",
					currency.getCode(), profit, currency.getCode(), duration));
		}
		LOG.debug(String.format("Resulting Profit: %s %s [%s hrs]", profit, currency.getCode(), duration));
		return profit;
	}

	public Double calculateRequirements(Asset asset) throws GeneralException {
		return calculateRequirements(asset, null);
	}
	
	public Double calculateExtracts(Asset asset) throws GeneralException {
		return calculateExtracts(asset, null);
	}
	
	public Double calculateExtracts(Asset asset, Asset seed) throws GeneralException {
		if (Asset.AssetType.CROPLAND == asset.getAssetType()
				&& (seed == null || Asset.AssetType.SEED != seed.getAssetType())) {
			throw new GeneralException("Cannot calculate extracts for cropland without a seed!");
		}
		Double result = 0D;
		for (Extract extract : asset.getExtracts()) {
			MarketQuote quote = provideMarketQuote(extract.getTarget(), cropBytesToken);
			Double price = quote.getPrice() * extract.getAmount() / asset.getDuration() * 24D;
			
			// CROPLAND: Load price based on the given seed
			if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
				if (!extract.getTarget().getOrigin().getCode().equals(seed.getCode())) {
					continue;
				}
				price = quote.getPrice() * extract.getAmount() / seed.getDuration() * 24D;
			}
			
			// TREE + CROPLAND: Apply grinding fees for extracts
			Double grindingFees = extract.getAmount() * extract.getTarget().getGrindingFees() / asset.getDuration() * 24D; // grinding fees for extract
			if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
				grindingFees = extract.getAmount() * extract.getTarget().getGrindingFees() / seed.getDuration() * 24D; // grinding fees for extract
			}
			result -= grindingFees;
			
			// BUILDING: Nothing to do, plain extract price is fine
			
			// ANIMAL: Nothing to do, plain extract price is fine
			
			result += price;
		}
		return result;
	}

	public Double calculateRequirements(Asset asset, Asset seed) throws GeneralException {
		if (Asset.AssetType.CROPLAND == asset.getAssetType()
				&& (seed == null || Asset.AssetType.SEED != seed.getAssetType())) {
			throw new GeneralException("Cannot calculate requirements for cropland without a seed!");
		}
		Double result = 0D;
		for (Requirement requirement : asset.getRequirements()) {
			MarketQuote quote = provideMarketQuote(requirement.getTarget(), cropBytesToken);
			Double price = quote.getPrice() * requirement.getAmount() / asset.getDuration() * 24D;
			
			// TREE: Nothing to do, plain requirement price is fine
			
			// CROPLAND: Needs to be calculated on the given seed
			if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
				price = quote.getPrice() * requirement.getAmount() / seed.getDuration() * 24D;
				if (Asset.AssetType.SEED == requirement.getTarget().getAssetType()
						&& !requirement.getTarget().getCode().equals(seed.getCode())) {
					continue;
				}
			}
			
			// BUILDING: Nothing to do, plain requirement price is fine
			
			// ANIMAL: Only 6/7 days normal feed - 1/7 days fruit feed
			if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
				if (requirement.getTarget().getAssetType() == Asset.AssetType.FEED) {
					price = price / 7D * 6D; // only 6/7 days normal feed
				}
				price = price / 24D * asset.getDuration(); // some animals have a longer duration than 24 hrs (eg BR)
			}
			result += price;
		}
		
		// ANIMAL: Add 1/7 days fruit feed
		if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
			Asset fruitFeed = session.get(Asset.class, "FRF");
			MarketQuote quote = provideMarketQuote(fruitFeed, cropBytesToken);
			Double fruitPrice = quote.getPrice() / 7D;
			result += fruitPrice; // add 1/7 fruit feed
		}
		
		return result;
	}

	public Double calculateBalance(Farm farm) throws GeneralException {
		Double result = 0D;
		for (FarmAsset asset : farm.getFarmAssets()) {
			result += calculateProfit(asset.getTarget(), 168, asset.getSeeds()) * asset.getAmount();
		}
		return result;
	}
	
	private FiatQuote provideFiatQuote(Currency currency1, Currency currency2) throws GeneralException {
		FiatQuote result = null;
		if (fiatQuotes != null) {
			for (FiatQuote quote : fiatQuotes) {
				if (quote.getBaseCurrency().getCode().equals(currency1.getCode())
						&& quote.getTargetCurrency().getCode().equals(currency2.getCode())) {
					result = quote;
				}
			}
			if (result == null) {
				throw new GeneralException("FiatQuote not found in pre-defined values: "
						+ currency1.getCode() + "/" + currency2.getCode());
			}
		} else {
			result = provider.provideFiatQuote(currency1, currency2);
		}
		return result;
	}
	
	private MarketQuote provideMarketQuote(Asset asset, Currency currency) throws GeneralException {
		MarketQuote result = null;
		if (marketQuotes != null) {
			for (MarketQuote quote : marketQuotes) {
				if (quote.getAsset().getCode().equals(asset.getCode())
						&& quote.getCurrency().getCode().equals(currency.getCode())) {
					result = quote;
					break;
				}
			}
			if (result == null) {
				throw new GeneralException("MarketQuote not found in pre-defined values: "
						+ asset.getCode() + "/" + currency.getCode());
			}
		} else {
			result = provider.provideMarketQuote(asset, currency);
		}
		return result;
	}
}
