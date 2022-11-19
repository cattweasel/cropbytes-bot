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

/**
 * Utility class for several kinds of profit calculations.
 * 
 * @author cattweasel
 *
 */
public class ProfitCalculator {

	private static final Logger LOG = Logger.getLogger(ProfitCalculator.class);
	
	private final Session session;
	private final MarketDataProvider provider;
	private final Currency cropBytesToken;
	private final List<FiatQuote> fiatQuotes;
	private final List<MarketQuote> marketQuotes;
	
	/**
	 * Create a new calculator instance with a given database session.
	 * 
	 * @param session The database session to be used
	 */
	public ProfitCalculator(Session session) {
		this(session, null, null);
	}
	
	/**
	 * Create a new calculator instance with pre-defined market values (for testing).
	 * 
	 * @param session The database session to be used
	 * @param fiatQuotes The FiatQuotes to be used
	 * @param marketQuotes The MarketQuotes to be used
	 */
	public ProfitCalculator(Session session, List<FiatQuote> fiatQuotes, List<MarketQuote> marketQuotes) {
		this.session = session;
		this.provider = new MarketDataProvider(session);
		this.cropBytesToken = session.get(Currency.class, "CBX");
		this.fiatQuotes = fiatQuotes;
		this.marketQuotes = marketQuotes;
	}
	
	/**
	 * Calculate the profitability for a single asset (non-cropland).
	 * 
	 * @param asset The asset to be calculated
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset) throws GeneralException {
		return calculateProfit(asset, 168); // default: 1 week
	}
	
	/**
	 * Calculate the profitability for a single cropland asset.
	 * 
	 * @param asset The asset to be calculated
	 * @param seed The seed to be used
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset, Asset seed) throws GeneralException {
		return calculateProfit(asset, 168, seed, cropBytesToken, true); // default: 1 week, CBX + grinding fees
	}
	
	/**
	 * Calculate the profitability for a single asset and a given duration.
	 * 
	 * @param asset The asset to be calculated
	 * @param duration The duration (hours) to be applied for calculation
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset, Integer duration) throws GeneralException {
		return calculateProfit(asset, duration, cropBytesToken); // default: CBX
	}
	
	/**
	 * Calculate the profitability for a single asset and a given duration (non-cropland).
	 * 
	 * @param asset The asset to be calculated
	 * @param duration The duration (hours) to be applied for calculation
	 * @param currency The currency to be used for output
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset, Integer duration, Currency currency) throws GeneralException {
		return calculateProfit(asset, duration, null, currency, true); // default: grinding fees
	}
	
	/**
	 * Calculate the profitability for a single cropland asset and a given duration.
	 * 
	 * @param asset The asset to be calculated
	 * @param duration The duration (hours) to be applied for calculation
	 * @param seed The seed to be used
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset, Integer duration, Asset seed) throws GeneralException {
		return calculateProfit(asset, duration, seed, cropBytesToken, true); // default: CBX + grinding fees
	}
	
	/**
	 * Calculate the profitability for a single asset, a given duration and a given currency.
	 * 
	 * @param asset The asset to be calculated
	 * @param duration The duration (hours) to be applied for calculation
	 * @param seed The seed to be used (if asset is cropland)
	 * @param currency The currency to be used for output
	 * @param grindingFees Select if grinding fees should be applies
	 * @return The profitability of the given asset
	 * @throws GeneralException If the profitability cannot be calculated
	 */
	public Double calculateProfit(Asset asset, Integer duration, Asset seed,
			Currency currency, Boolean grindingFees) throws GeneralException {
		LOG.debug(String.format("Calculating Profit for %s [%s] (Duration: %s hrs, Asset Duration: %s hrs)",
				asset.getCode(), asset.getName(), duration, asset.getDuration()));
		if (Asset.AssetType.ANIMAL != asset.getAssetType() && Asset.AssetType.BUILDING != asset.getAssetType()
				&& Asset.AssetType.CROPLAND != asset.getAssetType() && Asset.AssetType.TREE != asset.getAssetType()) {
			throw new GeneralException("Unknown AssetType for ProfitCalculator: " + asset.getAssetType());
		}
		Double profit = calculateExtracts(asset, seed, grindingFees);
		profit -= calculateRequirements(asset, seed);
		LOG.debug(String.format("Resulting Profit: %s CBX [24 hrs]", profit));
		// profit has to be provided on 24hrs basis here
		profit = profit / 24D * duration; // apply duration factor to the profit
		LOG.debug(String.format("Resulting Profit: %s CBX [%s hrs]", profit, duration));
		if (currency != null && !currency.getCode().equals(cropBytesToken.getCode())) {
			FiatQuote quote = provideFiatQuote(cropBytesToken, currency);
			profit = profit * quote.getPrice(); // convert to the desired currency
			LOG.debug(String.format("Converting Profit from CBX to %s: %s %s [%s hrs]",
					currency.getCode(), profit, currency.getCode(), duration));
		}
		return profit;
	}

	/**
	 * Calculate the requirements for a single asset (24 hours).
	 * 
	 * @param asset The asset to be calculated
	 * @return The costs for all requirements (24 hours)
	 * @throws GeneralException If requirements cannot be calculated
	 */
	public Double calculateRequirements(Asset asset) throws GeneralException {
		return calculateRequirements(asset, null);
	}
	
