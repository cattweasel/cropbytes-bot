package net.cattweasel.cropbytes.telegram.cmd.alerts;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.telegram.Alert;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class SetupAlert implements CallbackExecutor {

	private static final Logger LOG = Logger.getLogger(SetupAlert.class);
	
	private static final String BASE_CALLBACK = "alerts#SetupAlert";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data.trim())) {
			createSetupDialog(bot, chatId, messageId);
		} else {
			String[] parts = data.split("#");
			if ("CUSTOM_CURRENCY".equals(parts[0])) {
				if (parts.length == 1) {
					createCurrencySelector(session, bot, chatId, messageId, data);
				} else if (parts.length == 2) {
					createFactorSelector(bot, chatId, messageId, data);
				} else {
					setupAlert(session, bot, chatId, messageId, user, Double.valueOf(parts[2]),
							false, false, null, session.get(Currency.class, parts[1]));
				}
			} else if ("CUSTOM_ASSET".equals(parts[0])) {
				if (parts.length == 1) {
					createAssetSelector(session, bot, chatId, messageId, data);
				} else if (parts.length == 2) {
					createFactorSelector(bot, chatId, messageId, data);
				} else {
					setupAlert(session, bot, chatId, messageId, user, Double.valueOf(parts[2]),
							false, false, session.get(Asset.class, parts[1]), null);
				}
			} else if ("ALL_CURRENCIES".equals(parts[0])) {
				if (parts.length == 1) {
					createFactorSelector(bot, chatId, messageId, data);
				} else {
					setupAlert(session, bot, chatId, messageId, user, Double.valueOf(parts[1]),
							false, true, null, null);
				}
			} else if ("ALL_ASSETS".equals(parts[0])) {
				if (parts.length == 1) {
					createFactorSelector(bot, chatId, messageId, data);
				} else {
					setupAlert(session, bot, chatId, messageId, user, Double.valueOf(parts[1]),
							true, false, null, null);
				}
			} else {
				LOG.error("Unknown Action for SetupAlert: " + parts[0]);
			}
		}
	}

	private void createSetupDialog(TelegramBot bot, Long chatId, Integer messageId) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select an asset pair to be observed:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("All Currencies").callbackData(BASE_CALLBACK + "#ALL_CURRENCIES"),
				new InlineKeyboardButton("Custom Currency").callbackData(BASE_CALLBACK + "#CUSTOM_CURRENCY"));
		keyboard.addRow(
				new InlineKeyboardButton("All Assets").callbackData(BASE_CALLBACK + "#ALL_ASSETS"),
				new InlineKeyboardButton("Custom Asset").callbackData(BASE_CALLBACK + "#CUSTOM_ASSET"));
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alerts").callbackData("/alerts"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	@SuppressWarnings("unchecked")
	private void createAssetSelector(Session session, TelegramBot bot, Long chatId, Integer messageId, String data) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select your custom asset:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		Asset tempAsset1 = null;
		Asset tempAsset2 = null;
		Query<Asset> query = session.createQuery("from Asset where assetType!= :assetType and proAsset= :proAsset and trialAsset= :trialAsset and tradeable= :tradeable");
		query.setParameter("assetType", Asset.AssetType.SEED);
		query.setParameter("proAsset", false);
		query.setParameter("trialAsset", false);
		query.setParameter("tradeable", true);
		for (Asset asset : query.list()) {
			if (tempAsset1 == null) {
				tempAsset1 = asset;
			} else if (tempAsset2 == null) {
				tempAsset2 = asset;
			} else {
				keyboard.addRow(
						new InlineKeyboardButton(tempAsset1.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + tempAsset1.getCode()),
						new InlineKeyboardButton(tempAsset2.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + tempAsset2.getCode()),
						new InlineKeyboardButton(asset.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + asset.getCode()));
				tempAsset1 = null;
				tempAsset2 = null;
			}
		}
		if (tempAsset1 != null) {
			if (tempAsset2 == null) {
				keyboard.addRow(new InlineKeyboardButton(tempAsset1.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + tempAsset1.getCode()));
			} else {
				keyboard.addRow(
						new InlineKeyboardButton(tempAsset1.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + tempAsset1.getCode()),
						new InlineKeyboardButton(tempAsset2.getCode() + " / CBX").callbackData(BASE_CALLBACK + "#" + data + "#" + tempAsset2.getCode()));
			}
		}
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alert Settings").callbackData(BASE_CALLBACK));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	@SuppressWarnings("unchecked")
	private void createCurrencySelector(Session session, TelegramBot bot, Long chatId, Integer messageId, String data) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select your custom currency:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		Query<Currency> query = session.createQuery("from Currency where code!= :code");
		query.setParameter("code", "CBX");
		Currency tempCurrency = null;
		for (Currency currency : query.list()) {
			if (tempCurrency == null) {
				tempCurrency = currency;
			} else {
				keyboard.addRow(
						new InlineKeyboardButton(String.format("%s / %s", "CBX",
								tempCurrency.getCode())).callbackData(BASE_CALLBACK + "#" + data + "#" + tempCurrency.getCode()),
						new InlineKeyboardButton(String.format("%s / %s", "CBX",
								currency.getCode())).callbackData(BASE_CALLBACK + "#" + data + "#" + currency.getCode()));
				tempCurrency = null;
			}
		}
		if (tempCurrency != null) {
			keyboard.addRow(new InlineKeyboardButton(String.format("%s / %s", "CBX",
					tempCurrency.getCode())).callbackData(BASE_CALLBACK + "#" + data + "#" + tempCurrency.getCode()));
		}
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alert Settings").callbackData(BASE_CALLBACK));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	private void createFactorSelector(TelegramBot bot, Long chatId, Integer messageId, String data) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select your desired change factor:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("5%").callbackData(BASE_CALLBACK + "#" + data + "#5"),
				new InlineKeyboardButton("10%").callbackData(BASE_CALLBACK + "#" + data + "#10"),
				new InlineKeyboardButton("15%").callbackData(BASE_CALLBACK + "#" + data + "#15"),
				new InlineKeyboardButton("20%").callbackData(BASE_CALLBACK + "#" + data + "#20"));
		keyboard.addRow(
				new InlineKeyboardButton("25%").callbackData(BASE_CALLBACK + "#" + data + "#25"),
				new InlineKeyboardButton("30%").callbackData(BASE_CALLBACK + "#" + data + "#30"),
				new InlineKeyboardButton("35%").callbackData(BASE_CALLBACK + "#" + data + "#35"),
				new InlineKeyboardButton("40%").callbackData(BASE_CALLBACK + "#" + data + "#40"));
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alert Settings").callbackData(BASE_CALLBACK));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	private void setupAlert(Session session, TelegramBot bot, Long chatId, Integer messageId, User user,
			Double factor, Boolean allAssets, Boolean allCurrencies, Asset customAsset, Currency customCurrency) {
		Transaction tx = session.beginTransaction();
		Alert alert = new Alert();
		alert.setAllAssets(allAssets);
		alert.setAllCurrencies(allCurrencies);
		alert.setCustomAsset(customAsset);
		alert.setCustomCurrency(customCurrency);
		alert.setFactor(factor);
		alert.setUser(user);
		session.save(alert);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Your new alert has been saved successfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alerts").callbackData("/alerts"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
