package net.cattweasel.cropbytes.telegram.cmd.farms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.Extract;
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.object.Requirement;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.FarmsCommand;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class CheckBalance implements CallbackExecutor {

	private static final Logger LOG = Logger.getLogger(CheckBalance.class);
	
	private static final String BASE_CALLBACK = "farms#CheckBalance";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data)) {
			FarmsCommand.createFarmSelector(session, bot, user, chatId, messageId,
					"Please select the farm for calculation:", BASE_CALLBACK);
		} else {
			String[] parts = data.split("#");
			if (parts.length == 1) {
				EditMessageText message = new EditMessageText(chatId, messageId, "Please select the point of view for this calculation:");
				InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
				keyboard.addRow(
						new InlineKeyboardButton("Market Price").callbackData(BASE_CALLBACK + "#" + data + "#market_price"),
						new InlineKeyboardButton("Asset Amount").callbackData(BASE_CALLBACK + "#" + data + "#asset_amount"));
				keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
				message.replyMarkup(keyboard);
				bot.execute(message);
			} else {
				Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
				if ("market_price".equals(parts[1])) {
					try {
						ProfitCalculator calc = new ProfitCalculator(session);
						StringBuilder sb = new StringBuilder();
						sb.append("<b>ANIMAL ASSETS (" + countAssets(session, farm, Asset.AssetType.ANIMAL) + ")</b>\n");
						if (!farm.isGrazingMode()) {
							printAssetBalance(session, farm, sb, calc, Asset.AssetType.ANIMAL);
						} else {
							printGrazingFees(farm, sb);
						}
						sb.append("<b>BUILDING ASSETS (" + countAssets(session, farm, Asset.AssetType.BUILDING) + ")</b>\n");
						printAssetBalance(session, farm, sb, calc, Asset.AssetType.BUILDING);
						sb.append("<b>CROPLAND ASSETS (" + countAssets(session, farm, Asset.AssetType.CROPLAND) + ")</b>\n");
						printAssetBalance(session, farm, sb, calc, Asset.AssetType.CROPLAND);
						sb.append("<b>TREE ASSETS (" + countAssets(session, farm, Asset.AssetType.TREE) + ")</b>\n");
						printAssetBalance(session, farm, sb, calc, Asset.AssetType.TREE);
						sb.append("<b>TOTAL ASSETS (" + countAssets(session, farm, Asset.AssetType.ANIMAL,
								Asset.AssetType.BUILDING, Asset.AssetType.CROPLAND, Asset.AssetType.TREE) + ")</b>\n");
						printAssetBalance(session, farm, sb, calc, Asset.AssetType.ANIMAL,
								Asset.AssetType.BUILDING, Asset.AssetType.CROPLAND, Asset.AssetType.TREE);
						sb.append("<i>Note: All prices are displayed in CBX (weekly projection)</i>");
						EditMessageText message = new EditMessageText(chatId, messageId, sb.toString()).parseMode(ParseMode.HTML);
						InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
						keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
						message.replyMarkup(keyboard);
						bot.execute(message);
					} catch (GeneralException ex) {
						LOG.error(ex);
					}
				} else {
					try {
						StringBuilder sb = new StringBuilder();
						Map<String, Double> result = new HashMap<String, Double>();
						for (FarmAsset farmAsset : farm.getFarmAssets()) {
							if (Asset.AssetType.ANIMAL == farmAsset.getTarget().getAssetType() && farm.isGrazingMode()) {
								continue;
							}
							Map<String, Double> requirements = calculateRequirements(farmAsset.getTarget(), farmAsset.getSeeds());
							for (Map.Entry<String, Double> entry : requirements.entrySet()) {
								Double current = result.get(entry.getKey());
								current = current == null ? entry.getValue() * -1D * farmAsset.getAmount() : current - entry.getValue() * farmAsset.getAmount();
								result.put(entry.getKey(), current);
							}
							Map<String, Double> extracts = calculateExtracts(session, farmAsset.getTarget(), farmAsset.getSeeds());
							for (Map.Entry<String, Double> entry : extracts.entrySet()) {
								Double current = result.get(entry.getKey());
								current = current == null ? entry.getValue() * farmAsset.getAmount() : current + entry.getValue() * farmAsset.getAmount();
								result.put(entry.getKey(), current);
							}
						}
						appendBalances(session, sb, result, 7);
						sb.append("\n<i>Note: All values are displayed in a weekly projection</i>");
						EditMessageText message = new EditMessageText(chatId, messageId, sb.toString()).parseMode(ParseMode.HTML);
						InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
						keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
						message.replyMarkup(keyboard);
						bot.execute(message);
					} catch (GeneralException ex) {
						LOG.error(ex);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Long countAssets(Session session, Farm farm, AssetType ... assetTypes) {
		Long result = 0L;
		for (AssetType assetType : assetTypes) {
			Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target.assetType= :assetType");
			query.setParameter("farm", farm);
			query.setParameter("assetType", assetType);
			for (FarmAsset asset : query.list()) {
				result += asset.getAmount();
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void printAssetBalance(Session session, Farm farm, StringBuilder sb,
			ProfitCalculator calc, AssetType ... assetTypes) throws GeneralException {
		Double extracts = 0D;
		Double requirements = 0D;
		for (AssetType assetType : assetTypes) {
			Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target.assetType= :assetType");
			query.setParameter("farm", farm);
			query.setParameter("assetType", assetType);
			for (FarmAsset asset : query.list()) {
				if (Asset.AssetType.ANIMAL == asset.getTarget().getAssetType() && farm.isGrazingMode()) {
					requirements += asset.getTarget().getGrazingFees() * asset.getAmount();
				} else {
					extracts += calc.calculateExtracts(asset.getTarget(), asset.getSeeds(), farm.isGrindingFees()) * asset.getAmount() / 24D * 168D;
					requirements += calc.calculateRequirements(asset.getTarget(), asset.getSeeds()) * asset.getAmount() / 24D * 168D;
				}
			}
		}
		sb.append(Util.formatNumber(requirements * -1D, 8) + "\t\t=\t\tTotal Requirements\n");
		sb.append(Util.formatNumber(extracts, 8) + "\t\t=\t\tTotal Extracts\n");
		sb.append(Util.formatNumber(extracts - requirements, 8) + "\t\t=\t\tResulting Balance\n\n");
	}
	
	private void printGrazingFees(Farm farm, StringBuilder sb) {
		Double fees = 0D;
		for (FarmAsset asset : farm.getFarmAssets()) {
			if (Asset.AssetType.ANIMAL == asset.getTarget().getAssetType()) {
				fees -= asset.getTarget().getGrazingFees() * asset.getAmount();
			}
		}
		sb.append(Util.formatNumber(fees, 8) + "\t\t=\t\tGrazing Fees\n\n");
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Double> calculateExtracts(Session session, Asset asset, Asset seed) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (Extract extract : asset.getExtracts()) {
			String code = extract.getTarget().getCode();
			Double balance = extract.getAmount() / asset.getDuration() * 24D;
			if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
				if (!extract.getTarget().getOrigin().getCode().equals(seed.getCode())) {
					continue;
				}
				balance = extract.getAmount() / seed.getDuration() * 24D;
				Query<Asset> query = session.createQuery("from Asset where origin= :origin");
				query.setParameter("origin", seed);
				code = query.uniqueResult().getCode();
			}
			result.put(code, balance);
		}
		return result;
	}
	
	private Map<String, Double> calculateRequirements(Asset asset, Asset seed) {
		Map<String, Double> result = new HashMap<String, Double>();
		for (Requirement requirement : asset.getRequirements()) {
			String code = requirement.getTarget().getCode();
			Double balance = requirement.getAmount() / asset.getDuration() * 24D;
			if (Asset.AssetType.CROPLAND == asset.getAssetType()) {
				balance = requirement.getAmount() / seed.getDuration() * 24D;
				code = seed.getCode();
				if (Asset.AssetType.SEED == requirement.getTarget().getAssetType()
						&& !requirement.getTarget().getCode().equals(seed.getCode())) {
					continue;
				}
			}
			if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
				if (requirement.getTarget().getAssetType() == Asset.AssetType.FEED) {
					balance = balance / 7D * 6D;
				}
				balance = balance / 24D * asset.getDuration();
			}
			result.put(code, balance);
		}
		if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
			Double current = result.get("FRF");
			current = current == null ? 1D / 7D : current + (1D / 7D);
			result.put("FRF", current);
		}
		return result;
	}
	
	private void appendBalances(Session session, StringBuilder sb, Map<String, Double> balances, Integer days) throws GeneralException {
		Map<String, Double> result = balances.entrySet()
				.stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		MarketDataProvider provider = new MarketDataProvider(session);
		for (Map.Entry<String, Double> entry : result.entrySet()) {
			MarketQuote quote = provider.provideMarketQuote(session.get(Asset.class, entry.getKey()), session.get(Currency.class, "CBX"));
			sb.append(String.format("%s\t\t=\t\t%s\t\t[%s CBX]%n", Util.formatNumber(entry.getValue() * days, 8), entry.getKey(),
					Util.formatNumber(quote.getPrice() * entry.getValue() * days, 8)));
		}
	}
}
