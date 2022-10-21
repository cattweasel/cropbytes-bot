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
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class GrazingCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(GrazingCommand.class);
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Currency currency = resolveCurrency(session, bot, chatId, data);
		if (currency != null) {
			StringBuilder sb = new StringBuilder();
			ProfitCalculator calc = new ProfitCalculator(session);
			Query<Asset> query = session.createQuery("from Asset where grazingFees is not null and proAsset = false");
			Map<Asset, Double> grazingFees = new HashMap<Asset, Double>();
			for (Asset asset : query.list()) {
				try {
					Double profit = calc.calculateProfit(asset, 168);
					profit -= asset.getGrazingFees();
					if (!currency.getCode().equals("CBX")) {
						MarketDataProvider provider = new MarketDataProvider(session);
						FiatQuote quote = provider.provideFiatQuote(session.get(Currency.class, "CBX"), currency);
						profit = profit * quote.getPrice(); // convert to the desired currency
					}
					grazingFees.put(asset, profit);
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
			}
			appendGrazingFees(sb, grazingFees);
			sb.append("\n<i>Note: All prices are displayed in " + currency.getCode() + "."
					+ " You can change the currency by appending it to the command (e.g. /grazing usdt)</i>");
			SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
			bot.execute(msg);
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
	
	private void appendGrazingFees(StringBuilder sb, Map<Asset, Double> grazingFees) {
		Map<Asset, Double> result = grazingFees.entrySet()
				.stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Map.Entry<Asset, Double> entry : result.entrySet()) {
			sb.append(String.format("%s\t\t[%s]\t\t%s%n", Util.formatNumber(entry.getValue(), 8),
					entry.getKey().getCode(), entry.getKey().getName()));
		}
	}
}
