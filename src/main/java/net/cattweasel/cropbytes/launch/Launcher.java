package net.cattweasel.cropbytes.launch;

import com.pengrad.telegrambot.TelegramBot;

import net.cattweasel.cropbytes.task.AlertNotificationTask;
import net.cattweasel.cropbytes.task.MarketDataRefreshTask;
import net.cattweasel.cropbytes.telegram.UpdateListener;

public class Launcher {

	public static void main(String[] args) {
		
		String telegramBotApiKey = System.getProperty("TELEGRAM_BOT_API_KEY");
		if (telegramBotApiKey == null || "".equals(telegramBotApiKey.trim())) {
			System.err.println("TELEGRAM_BOT_API_KEY VARIABLE NOT SET - SHUTTING DOWN");
			System.exit(1);
		}
		
		TelegramBot bot = new TelegramBot(telegramBotApiKey);
		bot.setUpdatesListener(new UpdateListener(bot));
		
		new MarketDataRefreshTask().start();
		
		new AlertNotificationTask(bot).start();
		
	}
}
