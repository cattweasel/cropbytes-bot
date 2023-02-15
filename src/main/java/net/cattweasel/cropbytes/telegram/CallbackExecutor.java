package net.cattweasel.cropbytes.telegram;

import java.util.Map;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;

public interface CallbackExecutor {

	String getBaseCallback();
	
	void execute(Session session, TelegramBot bot, Map<Long, CallbackExecutor> callbackCache,
			User user, Long chatId, Integer messageId, String data);
}
