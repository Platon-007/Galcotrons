package galcon.server;

import galcon.PartyEngine;
import galcon.PartyEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 21.04.2013
 * Time: 19:00:23
 * To change this template use File | Settings | File Templates.
 */
public class RoomBroadcast implements PartyEventListener {

	ArrayList<ServerPlayer> players = new ArrayList<ServerPlayer>();

	private PartyEngine engine;

	public void addPlayer(ServerPlayer p) {
		players.add(p);
		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer pp = it.next();
			try {
				p.notifyPlayerAdded(pp.player.id, pp.player.idColor);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void sendFromPlanet(int tick, int idFleet, int idFrom, int idTo, int amount, double amountLeft, double toAmountLeft) {
		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyShipsFromPlanet(tick, idFrom, idTo, amount, amountLeft, toAmountLeft);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void changeFleetDirection(int tick, int idFleet, int idTo) {
		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyChangeFleetDirection(tick, idFleet, idTo);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void planetOwned(int tick, int idPlanet, int idPlayer, double amount) {

		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyPlanetOwned(tick, idPlanet, idPlayer, amount);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void playerRemoved(int idPlayer) {

		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyPlayerLeft(idPlayer);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void gameStarted(PartyEngine e) {
		this.engine = e;
		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyGameStarted(e);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	public void gameFinished(int id) {
		Iterator<ServerPlayer> it = players.iterator();
		while (it.hasNext()) {
			ServerPlayer p = it.next();
			try {
				p.notifyGameFinished(id);
			} catch (IOException e1) {
				e1.printStackTrace();
				it.remove();
			}
		}
	}

	/*public void sendFromPlanet(int idFrom, int idTo, int amount) {
		int tick = engine.getTick();

	}*/

	/*public void fleetChangeDirection(int idFleet, int idTo) {
		int tick = engine.getTick();

	}*/

	/*public void planetOwned(int idPlanet, int idUser, int amount) {
		int tick = engine.getTick();

	}*/
}
