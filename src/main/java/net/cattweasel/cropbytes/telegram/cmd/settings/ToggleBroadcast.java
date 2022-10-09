package net.cattweasel.cropbytes.telegram.cmd.settings;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.DeleteMessage;

import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.SettingsCommand;

public class ToggleBroadcast implements CallbackExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		Transaction tx = session.beginTransaction();
		user.setBroadcastDisabled(!user.isBroadcastDisabled());
		session.save(user);
		tx.commit();
		DeleteMessage msg = new DeleteMessage(chatId, messageId);
		bot.execute(msg);
		SettingsCommand cmd = new SettingsCommand();
		cmd.execute(session, bot, user, chatId, data);
	}
}
