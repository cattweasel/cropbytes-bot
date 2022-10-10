package net.cattweasel.cropbytes.telegram;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.tools.Auditor;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.HibernateUtil;

public class UpdatesListener implements com.pengrad.telegrambot.UpdatesListener {

	private static final Logger LOG = Logger.getLogger(UpdatesListener.class);
	
	private final TelegramBot bot;
	
	public UpdatesListener(TelegramBot bot) {
		this.bot = bot;
	}
	
	@Override
	public int process(List<Update> updates) {
		for (Update update : updates) {
			LOG.debug("Processing Update: " + update);
			Session session = null;
			try {
				session = HibernateUtil.openSession();
				if (update.message() != null) {
					storeUserData(session, update.message().from());
					processMessage(session, update.message());
				} else if (update.callbackQuery() != null) {
					storeUserData(session, update.callbackQuery().from());
					processCallbackQuery(session, update.callbackQuery());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				LOG.error(ex);
			} finally {
				if (session != null) {
					try {
						session.close();
					} catch (Exception ex) {
						LOG.error(ex);
					}
				}
			}
		}
		return UpdatesListener.CONFIRMED_UPDATES_ALL;
	}

	private void storeUserData(Session session, com.pengrad.telegrambot.model.User user) {
		Transaction tx = session.beginTransaction();
		net.cattweasel.cropbytes.telegram.User usr = session.get(
				net.cattweasel.cropbytes.telegram.User.class, user.id());
		if (usr == null) {
			usr = new net.cattweasel.cropbytes.telegram.User();
			usr.setUserId(user.id());
			usr.setAdmin(false);
			usr.setBroadcastDisabled(false);
			usr.setSleepMode(false);
		}
		if (user.languageCode() != null) usr.setLanguage(user.languageCode());
		if (user.username() != null) usr.setUsername(user.username());
		if (user.firstName() != null) usr.setFirstname(user.firstName());
		if (user.lastName() != null) usr.setLastname(user.lastName());
		usr.setLastSeen(new Date());
		session.save(usr);
		tx.commit();
	}

	private void processMessage(Session session, Message message) throws GeneralException {
		LOG.debug("Processing message: " + message);
		if (message.text() != null && !message.text().trim().isEmpty()) {
			if (message.text().startsWith("/")) {
				Query<BotCommand> query = session.createQuery("from BotCommand where command= :command");
				String txt = message.text();
				if (txt.contains(" ")) txt = txt.split(" ")[0];
				query.setParameter("command", txt);
				BotCommand cmd = query.uniqueResult();
				if (cmd != null) {
					User user = session.get(User.class, message.from().id());
					String data = message.text().replaceAll(cmd.getCommand(), "").trim();
					processBotCommand(session, cmd, user, message.chat().id(), data);
				} else {
					bot.execute(new SendMessage(message.chat().id(), "Unrecognized command. Use /help to get a list of all commands."));
				}
			}
		}
	}

	private void processBotCommand(Session session, BotCommand cmd, User user, Long chatId, String data) throws GeneralException {
		LOG.debug("Processing BotCommand: " + cmd);
		if (cmd.isDisabled()) {
			SendMessage message = new SendMessage(user.getUserId(), "The requested command is currently disabled! Please check again later.");
			bot.execute(message);
		} else if (cmd.isAdmin() && !user.isAdmin()) {
			SendMessage message = new SendMessage(user.getUserId(), "The requested command is only availabe for administrators!");
			bot.execute(message);
		} else if (cmd.isDevelopment() && !user.isAdmin()) {
			SendMessage message = new SendMessage(user.getUserId(), "The requested command is currently in development-mode"
					+ " and so only availabe for administrators! Please check again later.");
			bot.execute(message);
		} else {
			try {
				Class<BotCommandExecutor> cls = (Class<BotCommandExecutor>) Class.forName(cmd.getExecutor());
				BotCommandExecutor executor = cls.newInstance();
				executor.execute(session, bot, user, chatId, data);
				Auditor.audit(session, user, AuditEvent.AuditAction.EXECUTE_BOT_COMMAND, executor.getClass().getSimpleName(), data);
			} catch (Exception ex) {
				
				
				ex.printStackTrace(); // TODO -> REMOVE
				
				
				throw new GeneralException(ex);
			}
		}
	}
	
	private void processCallbackQuery(Session session, CallbackQuery callbackQuery) throws GeneralException {
		LOG.debug("Processing CallbackQuery: " + callbackQuery);
		User user = session.get(User.class, callbackQuery.from().id());
		Long chatId = callbackQuery.message().chat().id();
		Integer messageId = callbackQuery.message().messageId();
		if (callbackQuery.data().startsWith("/")) {
			DeleteMessage message = new DeleteMessage(callbackQuery.from().id(), callbackQuery.message().messageId());
			bot.execute(message);
			Query<BotCommand> query = session.createQuery("from BotCommand where command= :command");
			query.setParameter("command", callbackQuery.data());
			BotCommand cmd = query.uniqueResult();
			processBotCommand(session, cmd, user, chatId, null);
		} else {
			String[] data = callbackQuery.data().split("#");
			try {
				Class<CallbackExecutor> cls = (Class<CallbackExecutor>) Class.forName(
						"net.cattweasel.cropbytes.telegram.cmd." + data[0] + "." + data[1]);
				CallbackExecutor executor = cls.newInstance();
				String payload = callbackQuery.data().replaceAll(data[0] + "#" + data[1], "");
				payload = payload.startsWith("#") ? payload.substring(1, payload.length()) : payload;
				executor.execute(session, bot, user, chatId, messageId, payload);
				Auditor.audit(session, session.get(User.class, callbackQuery.from().id()),
						AuditEvent.AuditAction.EXECUTE_CALLBACK, executor.getClass().getSimpleName(), payload);
			} catch (Exception ex) {
				throw new GeneralException(ex);
			}
		}
	}
}
