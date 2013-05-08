package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 1:07:41
 * To change this template use File | Settings | File Templates.
 */
public interface PartyEventListener {
	public void sendFromPlanet(int tick, int idFleet, int idFrom, int idTo, int amount, double amountLeft, double toAmountLeft);
	public void changeFleetDirection(int tick, int idFleet, int idTo);
	public void planetOwned(int tick, int idPlanet, int idPlayer, double amount);

	public void playerRemoved(int idPlayer);

	public static final class Adapter implements PartyEventListener {
		public void sendFromPlanet(int tick, int idFleet, int idFrom, int idTo, int amount, double amountLeft, double toAmountLeft) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void changeFleetDirection(int tick, int idFleet, int idTo) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void planetOwned(int tick, int idPlanet, int idPlayer, double amount) {
			//To change body of implemented methods use File | Settings | File Templates.
		}

		public void playerRemoved(int idPlayer) {
			//To change body of implemented methods use File | Settings | File Templates.
		}
	}
}
