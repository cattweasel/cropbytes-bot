package net.cattweasel.cropbytes.task;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.tools.GeneralException;
import net.cattweasel.cropbytes.tools.HibernateUtil;
import net.cattweasel.cropbytes.tools.Util;

public class GameAssetRefreshTask extends Thread {

	private static final Logger LOG = Logger.getLogger(GameAssetRefreshTask.class);
	
	private final Session session;
	
	public GameAssetRefreshTask() {
		this.session = HibernateUtil.openSession();
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				refreshGameAssets();
			} catch (Exception ex) {
				LOG.error(ex);
			}
			try {
				Thread.sleep(3600000L);
			} catch (InterruptedException ex) {
				LOG.error(ex);
			}
		}
	}

	private void refreshGameAssets() throws JSONException, IOException, GeneralException {
		Transaction tx = session.beginTransaction();
		JSONObject json = new JSONObject(Util.readStringFromURL("https://api.cropbytes.com/api/v1/game/launch"));
		JSONArray assets = json.getJSONObject("data").getJSONArray("feedConfigNew");
		for (int i=0; i<assets.length(); i++) {
			JSONObject current = assets.getJSONObject(i);
			String code = current.getString("assetId").toUpperCase();
			Asset asset = session.get(Asset.class, code);
			if (asset != null) {
				Integer duration = current.getInt("extractTime") * 24;
				asset.setDuration(duration);
				Double appetiteLevel = current.getDouble("netWorkDifficulty");
				asset.setAppetiteLevel(appetiteLevel);
				if (current.getJSONObject("takes").has("hibernation_fee")) {
					Double grazingFees = current.getJSONObject("takes").getDouble("hibernation_fee");
					asset.setGrazingFees(grazingFees);
				}
				session.save(asset);
			}
		}
		tx.commit();
	}
}
