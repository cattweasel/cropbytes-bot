package net.cattweasel.cropbytes.telegram.cmd.farms;

import org.apache.log4j.Logger;
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
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.ProfitCalculator;
import net.cattweasel.cropbytes.tools.Util;

public class CheckBalance implements CallbackExecutor {

	private static final Logger LOG = Logger.getLogger(CheckBalance.class);
	
	private static final String BASE_CALLBACK = "farms#CheckBalance";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		if (data == null || "".equals(data)) {
			FarmsCommand.createFarmSelector(session, bot, user, chatId, messageId,
					"Please select the farm for calculation:", BASE_CALLBACK);
		} else {
			String[] parts = data.split("#");
			if (parts.length == 1) {
				try {
					ProfitCalculator calc = new ProfitCalculator(session);
					Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
					StringBuilder sb = new StringBuilder();
					
					sb.append("<b>ANIMAL ASSETS (" + countAssets(session, farm, Asset.AssetType.ANIMAL) + ")</b>\n");
					if (!farm.isGrazingMode()) {
						printAssetBalance(session, farm, sb, calc, Asset.AssetType.ANIMAL);
					} else {
						printGrazingFees(farm, sb);
					}
					
					sb.append("<b>BUILDING ASSETS (" + countAssets(session, farm, Asset.AssetType.BUILDING) + ")</b>\n");
					printAssetBalance(session, farm, sb, calc, Asset.AssetType.BUILDING);
					
					sb.append("<b>CROPLAND ASSETS (" + countAssets(session, farm, Asset.AssetType.CROPLAND) + ")</b>\n");
					printAssetBalance(session, farm, sb, calc, Asset.AssetType.CROPLAND);
					
					sb.append("<b>TREE ASSETS (" + countAssets(session, farm, Asset.AssetType.TREE) + ")</b>\n");
					printAssetBalance(session, farm, sb, calc, Asset.AssetType.TREE);
					
					sb.append("<b>TOTAL ASSETS (" + countAssets(session, farm, Asset.AssetType.ANIMAL,
							Asset.AssetType.BUILDING, Asset.AssetType.CROPLAND, Asset.AssetType.TREE) + ")</b>\n");
					printAssetBalance(session, farm, sb, calc, Asset.AssetType.ANIMAL,
							Asset.AssetType.BUILDING, Asset.AssetType.CROPLAND, Asset.AssetType.TREE);
					
					sb.append("<i>Note: All prices are displayed in CBX (weekly projection)</i>");
					
					EditMessageText message = new EditMessageText(chatId, messageId, sb.toString()).parseMode(ParseMode.HTML);
					InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
					keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
					message.replyMarkup(keyboard);
					bot.execute(message);
				} catch (GeneralException ex) {
					LOG.error(ex);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Long countAssets(Session session, Farm farm, AssetType ... assetTypes) {
		Long result = 0L;
		for (AssetType assetType : assetTypes) {
			Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target.assetType= :assetType");
			query.setParameter("farm", farm);
			query.setParameter("assetType", assetType);
			for (FarmAsset asset : query.list()) {
				result += asset.getAmount();
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void printAssetBalance(Session session, Farm farm, StringBuilder sb,
			ProfitCalculator calc, AssetType ... assetTypes) throws GeneralException {
		Double extracts = 0D;
		Double requirements = 0D;
		for (AssetType assetType : assetTypes) {
			Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm and target.assetType= :assetType");
			query.setParameter("farm", farm);
			query.setParameter("assetType", assetType);
			for (FarmAsset asset : query.list()) {
				if (Asset.AssetType.ANIMAL == asset.getTarget().getAssetType() && farm.isGrazingMode()) {
					requirements += asset.getTarget().getGrazingFees() * asset.getAmount();
				} else {
					extracts += calc.calculateExtracts(asset.getTarget(), asset.getSeeds(), farm.isGrindingFees()) * asset.getAmount() / 24D * 168D;
					requirements += calc.calculateRequirements(asset.getTarget(), asset.getSeeds()) * asset.getAmount() / 24D * 168D;
				}
			}
		}
		sb.append(Util.formatNumber(requirements * -1D, 8) + "\t\t=\t\tTotal Requirements\n");
		sb.append(Util.formatNumber(extracts, 8) + "\t\t=\t\tTotal Extracts\n");
		sb.append(Util.formatNumber(extracts - requirements, 8) + "\t\t=\t\tResulting Balance\n\n");
	}
	
	private void printGrazingFees(Farm farm, StringBuilder sb) {
		Double fees = 0D;
		for (FarmAsset asset : farm.getFarmAssets()) {
			if (Asset.AssetType.ANIMAL == asset.getTarget().getAssetType()) {
				fees -= asset.getTarget().getGrazingFees() * asset.getAmount();
			}
		}
		sb.append(Util.formatNumber(fees, 8) + "\t\t=\t\tGrazing Fees\n\n");
	}
}
