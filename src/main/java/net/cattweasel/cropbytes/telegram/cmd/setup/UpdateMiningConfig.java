package net.cattweasel.cropbytes.telegram.cmd.setup;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;

public class UpdateMiningConfig implements CallbackExecutor {

	@Override
	public String getBaseCallback() {
		return "setup#UpdateMiningConfig";
	}
	
	@Override
	public void execute(Session session, TelegramBot bot, Map<Long, CallbackExecutor> callbackCache,
			User user, Long chatId, Integer messageId, String data) {
		String[] parts = data.split("#");
		if (parts.length == 1) {
			EditMessageText message = new EditMessageText(chatId, messageId, String.format(
					"Please enter the new mining config in the following format:%n%n"
					+ "[ASSET CODE]\t\t;\t\t[REQUIRED PMIX]\t\t;\t\t[REQUIRED CBX]%n%n"
					+ "<u>Example</u>: ORT;276.58;1046.75%n%n"
					+ "<i>Note: You can also provide multiple entries within one message (one line per entry).</i>"))
					.parseMode(ParseMode.HTML);
			bot.execute(message);
			callbackCache.put(user.getUserId(), this);
		} else if (parts.length == 3) {
			Transaction tx = session.beginTransaction();
			try {
				String[] entries = parts[2].split("\n");
				for (int i=0; i<entries.length; i++) {
					updateMiningConfig(session, entries[i]);
				}
				SendMessage message = new SendMessage(chatId, "Mining configuration successfully updated!\nCheck /assets and /mining commands to verify results.");
				bot.execute(message);
				tx.commit();
			} catch (GeneralException ex) {
				SendMessage message = new SendMessage(chatId, String.format("<b>ERROR:</b>\t\t" + ex.getMessage())).parseMode(ParseMode.HTML);
				bot.execute(message);
				tx.rollback();
			}
		}
	}

	private void updateMiningConfig(Session session, String data) throws GeneralException {
		String[] parts = data.split(";");
		Asset asset = session.get(Asset.class, parts[0]);
		if (asset == null) {
			throw new GeneralException("Asset could not be found: " + parts[0]);
		}
		if (!asset.isMineable()) {
			throw new GeneralException("Asset is not mineable: " + parts[0]);
		}
		Double requiredPmix;
		try {
			requiredPmix = Double.valueOf(parts[1]);
		} catch (Exception ex) {
			throw new GeneralException("Missing or invalid [REQUIRED PMIX] for asset: " + parts[0]);
		}
		Double requiredCbx;
		try {
			requiredCbx = Double.valueOf(parts[2]);
		} catch (Exception ex) {
			throw new GeneralException("Missing or invalid [REQUIRED CBX] for asset: " + parts[0]);
		}
		MarketDataProvider provider = new MarketDataProvider(session);
		FiatQuote quote = provider.provideFiatQuote(session.get(Currency.class, "CBX"), session.get(Currency.class, "USDT"));
		asset.setMiningProMix(requiredPmix);
		asset.setMiningFees(requiredCbx);
		asset.setMiningRatio(quote.getPrice());
		session.save(asset);
	}
}
