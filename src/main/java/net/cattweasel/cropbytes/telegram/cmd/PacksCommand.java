package net.cattweasel.cropbytes.telegram.cmd;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class PacksCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(PacksCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		Double profit = null;
		StringBuilder sb = new StringBuilder();
		try {
			
			sb.append("<b>SMALL STARTER PACK</b>\n");
			sb.append(Util.formatNumber(20D, 6, false) + "\t\t=\t\tTotal Investment (USDT)\n");
			profit = calculateProfit(session, session.get(Asset.class, "SCL"), session.get(Asset.class, "DS"), session.get(Asset.class, "DS"));
			sb.append(Util.formatNumber(profit, 6, false) + "\t\t=\t\tDaily Profitability (USDT)\n");
			sb.append(Util.formatNumber(20D / profit, 6, false) + "\t\t=\t\tProjected ROI (DAYS)\n\n");
			
			sb.append("<b>MEDIUM STARTER PACK</b>\n");
			sb.append(Util.formatNumber(35D, 6, false) + "\t\t=\t\tTotal Investment (USDT)\n");
			profit = calculateProfit(session, session.get(Asset.class, "SCL"), session.get(Asset.class, "AT"), session.get(Asset.class, "DS"), session.get(Asset.class, "RR"));
			sb.append(Util.formatNumber(profit, 6, false) + "\t\t=\t\tDaily Profitability (USDT)\n");
			sb.append(Util.formatNumber(35D / profit, 6, false) + "\t\t=\t\tProjected ROI (DAYS)\n\n");
			
			sb.append("<b>LARGE STARTER PACK</b>\n");
			sb.append(Util.formatNumber(50D, 6, false) + "\t\t=\t\tTotal Investment (USDT)\n");
			profit = calculateProfit(session, session.get(Asset.class, "SCL"), session.get(Asset.class, "AT"), session.get(Asset.class, "SW"),
					session.get(Asset.class, "DS"), session.get(Asset.class, "RR"), session.get(Asset.class, "MG"));
			sb.append(Util.formatNumber(profit, 6, false) + "\t\t=\t\tDaily Profitability (USDT)\n");
			sb.append(Util.formatNumber(50D / profit, 6, false) + "\t\t=\t\tProjected ROI (DAYS)\n\n");
			
			sb.append("<b>ALL PACKS TOGETHER</b>\n");
			sb.append(Util.formatNumber(105D, 6, false) + "\t\t=\t\tTotal Investment (USDT)\n");
			profit = calculateProfit(session, session.get(Asset.class, "SCL"), session.get(Asset.class, "DS"), session.get(Asset.class, "DS"),
					session.get(Asset.class, "SCL"), session.get(Asset.class, "AT"), session.get(Asset.class, "DS"), session.get(Asset.class, "RR"),
					session.get(Asset.class, "SCL"), session.get(Asset.class, "AT"), session.get(Asset.class, "SW"),
					session.get(Asset.class, "DS"), session.get(Asset.class, "RR"), session.get(Asset.class, "MG"));
			sb.append(Util.formatNumber(profit, 6, false) + "\t\t=\t\tDaily Profitability (USDT)\n");
			sb.append(Util.formatNumber(105D / profit, 6, false) + "\t\t=\t\tProjected ROI (DAYS)\n\n");
			
			sb.append("<i>Note: The values are calculated on current market prices and exchange rates."
					+ " So they are very volatile and may dramatically change in the future.</i>");
			
			SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
			bot.execute(msg);
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
	}
	
	private Double calculateProfit(Session session, Asset ... assets) throws GeneralException {
		Double profit = 0D;
		ProfitCalculator calc = new ProfitCalculator(session);
		Currency currency = session.get(Currency.class, "USDT");
		for (Asset asset : assets) {
			if (asset.getAssetType() == Asset.AssetType.CROPLAND) {
				Asset seed = session.get(Asset.class, "COS");
				profit += calc.calculateProfit(asset, 24, seed, currency, true);
			} else {
				profit += calc.calculateProfit(asset, 24, currency);
			}
		}
		return profit;
	}
}
