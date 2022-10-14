package net.cattweasel.cropbytes.telegram.cmd;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;
import net.cattweasel.cropbytes.tools.Util;

public class QuotesCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(QuotesCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Currency currency = resolveCurrency(session, bot, chatId, data);
		if (currency != null) {
			StringBuilder sb = new StringBuilder();
			printMarketQuotes(session, currency, sb);
			sb.append("\n");
			printFiatQuotes(session, currency, sb);
			sb.append("\n<i>Note: All asset prices are displayed in " + currency.getCode() + ". You can change the"
					+ " currency by appending it to the command (e.g. /quotes usdt)</i>");
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
	
	@SuppressWarnings("unchecked")
	private void printMarketQuotes(Session session, Currency currency, StringBuilder sb) {
		MarketDataProvider provider = new MarketDataProvider(session);
		Query<Asset> query = session.createQuery("from Asset where tradeable= :tradeable");
		query.setParameter("tradeable", true);
		sb.append("\n<b>GAME ASSETS</b>\n");
		for (Asset asset : query.list()) {
			try {
				MarketQuote quote = provider.provideMarketQuote(asset, currency);
				sb.append(String.format("%s\t\t[%s]\t\t%s\t\t[%s%s%%]%n", Util.formatNumber(quote.getPrice(), 8, false),
						asset.getCode(), asset.getName(), quote.getPriceChange() >= 0D ? "+" : "", quote.getPriceChange()));
			} catch (GeneralException ex) {
				LOG.error(ex);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void printFiatQuotes(Session session, Currency currency, StringBuilder sb) {
		MarketDataProvider provider = new MarketDataProvider(session);
		Query<Currency> query = session.createQuery("from Currency where code!= :code");
		query.setParameter("code", currency.getCode());
		sb.append("<b>CURRENCIES</b>\n");
		for (Currency c : query.list()) {
			try {
				FiatQuote quote = provider.provideFiatQuote(currency, c);
				sb.append(String.format("%s\t\t%s / %s\t\t[%s%s%%]%n", Util.formatNumber(quote.getPrice(), 8, false),
						currency.getCode(), c.getCode(), quote.getPriceChange() >= 0D ? "+" : "", quote.getPriceChange()));
			} catch (GeneralException ex) {
				LOG.error(ex);
			}
		}
	}
}
