package net.cattweasel.cropbytes.telegram.cmd.farms;

import java.util.Map;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.User;

public class SetupFarm implements CallbackExecutor {

	@Override
	public String getBaseCallback() {
		return "farms#SetupFarm";
	}
	
	@Override
	public void execute(Session session, TelegramBot bot, Map<Long, CallbackExecutor> callbackCache,
			User user, Long chatId, Integer messageId, String data) {
		Transaction tx = session.beginTransaction();
		Farm farm = new Farm();
		farm.setUser(user);
		farm.setGrindingFees(true);
		farm.setGrazingMode(false);
		session.save(farm);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Your new farm has been created successfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
