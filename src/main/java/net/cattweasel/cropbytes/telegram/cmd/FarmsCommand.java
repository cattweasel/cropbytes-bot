package net.cattweasel.cropbytes.telegram.cmd;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.object.Asset.AssetType;
import net.cattweasel.cropbytes.object.Currency;
import net.cattweasel.cropbytes.object.FiatQuote;
import net.cattweasel.cropbytes.telegram.BotCommandExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class FarmsCommand implements BotCommandExecutor {

	private static final Logger LOG = Logger.getLogger(FarmsCommand.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, String data) {
		StringBuilder sb = new StringBuilder();
		List<Farm> farms = loadFarms(session, user);
		if (farms.isEmpty()) {
			sb.append("You currently have no farms defined! Please setup a new farm first.");
		} else {
			ProfitCalculator calc = new ProfitCalculator(session);
			for (Farm farm : farms) {
				sb.append("<b>" + createKeyboardName(session, farm) + "</b>\n");
				try {
					Double balance = calc.calculateBalance(farm);
					sb.append("Balance (CBX):\t\t" + Util.formatNumber(balance, 6, true) + " / Week\n");
					Query<FiatQuote> q = session.createQuery("from FiatQuote where baseCurrency= :baseCurrency and targetCurrency= :targetCurrency");
					q.setParameter("baseCurrency", session.get(Currency.class, "CBX"));
					q.setParameter("targetCurrency", session.get(Currency.class, "USDT"));
					FiatQuote quote = q.uniqueResult();
					sb.append("Balance (USDT):\t\t" + Util.formatNumber(balance * quote.getPrice(), 6, true) + " / Week\n");
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
				sb.append("\n");
			}
		}
		SendMessage msg = new SendMessage(chatId, sb.toString()).parseMode(ParseMode.HTML);
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("Manage Farm").callbackData("farms#ManageFarm"),
				new InlineKeyboardButton("Check Balance").callbackData("farms#CheckBalance"));
		keyboard.addRow(
				new InlineKeyboardButton("Setup Farm").callbackData("farms#SetupFarm"),
				new InlineKeyboardButton("Remove Farm").callbackData("farms#RemoveFarm"));
		msg.replyMarkup(keyboard);
		bot.execute(msg);
	}
	
	@SuppressWarnings("unchecked")
	public static List<Farm> loadFarms(Session session, User user) {
		Query<Farm> query = session.createQuery("from Farm where user= :user");
		query.setParameter("user", user);
		return query.list();
	}
	
	public static String createKeyboardName(Session session, Farm farm) {
		return String.format("Farm #%s (%s Assets)", farm.getId(), countFarmAssets(session, farm));
	}
	
	@SuppressWarnings("unchecked")
	public static Integer countFarmAssets(Session session, Farm farm) {
		Integer count = 0;
		Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm");
		query.setParameter("farm", farm);
		for (FarmAsset asset : query.list()) {
			count += asset.getAmount();
		}
		return count;
	}
	
	public static void createAssetTypeSelector(TelegramBot bot, Long chatId, Integer messageId, Farm farm, String baseCallback) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select the asset type:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("ANIMAL ASSET").callbackData(baseCallback + "#" + farm.getId() + "#ANIMAL"),
				new InlineKeyboardButton("BUILDING ASSET").callbackData(baseCallback + "#" + farm.getId() + "#BUILDING"));
		keyboard.addRow(
				new InlineKeyboardButton("CROPLAND ASSET").callbackData(baseCallback + "#" + farm.getId() + "#CROPLAND"),
				new InlineKeyboardButton("TREE ASSET").callbackData(baseCallback + "#" + farm.getId() + "#TREE"));
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}

	@SuppressWarnings("unchecked")
	public static void createSeedSelector(Session session, TelegramBot bot, Long chatId,
			Integer messageId, String data, Farm farm, String baseCallback) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select the cropland seeds:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		Query<Asset> query = session.createQuery("from Asset where assetType= :assetType");
		query.setParameter("assetType", Asset.AssetType.SEED);
		Asset tempSeed = null;
		for (Asset seed : query.list()) {
			if (tempSeed == null) {
				tempSeed = seed;
			} else {
				keyboard.addRow(
						new InlineKeyboardButton(tempSeed.getName()).callbackData(baseCallback + "#" + data + "#" + tempSeed.getCode()),
						new InlineKeyboardButton(seed.getName()).callbackData(baseCallback + "#" + data + "#" + seed.getCode()));
				tempSeed = null;
			}
		}
		if (tempSeed != null) {
			keyboard.addRow(new InlineKeyboardButton(tempSeed.getName()).callbackData(baseCallback + "#" + data + "#" + tempSeed.getCode()));
		}
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}

	public static void createAmountSelector(TelegramBot bot, Long chatId, Integer messageId,
			String data, Farm farm, String baseCallback) {
		EditMessageText message = new EditMessageText(chatId, messageId, "Please select the amount to add:");
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		keyboard.addRow(
				new InlineKeyboardButton("1").callbackData(baseCallback + "#" + data + "#1"),
				new InlineKeyboardButton("2").callbackData(baseCallback + "#" + data + "#2"),
				new InlineKeyboardButton("3").callbackData(baseCallback + "#" + data + "#3"),
				new InlineKeyboardButton("4").callbackData(baseCallback + "#" + data + "#4"),
				new InlineKeyboardButton("5").callbackData(baseCallback + "#" + data + "#5"));
		keyboard.addRow(
				new InlineKeyboardButton("10").callbackData(baseCallback + "#" + data + "#10"),
				new InlineKeyboardButton("15").callbackData(baseCallback + "#" + data + "#15"),
				new InlineKeyboardButton("20").callbackData(baseCallback + "#" + data + "#20"),
				new InlineKeyboardButton("25").callbackData(baseCallback + "#" + data + "#25"),
				new InlineKeyboardButton("30").callbackData(baseCallback + "#" + data + "#30"));
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}

	@SuppressWarnings("unchecked")
	public static void createAssetSelector(Session session, TelegramBot bot, Long chatId, Integer messageId,
			String data, Farm farm, AssetType assetType, String baseCallback, boolean removal) {
		EditMessageText message = new EditMessageText(chatId, messageId, String.format(
				"Please select the %s to %s:", assetType.name().toLowerCase(), removal ? "remove" : "add"));
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
		Query<Asset> query = session.createQuery("from Asset where assetType= :assetType and proAsset= :proAsset and trialAsset= :trialAsset");
		query.setParameter("assetType", assetType);
		query.setParameter("proAsset", false);
		query.setParameter("trialAsset", false);
		Asset tempAsset = null;
		for (Asset asset : query.list()) {
			if (tempAsset == null) {
				tempAsset = asset;
			} else {
				keyboard.addRow(
						new InlineKeyboardButton(tempAsset.getName()).callbackData(baseCallback + "#" + data + "#" + tempAsset.getCode()),
						new InlineKeyboardButton(asset.getName()).callbackData(baseCallback + "#" + data + "#" + asset.getCode()));
				tempAsset = null;
			}
		}
		if (tempAsset != null) {
			keyboard.addRow(new InlineKeyboardButton(tempAsset.getName()).callbackData(baseCallback + "#" + data + "#" + tempAsset.getCode()));
		}
		keyboard.addRow(new InlineKeyboardButton("<< Back to Farm").callbackData("farms#ManageFarm#" + farm.getId()));
		message.replyMarkup(keyboard);
		bot.execute(message);
	}
	
	@SuppressWarnings("unchecked")
	public static void createFarmSelector(Session session, TelegramBot bot, User user, Long chatId,
			Integer messageId, String headline, String baseCallback) {
		Query<Farm> query = session.createQuery("from Farm where user= :user");
		query.setParameter("user", user);
		List<Farm> farms = query.list();
		if (!farms.isEmpty()) {
			EditMessageText message = new EditMessageText(chatId, messageId, headline);
			InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
			Farm tempFarm = null;
			String tempName = null;
			for (Farm farm : farms) {
				if (tempFarm == null) {
					tempFarm = farm;
					tempName = FarmsCommand.createKeyboardName(session, farm);
				} else {
					keyboard.addRow(
							new InlineKeyboardButton(tempName).callbackData(baseCallback + "#" + tempFarm.getId()),
							new InlineKeyboardButton(FarmsCommand.createKeyboardName(session, farm))
								.callbackData(baseCallback + "#" + farm.getId()));
					tempFarm = null;
				}
			}
			if (tempFarm != null) {
				keyboard.addRow(new InlineKeyboardButton(tempName).callbackData(baseCallback + "#" + tempFarm.getId()));
			}
			keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
			message.replyMarkup(keyboard);
			bot.execute(message);
		}
	}
}