	/**
	 * Calculate the extracts for a single asset (24 hours).
	 * 
	 * @param asset The asset to be calculated
	 * @return The costs for all extracts (24 hours)
	 * @throws GeneralException If extracts cannot be calculated
	 */
	public Double calculateExtracts(Asset asset) throws GeneralException {
		return calculateExtracts(asset, null, true);
	}
	
	/**
	 * Calculate the extracts for a single asset (24 hours).
	 * 
	 * @param asset The asset to be calculated
	 * @param seed The seed to be used (if asset is cropland)
	 * @param grindingFees Select to apply grinding fees or not
	 * @return The costs for all extracts (24 hours)
	 * @throws GeneralException If extracts cannot be calculated
	 */
	public Double calculateExtracts(Asset asset, Asset seed, Boolean grindingFees) throws GeneralException {
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
			if (grindingFees) {
				Double fees = extract.getAmount() * extract.getTarget().getGrindingFees() / asset.getDuration() * 24D; // grinding fees for extract
				if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
					fees = extract.getAmount() * extract.getTarget().getGrindingFees() / seed.getDuration() * 24D; // grinding fees for extract
				}
				result -= fees;
			}
			
			// BUILDING: Nothing to do, plain extract price is fine
			
			// ANIMAL: Nothing to do, plain extract price is fine
			
			result += price;
		}
		return result;
	}

	/**
	 * Calculate the requirements for a single asset (24 hours).
	 * 
	 * @param asset The asset to be calculated
	 * @param seed The seed to be used (if asset is cropland)
	 * @return The costs for all requirements (24 hours)
	 * @throws GeneralException If the requirements cannot be calculated
	 */
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
				price = price * asset.getAppetiteLevel(); // apply appetite level for animal assets here
			}
			result += price;
		}
		
		// ANIMAL: Add 1/7 days fruit feed
		if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
			Asset fruitFeed = session.get(Asset.class, "FRF");
			MarketQuote quote = provideMarketQuote(fruitFeed, cropBytesToken);
			Double fruitPrice = quote.getPrice() / 7D * asset.getAppetiteLevel();
			result += fruitPrice; // add 1/7 fruit feed
		}
		
		return result;
	}

	/**
	 * Check the balance of a given farm. All profits will be aggregated.
	 * 
	 * @param farm The farm to be checked
	 * @return The balance of the farm
	 * @throws GeneralException If the balance cannot be calculated
	 */
	public Double calculateBalance(Farm farm) throws GeneralException {
		Double result = 0D;
		for (FarmAsset asset : farm.getFarmAssets()) {
			if (Asset.AssetType.ANIMAL == asset.getTarget().getAssetType() && farm.isGrazingMode()) {
				result -= asset.getTarget().getGrazingFees() * asset.getAmount();
			} else {
				result += calculateProfit(asset.getTarget(), 168, asset.getSeeds(),
						cropBytesToken, farm.isGrindingFees()) * asset.getAmount();
			}
		}
		return result;
	}
	
	/**
	 * Check the mining profitability of an asset (market price vs mining price).
	 * 
	 * @param asset The asset to be checked
	 * @return The resulting profitability
	 * @throws GeneralException If profitability cannot be calculated
	 */
	public Double calculateMiningProfit(Asset asset) throws GeneralException {
		return calculateMiningProfit(asset, cropBytesToken);
	}
	
	/**
	 * Check the mining profitability of an asset (market price vs mining price).
	 * 
	 * @param asset The asset to be checked
	 * @param currency The currency to be applied
	 * @return The resulting profitability
	 * @throws GeneralException If profitability cannot be calculated
	 */
	public Double calculateMiningProfit(Asset asset, Currency currency) throws GeneralException {
		if (!asset.isMineable()) {
			throw new GeneralException("Cannot calculate mining profitability for non-mineable asset!");
		}
		MarketQuote assetQuote = provideMarketQuote(asset, cropBytesToken);
		MarketQuote pmixQuote = provideMarketQuote(session.get(Asset.class, "PMIX"), cropBytesToken);
		Double miningPrice = asset.getMiningProMix() * pmixQuote.getPrice() + asset.getMiningFees();
		Double profit = assetQuote.getPrice() - miningPrice;
		if (currency != null && !currency.getCode().equals(cropBytesToken.getCode())) {
			FiatQuote quote = provideFiatQuote(cropBytesToken, currency);
			profit = profit * quote.getPrice(); // convert to the desired currency
		}
		return profit;
	}
	
	/**
	 * Private helper method to provide the correct FiatQuotes (test vs real world).
	 * 
	 * @param currency1 The base currency of the quote
	 * @param currency2 The target currency of the quote
	 * @return The resolved FiatQuote
	 * @throws GeneralException If the FiatQuote cannot be resolved
	 */
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
	
	/**
	 * Private helper method to provide the correct MarketQuotes (test vs real world).
	 * 
	 * @param asset The asset of the quote
	 * @param currency The currency of the quote
	 * @return The resolved MarketQuote
	 * @throws GeneralException If the MarketQuote cannot be resolved
	 */
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
