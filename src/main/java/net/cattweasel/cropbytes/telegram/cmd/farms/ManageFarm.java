package net.cattweasel.cropbytes.telegram.cmd.farms;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.FarmsCommand;

public class ManageFarm implements CallbackExecutor {

	private static final String BASE_CALLBACK = "farms#ManageFarm";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data)) {
			FarmsCommand.createFarmSelector(session, bot, user, chatId, messageId,
					"Please select the farm to be managed:", BASE_CALLBACK);
		} else {
			String[] parts = data.split("#");
			if (parts.length == 1) {
				Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
				StringBuilder sb = new StringBuilder();
				sb.append("<b>Farm #" + farm.getId() + "</b> (" + FarmsCommand.countFarmAssets(session, farm) + " Assets)\n\n");
				sb.append("<b>Grazing Mode:</b> " + (farm.isGrazingMode() ? "enabled" : "disabled") + "\n");
				sb.append("<b>Grinding Fees:</b> " + (farm.isGrindingFees() ? "enabled" : "disabled") + "\n\n");
				printFarmOverview(session, farm, sb);
				EditMessageText message = new EditMessageText(chatId, messageId, sb.toString()).parseMode(ParseMode.HTML);
				InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
				keyboard.addRow(
						new InlineKeyboardButton("Add Asset(s)").callbackData("farms#AddAsset#" + farm.getId()),
						new InlineKeyboardButton("Remove Asset(s)").callbackData("farms#RemoveAsset#" + farm.getId()));
				keyboard.addRow(
						new InlineKeyboardButton("Toogle Grazing Mode").callbackData("farms#ToggleGrazingMode#" + farm.getId()),
						new InlineKeyboardButton("Toggle Grinding Fees").callbackData("farms#ToggleGrindingFees#" + farm.getId()));
				keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
				message.replyMarkup(keyboard);
				bot.execute(message);
			}
		}
	}
	
	private void printFarmOverview(Session session, Farm farm, StringBuilder sb) {
		printFarmOverview(session, farm, sb, Asset.AssetType.ANIMAL);
		printFarmOverview(session, farm, sb, Asset.AssetType.BUILDING);
		printFarmOverview(session, farm, sb, Asset.AssetType.CROPLAND);
		printFarmOverview(session, farm, sb, Asset.AssetType.TREE);
	}

	@SuppressWarnings("unchecked")
	private void printFarmOverview(Session session, Farm farm, StringBuilder sb, AssetType type) {
		sb.append(String.format("<b>%s ASSETS</b>%n", type));
		Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target.assetType= :assetType");
		query.setParameter("farm", farm);
		query.setParameter("assetType", type);
		List<FarmAsset> assets = query.list();
		if (assets.isEmpty()) {
			sb.append("n/a\n");
		} else {
			for (FarmAsset asset : assets) {
				if (Asset.AssetType.CROPLAND == asset.getTarget().getAssetType()) {
					sb.append(String.format("%s (%s) x %s%n", asset.getTarget().getName(),
							asset.getSeeds().getName(), asset.getAmount()));
				} else {
					sb.append(String.format("%s x %s%n", asset.getTarget().getName(), asset.getAmount()));
				}
			}
		}
		sb.append("\n");
	}
}
