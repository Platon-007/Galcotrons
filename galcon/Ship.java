package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 13:38:51
 * To change this template use File | Settings | File Templates.
 */
public class Ship {
	public int id;
	public double x;
	public double y;
	public double direction;
	public int idFleet;
	public int amount;

	public Ship(int id, double x, double y, double direction, int idFleet, int amount) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.idFleet = idFleet;
		this.amount = amount;
	}
}
