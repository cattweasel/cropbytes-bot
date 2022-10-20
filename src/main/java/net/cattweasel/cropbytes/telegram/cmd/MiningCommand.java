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
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class MiningCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(MiningCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Currency currency = resolveCurrency(session, bot, chatId, data);
		if (currency != null) {
			StringBuilder sb = new StringBuilder();
			appendProfits(session, sb, currency, Asset.AssetType.ANIMAL);
			appendProfits(session, sb, currency, Asset.AssetType.BUILDING);
			appendProfits(session, sb, currency, Asset.AssetType.CROPLAND);
			appendProfits(session, sb, currency, Asset.AssetType.TREE);
			sb.append("\n<i>Note: All asset prices are displayed in " + currency.getCode() + "."
					+ " You can change the currency by appending it to the command (e.g. /mining usdt)</i>");
			SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
			bot.execute(msg);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void appendProfits(Session session, StringBuilder sb, Currency currency, AssetType type) {
		if (sb.length() != 0) sb.append("\n");
		sb.append(String.format("<b>%s ASSETS</b>%n", type));
		ProfitCalculator calc = new ProfitCalculator(session);
		Query<Asset> query = session.createQuery("from Asset where assetType= :assetType and mineable= :mineable");
		query.setParameter("assetType", type);
		query.setParameter("mineable", true);
		Map<Asset, Double> profits = new HashMap<Asset, Double>();
		for (Asset asset : query.list()) {
			try {
				Double profit = calc.calculateMiningProfit(asset, currency);
				profits.put(asset, profit);
			} catch (GeneralException ex) {
				LOG.error(ex);
			}
		}
		appendProfits(sb, profits);
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
	
	@SuppressWarnings("unchecked")
	private Currency resolveCurrency(Session session, TelegramBot bot, Long chatId, String data) {
		Currency currency = session.get(Currency.class, "CBX");
		if (data != null && !"".equals(data)) {
			currency = session.get(Currency.class, data.toUpperCase());
			if (currency == null) {
				StringBuilder sb = new StringBuilder();
				Query<Currency> query = session.createQuery("from Currency");
				for (Currency c : query.list()) {
					if (!"".equals(sb.toString())) sb.append(", ");
					sb.append(c.getCode());
				}
				SendMessage msg = new SendMessage(chatId,
						data.toUpperCase() + " is currently not a supported currency!\n"
								+ "Supported currencies are: " + sb.toString());
				bot.execute(msg);
			}
		}
		return currency;
	}
}
