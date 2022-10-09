package net.cattweasel.cropbytes.telegram.cmd;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class ProfitCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(ProfitCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Currency currency = resolveCurrency(session, bot, chatId, data);
		if (currency != null) {
			StringBuilder sb = new StringBuilder();
			appendProfits(session, sb, currency, Asset.AssetType.ANIMAL);
			appendProfits(session, sb, currency, Asset.AssetType.BUILDING);
			appendProfits(session, sb, currency, Asset.AssetType.CROPLAND);
			appendProfits(session, sb, currency, Asset.AssetType.TREE);
			sb.append("\n<i>Note: All asset prices are displayed in " + currency.getCode() + " (weekly projection)."
					+ " You can change the currency by appending it to the command (e.g. /profit cbx)</i>");
			SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
			bot.execute(msg);
		}
	}
	
	private void appendProfits(Session session, StringBuilder sb, Currency currency, AssetType type) {
		if (Asset.AssetType.CROPLAND == type) {
			appendCroplandProfits(session, sb, currency);
		} else {
			if (!sb.isEmpty()) sb.append("\n");
			sb.append(String.format("<b>%s ASSETS</b>%n", type));
			ProfitCalculator calc = new ProfitCalculator(session);
			Query<Asset> query = session.createQuery("from Asset where assetType= :assetType and proAsset= :proAsset and trialAsset= :trialAsset");
			query.setParameter("assetType", type);
			query.setParameter("proAsset", false);
			query.setParameter("trialAsset", false);
			Map<Asset, Double> profits = new HashMap<Asset, Double>();
			for (Asset asset : query.list()) {
				try {
					Double profit = calc.calculateProfit(asset, 168, currency);
					profits.put(asset, profit);
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
			}
			appendProfits(sb, profits);
		}
	}
	
	private void appendCroplandProfits(Session session, StringBuilder sb, Currency currency) {
		ProfitCalculator calc = new ProfitCalculator(session);
		Query<Asset> query = session.createQuery("from Asset where assetType= :assetType");
		query.setParameter("assetType", Asset.AssetType.SEED);
		for (Asset seed : query.list()) {
			Map<Asset, Double> profits = new HashMap<Asset, Double>();
			if (!sb.isEmpty()) sb.append("\n");
			sb.append(String.format("<b>CROPLAND ASSETS (%s)</b>%n", seed.getName()));
			Query<Asset> q = session.createQuery("from Asset where assetType= :assetType");
			q.setParameter("assetType", Asset.AssetType.CROPLAND);
			for (Asset cropland : q.list()) {
				try {
					Double profit = calc.calculateProfit(cropland, 168, seed, currency);
					profits.put(cropland, profit);
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
			}
			appendProfits(sb, profits);
		}
	}

	private void appendProfits(StringBuilder sb, Map<Asset, Double> profits) {
		Map<Asset, Double> result = profits.entrySet()
				.stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Map.Entry<Asset, Double> entry : result.entrySet()) {
			sb.append(String.format("%s\t\t[%s]\t\t%s%n", Util.formatNumber(entry.getValue(), 8),
					entry.getKey().getCode(), entry.getKey().getName()));
		}
	}

	private Currency resolveCurrency(Session session, TelegramBot bot, Long chatId, String data) {
		Currency currency = session.get(Currency.class, "USDT");
		if (data != null && !"".equals(data)) {
			currency = session.get(Currency.class, data.toUpperCase());
			if (currency == null) {
				String currencies = "";
				Query<Currency> query = session.createQuery("from Currency");
				for (Currency c : query.list()) {
					if (!"".equals(currencies)) currencies += ", ";
					currencies += c.getCode();
				}
				SendMessage msg = new SendMessage(chatId,
						data.toUpperCase() + " is currently not a supported currency!\n"
								+ "Supported currencies are: " + currencies);
				bot.execute(msg);
			}
		}
		return currency;
	}
}
