package net.cattweasel.cropbytes.tools;

import org.hibernate.Session;
import org.hibernate.query.Query;

import net.cattweasel.cropbytes.object.Asset;
import net.cattweasel.cropbytes.telegram.Farm;
import net.cattweasel.cropbytes.telegram.FarmAsset;

public class BalanceCalculator {

	private final Session session;
	private final ProfitCalculator calculator;
	
	public BalanceCalculator(Session session) {
		this.session = session;
		this.calculator = new ProfitCalculator(session);
	}
	
	public Double calculateBalance(Farm farm) throws GeneralException {
		Double balance = 0D;
		Query<FarmAsset> query = session.createQuery("from FarmAsset where farm= :farm");
		query.setParameter("farm", farm);
		for (FarmAsset asset : query.list()) {
			if (Asset.AssetType.CROPLAND == asset.getTarget().getAssetType()) {
				balance += calculator.calculateProfit(asset.getTarget(), asset.getSeeds());
			} else {
				balance += calculator.calculateProfit(asset.getTarget());
			}
		}
		return balance;
	}
}
