package net.cattweasel.cropbytes.telegram;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;

public interface CallbackExecutor {

	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data);
}
