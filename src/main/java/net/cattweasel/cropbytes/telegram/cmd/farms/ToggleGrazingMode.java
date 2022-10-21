package net.cattweasel.cropbytes.telegram.cmd.farms;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.User;

public class ToggleGrazingMode implements CallbackExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		Transaction tx = session.beginTransaction();
		Farm farm = session.get(Farm.class, Integer.valueOf(data));
		farm.setGrazingMode(!farm.isGrazingMode());
		session.save(farm);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Grazing Mode has been "
				+ (farm.isGrazingMode() ? "enabled" : "disabled")).parseMode(ParseMode.HTML);
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + data));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
