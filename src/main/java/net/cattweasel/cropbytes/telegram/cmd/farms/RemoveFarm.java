package net.cattweasel.cropbytes.telegram.cmd.farms;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.FarmsCommand;

public class RemoveFarm implements CallbackExecutor {
	
	private static final String BASE_CALLBACK = "farms#RemoveFarm";

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data.trim())) {
			FarmsCommand.createFarmSelector(session, bot, user, chatId, messageId,
					"Please select the farm to be removed:", BASE_CALLBACK);
		} else {
			String[] parts = data.split("#");
			if (parts.length == 1) {
				createConfirmDialog(session, bot, chatId, messageId, session.get(Farm.class, Integer.valueOf(parts[0])));
			} else if ("CONFIRM".equals(parts[1])) {
				deleteFarm(session, bot, chatId, messageId, session.get(Farm.class, Integer.valueOf(parts[0])));
			}
		}
	}

	private void createConfirmDialog(Session session, TelegramBot bot, Long chatId, Integer messageId, Farm farm) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Delete Farm "
				+ FarmsCommand.createKeyboardName(session, farm) + " ?");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"),
				new InlineKeyboardButton("YES").callbackData(BASE_CALLBACK + "#" + farm.getId() + "#CONFIRM"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}

	private void deleteFarm(Session session, TelegramBot bot, Long chatId, Integer messageId, Farm farm) {
		Transaction tx = session.beginTransaction();
		Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm");
		query.setParameter("farm", farm);
		for (FarmAsset asset : query.list()) {
			session.remove(asset);
		}
		session.remove(farm);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Farm has been removed successfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
