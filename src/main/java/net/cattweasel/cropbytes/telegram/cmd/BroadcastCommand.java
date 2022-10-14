package net.cattweasel.cropbytes.telegram.cmd;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class BroadcastCommand implements BotCommandExecutor {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		if (data != null && !"".equals(data.trim())) {
			String msg = "<b>BROADCAST</b>\n\n" + data;
			Query<User> query = session.createQuery("from User");
			for (User u : query.list()) {
				if (!u.isBroadcastDisabled()) {
					SendMessage message = new SendMessage(u.getUserId(), msg + "\n\n<i>Note:"
							+ " You can disable broadcast notifications with the /settings command</i>")
							.parseMode(ParseMode.HTML);
					bot.execute(message);
				}
			}
		}
	}
}
