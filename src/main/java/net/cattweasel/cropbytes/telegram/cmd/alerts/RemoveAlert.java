package net.cattweasel.cropbytes.telegram.cmd.alerts;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.telegram.Alert;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class RemoveAlert implements CallbackExecutor {
	
	@Override
	public String getBaseCallback() {
		return "alerts#RemoveAlert";
	}

	@Override
	public void execute(Session session, TelegramBot bot, Map<Long, CallbackExecutor> callbackCache,
			User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data.trim())) {
			createRemoveDialog(session, bot, user, chatId, messageId);
		} else {
			String[] parts = data.split("#");
			if (parts.length == 1) {
				createConfirmDialog(bot, chatId, messageId, session.get(Alert.class, Integer.valueOf(parts[0])));
			} else if ("CONFIRM".equals(parts[1])) {
				deleteAlert(session, bot, chatId, messageId, session.get(Alert.class, Integer.valueOf(parts[0])));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createRemoveDialog(Session session, TelegramBot bot, User user, Long chatId, Integer messageId) {
		Query<Alert> query = session.createQuery("from Alert where user= :user");
		query.setParameter("user", user);
		List<Alert> alerts = query.list();
		if (!alerts.isEmpty()) {
			EditMessageText message = new EditMessageText(chatId, messageId, "Please select the alert to be removed:");
			InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
			Alert tempAlert = null;
			String tempName = null;
			for (Alert alert : alerts) {
				String name = createKeyboardName(alert);
				if (tempAlert == null) {
					tempAlert = alert;
					tempName = name;
				} else {
					keyboard.addRow(
							new InlineKeyboardButton(tempName).callbackData(getBaseCallback() + "#" + tempAlert.getId()),
							new InlineKeyboardButton(name).callbackData(getBaseCallback() + "#" + alert.getId()));
					tempAlert = null;
				}
			}
			if (tempAlert != null) {
				keyboard.addRow(new InlineKeyboardButton(tempName).callbackData(getBaseCallback() + "#" + tempAlert.getId()));
			}
			keyboard.addRow(new InlineKeyboardButton("<< Back to Alerts").callbackData("/alerts"));
			message.replyMarkup(keyboard);
			bot.execute(message);
		}
	}
	
	private void createConfirmDialog(TelegramBot bot, Long chatId, Integer messageId, Alert alert) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Delete Alert " + createKeyboardName(alert) + " ?");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("<< Back to Alerts").callbackData("/alerts"),
				new InlineKeyboardButton("YES").callbackData(getBaseCallback() + "#" + alert.getId() + "#CONFIRM"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	private void deleteAlert(Session session, TelegramBot bot, Long chatId, Integer messageId, Alert alert) {
		Transaction tx = session.beginTransaction();
		session.remove(alert);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Alert has been removed successfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Alerts").callbackData("/alerts"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	private String createKeyboardName(Alert alert) {
		String name = null;
		if (alert.getCustomAsset() != null) {
			name = alert.getCustomAsset().getCode() + " / CBX";
		} else if (alert.getCustomCurrency() != null) {
			name = alert.getCustomCurrency().getCode() + " / CBX";
		} else if (alert.isAllAssets()) {
			name = "All Assets";
		} else {
			name = "All Currencies";
		}
		name += " " + alert.getFactor() + "%";
		return name;
	}
}
