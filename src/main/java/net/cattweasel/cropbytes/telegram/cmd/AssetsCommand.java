package net.cattweasel.cropbytes.telegram.cmd;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.Extract;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.object.Requirement;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.MarketDataProvider;

public class AssetsCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(AssetsCommand.class);
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		try {
			sendAssetConfiguration(session, bot, chatId, Asset.AssetType.ANIMAL);
			sendAssetConfiguration(session, bot, chatId, Asset.AssetType.BUILDING);
			sendAssetConfiguration(session, bot, chatId, Asset.AssetType.CROPLAND);
			sendAssetConfiguration(session, bot, chatId, Asset.AssetType.TREE);
		} catch (GeneralException ex) {
			LOG.error(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void sendAssetConfiguration(Session session, TelegramBot bot, Long originChatId, AssetType assetType) throws GeneralException {
		StringBuilder sb = new StringBuilder();
		MarketDataProvider provider = new MarketDataProvider(session);
		FiatQuote usdtQuote = provider.provideFiatQuote(session.get(Currency.class, "CBX"), session.get(Currency.class, "USDT"));
		Query<Asset> query = session.createQuery("from Asset where assetType= :assetType and trialAsset= :trialAsset");
		query.setParameter("assetType", assetType);
		query.setParameter("trialAsset", false);
		for (Asset asset : query.list()) {
			String duration = String.format("[%sh]", asset.getDuration());
			if (Asset.AssetType.CROPLAND == assetType) {
				StringBuilder sbb = new StringBuilder();
				sbb.append("[");
				for (Requirement r : asset.getRequirements()) {
					if (Asset.AssetType.SEED == r.getTarget().getAssetType()) {
						if (sbb.length() > 1) sbb.append(", ");
						sbb.append(r.getTarget().getDuration() + "h");
					}
				}
				sbb.append("]");
				duration = sbb.toString();
			}
			sb.append(String.format("<b>%s\t-\t%s %s</b>%n", asset.getCode(), asset.getName(), duration));
			if (asset.isMineable()) {
				sb.append(String.format("<i>Mining:\t\t%s PMIX + %s CBX</i>%n", asset.getMiningProMix(),
						asset.getMiningFees() * asset.getMiningRatio() / usdtQuote.getPrice()));
			}
			if (Asset.AssetType.ANIMAL == asset.getAssetType()) {
				sb.append(String.format("<i>Takes:\t\t%s\t\t[%s]</i>%n", printRequirements(asset.getRequirements()), asset.getAppetiteLevel()));
			} else {
				sb.append(String.format("<i>Takes:\t\t%s</i>%n", printRequirements(asset.getRequirements())));
			}
			sb.append(String.format("<i>Gives:\t\t%s</i>%n%n", printExtracts(asset.getExtracts())));
		}
		SendMessage message = new SendMessage(originChatId, sb.toString()).parseMode(ParseMode.HTML);
		bot.execute(message);
	}
	
	private String printRequirements(List<Requirement> requirements) {
		StringBuilder sb = new StringBuilder();
		for (Requirement r : requirements) {
			if (!"".equals(sb.toString())) sb.append(", ");
			sb.append(r.getAmount() + " " + r.getTarget().getCode());
		}
		return sb.toString();
	}
	
	private String printExtracts(List<Extract> extracts) {
		StringBuilder sb = new StringBuilder();
		for (Extract e : extracts) {
			if (!"".equals(sb.toString())) sb.append(", ");
			sb.append(e.getAmount() + " " + e.getTarget().getCode());
		}
		return sb.toString();
	}
}
