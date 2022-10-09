package net.cattweasel.cropbytes.telegram.cmd.farms;

import org.hibernate.Session;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;

import net.cattweasel.cropbytes.telegram.CallbackExecutor;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.User;
import net.cattweasel.cropbytes.telegram.cmd.FarmsCommand;
import net.cattweasel.cropbytes.tools.Util;

public class CheckBalance implements CallbackExecutor {

	private static final String BASE_CALLBACK = "farms#CheckBalance";
	
	@Override
	public void execute(Session session, TelegramBot bot, User user, Long chatId, Integer messageId, String data) {
		
		if (!"cattweasel".equals(user.getUsername())) { // TODO -> REMOVE
			EditMessageText message = new EditMessageText(chatId, messageId, "The detailled balance calculation"
					+ " for farms will be available within the next few days. Please try again later!");
			InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
			keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
			message.replyMarkup(keyboard);
			bot.execute(message);
		} else {
			
			if (data == null || "".equals(data)) {
				FarmsCommand.createFarmSelector(session, bot, user, chatId, messageId,
						"Please select the farm for calculation:", BASE_CALLBACK);
			} else {
				String[] parts = data.split("#");
				if (parts.length == 1) {
					Farm farm = session.get(Farm.class, Integer.valueOf(parts[0]));
					StringBuilder sb = new StringBuilder();
					
					
					sb.append("<b>ANIMAL ASSETS (99999)</b>\n"); // TODO
					sb.append(Util.formatNumber(99999D, 8) + " CBX\t\t=\t\tTotal Requirements\n"); // TODO
					sb.append(Util.formatNumber(99999D, 8) + " CBX\t\t=\t\tTotal Extracts\n"); // TODO
					sb.append(Util.formatNumber(99999D, 8) + " CBX\t\t=\t\tResulting Balance\n"); // TODO
					
					
					EditMessageText message = new EditMessageText(chatId, messageId, sb.toString()).parseMode(ParseMode.HTML);
					InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
					keyboard.addRow(new InlineKeyboardButton("<< Back to Farms").callbackData("/farms"));
					message.replyMarkup(keyboard);
					bot.execute(message);
				}
			}
			
		}
		
	}
}
