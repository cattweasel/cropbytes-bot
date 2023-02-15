package net.cattweasel.cropbytes.telegram.cmd.farms;

import java.util.Map;

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

public class RemoveAsset implements CallbackExecutor {

	@Override
	public String getBaseCallback() {
		return "farms#RemoveAsset";
	}
	
	@Override
	public void execute(Session session, TelegramBot bot, Map<Long, CallbackExecutor> callbackCache,
			User user, Long chatId, Integer messageId, String data) {
		String[] parts = data.split("#");
		Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
		if (parts.length == 1) {
			FarmsCommand.createAssetTypeSelector(bot, chatId, messageId, farm, "farms#RemoveAsset");
		} else if (parts.length == 2) {
			Asset.AssetType assetType = Asset.AssetType.valueOf(parts[1]);
			FarmsCommand.createAssetSelector(session, bot, chatId, messageId, data, farm, assetType, getBaseCallback(), true);
		} else if (parts.length == 3) {
			FarmsCommand.createAmountSelector(bot, chatId, messageId, data, farm, getBaseCallback());
		} else if (parts.length == 4) {
			Asset.AssetType assetType = Asset.AssetType.valueOf(parts[1]);
			if (Asset.AssetType.CROPLAND == assetType) {
				FarmsCommand.createSeedSelector(session, bot, chatId, messageId, data, farm, getBaseCallback());
			} else {
				Asset target = session.get(Asset.class, parts[2]);
				Integer amount = Integer.valueOf(parts[3]);
				removeAsset(session, bot, chatId, messageId, farm, target, amount, null);
			}
		} else if (parts.length == 5) {
			Asset target = session.get(Asset.class, parts[2]);
			Integer amount = Integer.valueOf(parts[3]);
			Asset seeds = session.get(Asset.class, parts[4]);
			removeAsset(session, bot, chatId, messageId, farm, target, amount, seeds);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void removeAsset(Session session, TelegramBot bot, Long chatId,
			Integer messageId, Farm farm, Asset target, Integer amount, Asset seeds) {
		Transaction tx = session.beginTransaction();
		Query<FarmAsset> query = null;
		if (Asset.AssetType.CROPLAND == target.getAssetType()) {
			query = session.createQuery("from FarmAsset where farm= :farm and target= :target and seeds= :seeds");
			query.setParameter("farm", farm);
			query.setParameter("target", target);
			query.setParameter("seeds", seeds);
		} else {
			query = session.createQuery("from FarmAsset where farm= :farm and target= :target");
			query.setParameter("farm", farm);
			query.setParameter("target", target);
		}
		FarmAsset asset = query.uniqueResult();
		if (asset != null) {
			asset.setAmount(asset.getAmount() - amount);
			if (asset.getAmount() < 0) asset.setAmount(0);
			if (asset.getAmount() == 0) {
				session.remove(asset);
			} else {
				session.save(asset);
			}
		}
		tx.commit();
		EditMessageText message = new EditMessageText(chatId, messageId, "Asset was removed sucessfully!");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
}
