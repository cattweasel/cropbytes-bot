package net.cattweasel.cropbytes.telegram.cmd;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class SettingsCommand implements BotCommandExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		StringBuilder sb = new StringBuilder();
		sb.append("<b>Sleep Mode = " + (user.isSleepMode() ? "enabled" : "disabled") + "</b>\n");
		sb.append("If sleep mode is enabled you won't get any automatically generated messages like price alerts.\n\n");
		sb.append("<b>Broadcast Notifications = " + (user.isBroadcastDisabled() ? "disabled" : "enabled") + "</b>\n");
		sb.append("If broadcasts are disabled you won't receive any further broadcast notifications.");
		SendMessage message = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("Toggle Sleep Mode").callbackData("settings#ToggleSleepMode"),
				new InlineKeyboardButton("Toggle Broadcast").callbackData("settings#ToggleBroadcast"));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
