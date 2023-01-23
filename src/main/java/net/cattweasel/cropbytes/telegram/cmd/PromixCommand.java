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
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.object.PromixRequirement;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;
import net.cattweasel.cropbytes.tools.Util;

public class PromixCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(PromixCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Currency currency = resolveCurrency(session, bot, chatId, data);
		try {
			StringBuilder sb = new StringBuilder();
			MarketDataProvider provider = new MarketDataProvider(session);
			MarketQuote promixQuote = provider.provideMarketQuote(session.get(Asset.class, "PMIX"), currency);
			Map<String, Double> profits = new HashMap<String, Double>();
			profits.put("<b>Promix Market Price</b>", promixQuote.getPrice() * 10D);
			appendProfits(session, profits, currency);
			appendProfits(sb, profits);
			sb.append("\n<i>Note: The prices show you how much you have to pay for 10 PMIX compared to the current market price."
					+ " The costs for feeding your pro animals is not (yet) inculded in here."
					+ " All prices are displayed in " + currency.getCode() + ". You can change the"
					+ " currency by appending it to the command (e.g. /promix usdt)</i>");
			SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
			bot.execute(msg);
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void appendProfits(Session session, Map<String, Double> profits, Currency currency) throws GeneralException {
		MarketDataProvider provider = new MarketDataProvider(session);
		MarketQuote fruitQuote = provider.provideMarketQuote(session.get(Asset.class, "FRF"), currency);
		Query<PromixRequirement> query = session.createQuery("from PromixRequirement");
		for (PromixRequirement req : query.list()) {
			String str = String.format("FRF x 10, %s x %s, %s x %s", req.getPromixAsset().getCode(),
					req.getPromixAmount(), req.getRegularAsset().getCode(), req.getRegularAmount());
			Double price = fruitQuote.getPrice() * 10D;
			
			// TODO -> calculate back on what the pro animal requires to produce the pro assets
			//MarketQuote promixQuote = provider.provideMarketQuote(req.getPromixAsset(), currency);
			//price += promixQuote.getPrice() * req.getPromixAmount();
			
			//System.out.println("** debug: pro_asset: " + req.getPromixAsset().getCode() + " - pro_amount: " + req.getPromixAmount()); // TODO
			
			MarketQuote regularQuote = provider.provideMarketQuote(req.getRegularAsset(), currency);
			price += regularQuote.getPrice() * req.getRegularAmount();
			profits.put(str, price);
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
	
	private void appendProfits(StringBuilder sb, Map<String, Double> profits) {
		Map<String, Double> result = profits.entrySet()
				.stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		for (Map.Entry<String, Double> entry : result.entrySet()) {
			sb.append(String.format("%s\t\t=\t\t%s%n", Util.formatNumber(entry.getValue(), 8, false), entry.getKey()));
		}
	}
}
