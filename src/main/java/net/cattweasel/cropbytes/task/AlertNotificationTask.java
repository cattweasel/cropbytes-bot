package net.cattweasel.cropbytes.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.MarketQuote;
import net.cattweasel.cropbytes.telegram.Alert;
import net.cattweasel.cropbytes.telegram.AuditEvent;
import net.cattweasel.cropbytes.tools.Auditor;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.HibernateUtil;
import net.cattweasel.cropbytes.tools.MarketDataProvider;
import net.cattweasel.cropbytes.tools.Util;

public class AlertNotificationTask extends Thread {

	private static final Logger LOG = Logger.getLogger(AlertNotificationTask.class);
	
	private final TelegramBot bot;
	
	public AlertNotificationTask(TelegramBot bot) {
		this.bot = bot;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void run() {
		while (true) {
			Session session = HibernateUtil.openSession();
			List<Long> deleteNotifications = new ArrayList<Long>();
			MarketDataProvider provider = new MarketDataProvider(session);
			Query<Alert> query = session.createQuery("from Alert");
			for (Alert alert : query.list()) {
				// delete alert if user is inactive for >= 4 weeks
				if (alert.getUser().getLastSeen().before(new Date(new Date().getTime() - 2419200000L))) {
					deleteAlert(session, alert, !deleteNotifications.contains(alert.getUser().getUserId()));
					deleteNotifications.add(alert.getUser().getUserId());
				} else {
					// skip alert if user has sleep mode enabled
					if (!alert.getUser().isSleepMode()) {
						try {
							processAlert(session, provider, alert);
						} catch (Exception ex) {
							LOG.error(ex);
						}
					}
				}
			}
			session.close();
			try {
				Thread.sleep(60000L);
			} catch (InterruptedException ex) {
				LOG.error(ex);
			}
		}
	}

	private void deleteAlert(Session session, Alert alert, Boolean sendNotification) {
		Transaction tx = session.beginTransaction();
		session.remove(alert);
		tx.commit();
		if (sendNotification) {
			SendMessage message = new SendMessage(alert.getUser().getUserId(), String.format("<b>ATTENTION</b>%n%n"
					+ "Your alert(s) has been removed because you did not interact with the bot for the last 4 weeks."
					+ " If you still want to receive alerts please use the /alerts command and setup new one(s)."))
					.parseMode(ParseMode.HTML);
			bot.execute(message);
		}
	}

	private void processAlert(Session session, MarketDataProvider provider, Alert alert) throws GeneralException {
		List<Currency> currencies = resolveCurrencies(session, alert);
		for (Currency currency : currencies) {
			FiatQuote quote = provider.provideFiatQuote(session.get(Currency.class, "CBX"), currency);
			if (quote.getPriceChange() >= alert.getFactor() || (quote.getPriceChange() * -1D) >= alert.getFactor()) {
				String data = "CBX/" + currency.getCode();
				if (shouldBeSent(session, alert, data)) {
					SendMessage message = new SendMessage(alert.getUser().getUserId(), String.format(
							"<b>CBX / %s</b> is currently at <b>%s%%</b>%n"
									+ "https://www.cropbytes.com/exchange/cbx%s%n%n"
									+ "<i>Note: You can manage these notifications with the /alerts command</i>",
							currency.getCode(), Util.formatNumber(quote.getPriceChange(), 6, true), currency.getCode().toLowerCase()))
							.disableWebPagePreview(true)
							.parseMode(ParseMode.HTML);
					bot.execute(message);
					Auditor.audit(session, alert.getUser(), AuditEvent.AuditAction.SEND_ALERT_NOTIFICATION, "alert:" + alert.getId(), data);
				}
			}
		}
		List<Asset> assets = resolveAssets(session, alert);
		for (Asset asset : assets) {
			MarketQuote quote = provider.provideMarketQuote(asset, session.get(Currency.class, "CBX"));
			if (quote.getPriceChange() >= alert.getFactor() || (quote.getPriceChange() * -1D) >= alert.getFactor()) {
				String data = asset.getCode() + "/CBX";
				if (shouldBeSent(session, alert, data)) {
					SendMessage message = new SendMessage(alert.getUser().getUserId(), String.format(
							"<b>%s</b> (%s) is currently at <b>%s%%</b>%n"
									+ "https://www.cropbytes.com/exchange/%scbx%n%n"
									+ "<i>Note: You can manage these notifications with the /alerts command</i>",
							asset.getCode(), asset.getName(), Util.formatNumber(quote.getPriceChange(), 6, true),
							asset.getCode().toLowerCase())).disableWebPagePreview(true).parseMode(ParseMode.HTML);
					bot.execute(message);
					Auditor.audit(session, alert.getUser(), AuditEvent.AuditAction.SEND_ALERT_NOTIFICATION, "alert:" + alert.getId(), data);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean shouldBeSent(Session session, Alert alert, String data) {
		Boolean result = true;
		Query<AuditEvent> query = session.createQuery("from AuditEvent where action= :action and source= :source and user= :user and data= :data order by timestamp desc");
		query.setParameter("action", AuditEvent.AuditAction.SEND_ALERT_NOTIFICATION);
		query.setParameter("source", "alert:" + alert.getId());
		query.setParameter("user", alert.getUser());
		query.setParameter("data", data);
		List<AuditEvent> events = query.list();
		if (!events.isEmpty()) {
			AuditEvent event = events.get(0);
			Date date = new Date(event.getTimestamp().getTime() + 21600000L);
			if (date.after(new Date())) {
				result = false;
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<Currency> resolveCurrencies(Session session, Alert alert) {
		List<Currency> currencies = new ArrayList<Currency>();
		if (alert.getCustomCurrency() != null) {
			currencies.add(alert.getCustomCurrency());
		} else if (alert.isAllCurrencies()) {
			Query<Currency> query = session.createQuery("from Currency where code!= :code");
			query.setParameter("code", "CBX");
			currencies.addAll(query.list());
		}
		return currencies;
	}

	@SuppressWarnings("unchecked")
	private List<Asset> resolveAssets(Session session, Alert alert) {
		List<Asset> assets = new ArrayList<Asset>();
		if (alert.getCustomAsset() != null) {
			assets.add(alert.getCustomAsset());
		} else if (alert.isAllAssets()) {
			Query<Asset> query = session.createQuery("from Asset where assetType!= :assetType and proAsset= :proAsset and trialAsset= :trialAsset and tradeable= :tradeable");
			query.setParameter("assetType", Asset.AssetType.SEED);
			query.setParameter("proAsset", false);
			query.setParameter("trialAsset", false);
			query.setParameter("tradeable", true);
			assets.addAll(query.list());
		}
		return assets;
	}
}
