package net.cattweasel.cropbytes.telegram;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;

public interface BotCommandExecutor {

	void execute(Session session, TelegramBot bot, User user, Long chatId, String data);
}
