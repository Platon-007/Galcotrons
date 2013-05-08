package galcon.client;

import galcon.GameDataMismatchException;
import galcon.PartyEngine;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 24.04.2013
 * Time: 20:50:42
 * To change this template use File | Settings | File Templates.
 */
public class ControlModelListenerAdapter implements ControlModelListener {

	private PartyEngine engine;

	public ControlModelListenerAdapter(PartyEngine engine) {
		this.engine = engine;
	}

	public void messageShipsFromPlanet(int idFrom, int idTo, int amount) throws IOException {
		try {
			engine.sendShipsFromPlanet(idFrom, idTo, amount);
		} catch (GameDataMismatchException e) {
			e.printStackTrace();
		}
	}

	public void messageChangeFleetDirection(int idFleet, int idPlanet) throws IOException {
		try {
			engine.changeFleetDirection(idFleet, idPlanet);
		} catch (GameDataMismatchException e) {
			e.printStackTrace();
		}
	}
}
