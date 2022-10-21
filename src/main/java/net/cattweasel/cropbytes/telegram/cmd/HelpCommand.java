package net.cattweasel.cropbytes.telegram.cmd;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class HelpCommand implements BotCommandExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		SendMessage msg = new SendMessage(chatId,
				"<b>General Informations</b>\n"
				+ "/assets - list current asset configuration\n"
				+ "/quotes - retrieve current market quotes\n"
				+ "\n"
				+ "<b>Calculation Operations</b>\n"
				+ "/farms - manage your farms for calculations\n"
				+ "/packs - calculate ROI for starter packs\n"
				+ "/profit - calculate asset profitabilities\n"
				+ "/mining - calculate mining profitabilities\n"
				+ "/grazing - calculate grazing profitabilities\n"
				+ "\n"
				+ "<b>Miscellaneous</b>\n"
				+ "/alerts - manage your market price alerts\n"
				+ "/settings - manage your global bot settings\n"
				+ "/stats - displays current bot statistics\n"
				+ "/help - displays the list of bot functions\n"
				+ "\n"
				+ "Support Chat: https://t.me/cropbytes_bot_support\n"
				+ "Source Code: https://github.com/cattweasel/cropbytes-bot\n"
				+ "\n"
				+ "<i>Note: This is no kind of financial advice! The tools are created to make your own research easy. Please let your brain always be enabled when using it ;-)</i>")
				.disableWebPagePreview(true)
				.parseMode(ParseMode.HTML);
		bot.execute(msg);
	}
}
