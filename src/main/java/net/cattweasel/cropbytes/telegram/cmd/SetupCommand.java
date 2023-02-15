package net.cattweasel.cropbytes.telegram.cmd;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class SetupCommand implements BotCommandExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Please choose the operation to be performed:");
		SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("Update Mining Config").callbackData("setup#UpdateMiningConfig"));
		msg.replyMarkup(keyboard);
		bot.execute(msg);
		
	}
}
