package net.cattweasel.cropbytes.telegram.cmd;

import java.util.Date;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.AuditEvent;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class StatsCommand implements BotCommandExecutor {

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		Date timestamp1 = new Date(new Date().getTime() - 86400000L);
		Date timestamp2 = new Date(new Date().getTime() - 604800000L);
		sb1.append("<b>Bot Statistics (24 hours)</b>\n");
		sb2.append("<b>Bot Statistics (7 days)</b>\n");
		
		Query<Long> query = session.createQuery("select count(*) from User where lastSeen>= :lastSeen");
		query.setParameter("lastSeen", timestamp1);
		sb1.append(formatNumber(query.uniqueResult()) + "\t\tActive Users\n");
		query.setParameter("lastSeen", timestamp2);
		sb2.append(formatNumber(query.uniqueResult()) + "\t\tActive Users\n");
		
		query = session.createQuery("select count(*) from AuditEvent where action= :action and timestamp>= :timestamp");
		query.setParameter("action", AuditEvent.AuditAction.SEND_ALERT_NOTIFICATION);
		query.setParameter("timestamp", timestamp1);
		sb1.append(formatNumber(query.uniqueResult()) + "\t\tMarket Price Alerts\n");
		query.setParameter("timestamp", timestamp2);
		sb2.append(formatNumber(query.uniqueResult()) + "\t\tMarket Price Alerts\n");
		
		query.setParameter("action", AuditEvent.AuditAction.EXECUTE_BOT_COMMAND);
		query.setParameter("timestamp", timestamp1);
		sb1.append(formatNumber(query.uniqueResult()) + "\t\tBot Commands\n");
		query.setParameter("timestamp", timestamp2);
		sb2.append(formatNumber(query.uniqueResult()) + "\t\tBot Commands\n");
		
		query.setParameter("action", AuditEvent.AuditAction.EXECUTE_CALLBACK);
		query.setParameter("timestamp", timestamp1);
		sb1.append(formatNumber(query.uniqueResult()) + "\t\tReply Commands\n");
		query.setParameter("timestamp", timestamp2);
		sb2.append(formatNumber(query.uniqueResult()) + "\t\tReply Commands\n");
		
		SendMessage msg = new SendMessage(chatId, sb1.toString() + "\n" + sb2.toString()).parseMode(ParseMode.HTML);
		bot.execute(msg);
	}
	
	private String formatNumber(Long number) {
		String result = number.toString();
		while (result.length() < 5) {
			result = "0" + result;
		}
		return result;
	}
}
