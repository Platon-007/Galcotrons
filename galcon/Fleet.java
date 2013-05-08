package galcon;

import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 13:38:20
 * To change this template use File | Settings | File Templates.
 */
public class Fleet {
		public int id;
		public int x;
		public int y;
		public Planet destPlanet;
		public int idPlayer;

		public LinkedList<Ship> ships = new LinkedList<Ship>();

	public Fleet(int id, int x, int y, Planet destPlanet, int idPlayer) {
			this.id = id;
			this.x = x;
			this.y = y;
			this.destPlanet = destPlanet;
			this.idPlayer = idPlayer;
		}


	}
