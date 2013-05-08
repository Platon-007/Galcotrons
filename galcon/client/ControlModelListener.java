package galcon.client;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 24.04.2013
 * Time: 20:47:35
 * To change this template use File | Settings | File Templates.
 */
public interface ControlModelListener {
	void messageChangeFleetDirection(int idFleet, int idPlanet) throws IOException;
	void messageShipsFromPlanet(int idFrom, int idTo, int amount) throws IOException;
}
