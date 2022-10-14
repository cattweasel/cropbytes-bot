package net.cattweasel.cropbytes.telegram.cmd.farms;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.FarmsCommand;

public class AddAsset implements CallbackExecutor {

	private static final String BASE_CALLBACK = "farms#AddAsset";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		String[] parts = data.split("#");
		Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
		if (parts.length == 1) {
			FarmsCommand.createAssetTypeSelector(bot, chatId, messageId, farm, "farms#AddAsset");
		} else if (parts.length == 2) {
			Asset.AssetType assetType = Asset.AssetType.valueOf(parts[1]);
			FarmsCommand.createAssetSelector(session, bot, chatId, messageId, data, farm, assetType, BASE_CALLBACK);
		} else if (parts.length == 3) {
			FarmsCommand.createAmountSelector(bot, chatId, messageId, data, farm, BASE_CALLBACK);
		} else if (parts.length == 4) {
			Asset.AssetType assetType = Asset.AssetType.valueOf(parts[1]);
			if (Asset.AssetType.CROPLAND == assetType) {
				FarmsCommand.createSeedSelector(session, bot, chatId, messageId, data, farm, BASE_CALLBACK);
			} else {
				saveAsset(session, bot, chatId, messageId, farm,
						session.get(Asset.class, parts[2]),  Integer.valueOf(parts[3]), null);
			}
		} else if (parts.length == 5) {
			saveAsset(session, bot, chatId, messageId, farm,
					session.get(Asset.class, parts[2]), Integer.valueOf(parts[3]),
					session.get(Asset.class, parts[4]));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void saveAsset(Session session, TelegramBot bot, Long chatId,
			Integer messageId, Farm farm, Asset target, Integer amount, Asset seeds) {
		Transaction tx = session.beginTransaction();
		Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target= :target and seeds= :seeds");
		query.setParameter("farm", farm);
		query.setParameter("target", target);
		query.setParameter("seeds", seeds);
		FarmAsset asset = query.uniqueResult();
		asset = asset == null ? new FarmAsset() : asset;
		if (asset.getId() == null) {
			asset.setAmount(amount);
		} else {
			asset.setAmount(asset.getAmount() + amount);
		}
		asset.setFarm(farm);
		asset.setSeeds(seeds);
		asset.setTarget(target);
		session.save(asset);
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Asset was added sucessfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
