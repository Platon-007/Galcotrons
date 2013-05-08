package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 22:02:48
 * To change this template use File | Settings | File Templates.
 */
public class Interaction {
	public static final byte SEND_SHIPS = 1;
	public static final byte FLEET_CHANGE_DIRECTION = 2;
	public static final byte GAME_START = 4;
	public static final byte GAME_END = 5;

	public static final byte NOTIFY_UPDATE_INFO = 6;
	public static final byte NOTIFY_GAME_FINISHED  = 7;
	public static final byte NOTIFY_GAME_STARTED = 8;
	public static final byte NOTIFY_SHIPS_SENT = 9;
	public static final byte NOTIFY_FLEET_CHANGE_DIRECTION = 10;
	public static final byte NOTIFY_PLANET_OWNED = 11;
	public static final byte NOTIFY_PLAYER_INSTALLED = 12;
	public static final byte NOTIFY_PLAYER_LEFT = 13;
	public static final byte UPDATE_INFO = 14;
	public static final byte NOTIFY_PLAYER_ENTERED = 15;
}
