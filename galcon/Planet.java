package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 13:37:13
 * To change this template use File | Settings | File Templates.
 */
public class Planet {
	public int id;
	public int x;
	public int y;
	public int power;
	public int idPlayer;
	public double amount;

	public Planet(int id, int x, int y, int power, int idPlayer, double amount) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.power = power;
		this.idPlayer = idPlayer;
		this.amount = amount;
	}
}