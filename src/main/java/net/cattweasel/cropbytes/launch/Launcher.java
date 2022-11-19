package net.cattweasel.cropbytes.launch;

import org.apache.log4j.Logger;

import com.pengrad.telegrambot.TelegramBot;

import net.cattweasel.cropbytes.task.AlertNotificationTask;
import net.cattweasel.cropbytes.task.GameAssetRefreshTask;
import net.cattweasel.cropbytes.task.MarketDataRefreshTask;
import net.cattweasel.cropbytes.telegram.UpdateListener;

public class Launcher {

	private static final Logger LOG = Logger.getLogger(Launcher.class);
	
	public static void main(String[] args) {
		
		String telegramBotApiKey = System.getProperty("TELEGRAM_BOT_API_KEY");
		if (telegramBotApiKey == null || "".equals(telegramBotApiKey.trim())) {
			System.err.println("TELEGRAM_BOT_API_KEY VARIABLE NOT SET - SHUTTING DOWN");
			System.exit(1);
		}
		
		LOG.debug("Setting up telegram bot instance..");
		TelegramBot bot = new TelegramBot(telegramBotApiKey);
		bot.setUpdatesListener(new UpdateListener(bot));
		
		LOG.debug("Setting up market data refresh task..");
		new MarketDataRefreshTask().start();
		
		LOG.debug("Setting up game asset refresh task..");
		new GameAssetRefreshTask().start();
		
		LOG.debug("Setting up alert notification task..");
		new AlertNotificationTask(bot).start();
		
		LOG.debug("Bot is now up and running!");
	}
}
