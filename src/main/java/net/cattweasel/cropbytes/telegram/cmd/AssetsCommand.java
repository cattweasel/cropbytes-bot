package net.cattweasel.cropbytes.telegram.cmd;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.object.Extract;
import net.cattweasel.cropbytes.object.Requirement;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.User;

public class AssetsCommand implements BotCommandExecutor {

	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		sendAssetConfiguration(session, bot, chatId, Asset.AssetType.ANIMAL);
		sendAssetConfiguration(session, bot, chatId, Asset.AssetType.BUILDING);
		sendAssetConfiguration(session, bot, chatId, Asset.AssetType.CROPLAND);
		sendAssetConfiguration(session, bot, chatId, Asset.AssetType.TREE);
	}

	private void sendAssetConfiguration(Session session, TelegramBot bot, Long originChatId, AssetType assetType) {
		StringBuilder sb = new StringBuilder();
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
						if (!"[".equals(duration)) sbb.append(", ");
						sbb.append(r.getTarget().getDuration() + "h");
					}
				}
				sbb.append("]");
				duration = sbb.toString();
			}
			sb.append(String.format("<b>%s\t-\t%s %s</b>%n", asset.getCode(), asset.getName(), duration));
			sb.append(String.format("<i>Takes:\t\t%s</i>%n", printRequirements(asset.getRequirements())));
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
