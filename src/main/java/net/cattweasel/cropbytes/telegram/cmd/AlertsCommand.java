package net.cattweasel.cropbytes.telegram.cmd;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.telegram.Alert;
import net.cattweasel.cropbytes.telegram.AuditEvent;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class AlertsCommand implements BotCommandExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		StringBuilder sb = new StringBuilder();
		Query<Alert> query = session.createQuery("from Alert where user= :user");
		query.setParameter("user", user);
		List<Alert> alerts = query.list();
		if (alerts.isEmpty()) {
			sb.append("You currently have no alerts defined!");
		} else {
			for (Alert alert : query.list()) {
				sb.append("<b>Alert #" + alert.getId() + "</b>\n");
				Query<Long> q = session.createQuery("select count(*) from AuditEvent where action= :action and source= :source");
				q.setParameter("action", AuditEvent.AuditAction.SEND_ALERT_NOTIFICATION);
				q.setParameter("source", "alert:" + alert.getId());
				Long count = q.uniqueResult();
				sb.append("Notifications:\t\t" + count + "\n");
				if (alert.getCustomAsset() != null) {
					sb.append("Type:\t\tCustom Asset\n");
					sb.append("Asset:\t\t" + alert.getCustomAsset().getCode() + " / CBX\n");
				} else if (alert.getCustomCurrency() != null) {
					sb.append("Type:\t\tCustom Currency\n");
					sb.append("Currency:\t\t" + alert.getCustomCurrency().getCode() + " / CBX\n");
				} else {
					sb.append("Type:\t\t" + (alert.isAllCurrencies() ? "All Currencies" : "All Assets") + "\n");
				}
				sb.append("Factor:\t\t" + alert.getFactor() + " %\n\n");
			}
		}
		SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("Setup Alert").callbackData("alerts#SetupAlert"),
				new InlineKeyboardButton("Remove Alert").callbackData("alerts#RemoveAlert"));
		msg.replyMarkup(keyboard);
		bot.execute(msg);
	}
}
