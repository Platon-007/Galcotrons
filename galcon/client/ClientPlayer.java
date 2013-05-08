package galcon.client;

import galcon.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 18:36:25
 * To change this template use File | Settings | File Templates.
 */
public class ClientPlayer implements ControlModelListener {

	private PartyEngine engine;

	private Socket connection;

	private DataInputStream is;

	private DataOutputStream os;

	private Player player;

	public ClientPlayer(PartyEngine engine, Socket connection) throws IOException {
		this.engine = engine;
		this.connection = connection;
		is = new DataInputStream(connection.getInputStream());
		os = new DataOutputStream(connection.getOutputStream());
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					do {
						decryptReceivedMessage(is);
					} while (true);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	private void decryptReceivedMessage(DataInputStream dis) throws IOException {
		byte command = dis.readByte();
		switch (command) {
			case Interaction.NOTIFY_FLEET_CHANGE_DIRECTION:
				decryptChangeDirection(dis);
				break;
			case Interaction.NOTIFY_SHIPS_SENT:
				decryptShipsFromPlanet(dis);
				break;
			case Interaction.NOTIFY_PLANET_OWNED:
				decryptPlanetOwned(dis);
				break;
			case Interaction.NOTIFY_UPDATE_INFO:
				decryptRequestUpdate(dis);
				break;
			case Interaction.NOTIFY_PLAYER_INSTALLED:
				decryptPlayerInstalled(dis);
				break;
			case Interaction.NOTIFY_GAME_STARTED:
				receivedGameStarted();
				break;
			case Interaction.NOTIFY_GAME_FINISHED:
				decryptGameFinished(dis);
				break;
			case Interaction.NOTIFY_PLAYER_LEFT:
				decryptPlayerLeft(dis);
				break;
			case Interaction.NOTIFY_PLAYER_ENTERED:
				decryptPlayerEntered(dis);
				break;
		}
	}

	private void decryptGameFinished(DataInputStream dis) throws IOException {
		int idPlayer = dis.readInt();
		receivedGameFinished(idPlayer);
	}

	private void receivedGameFinished(int idPlayer) {
		System.out.println("Player #" + idPlayer + " wins");
	}

	private void decryptPlayerEntered(DataInputStream dis) throws IOException {
		int idPlayer = dis.readInt();
		int idColor = dis.readByte();
		receivePlayerEntered(idPlayer, idColor);
	}

	private void receivePlayerEntered(int idPlayer, int idColor) {
		// TODO: куда-то эту информацию закинуть
	}

	private void decryptPlayerLeft(DataInputStream dis) throws IOException {
		int idPlayer = dis.readInt();
		receivedPlayerLeft(idPlayer);
	}

	private void receivedPlayerLeft(int idPlayer) {
		engine.removePlayer(idPlayer);
	}

	private void decryptPlayerInstalled(DataInputStream dis) throws IOException {
		int idUser = dis.readInt();
		int idColor = dis.readByte();
		player = new Player(idUser, idColor);
	}

	private void decryptPlanetOwned(DataInputStream dis) throws IOException {
		int tick = dis.readInt();
		int idPlanet = dis.readByte();
		int idPlayer = dis.readInt();
		double amount = dis.readDouble();

		receivedPlanetOwned(tick, idPlanet, idPlayer, amount);
	}

	private void decryptRequestUpdate(DataInputStream dis) throws IOException {
		int tick = dis.readInt();

		Player[] players = new Player[dis.readByte()];
		for (int i = 0; i < players.length; i++)
		 	players[i] = new Player(dis.readInt(), dis.readByte());


		Planet[] planets = new Planet[dis.readByte()];
		for (int i = 0; i < planets.length; i++) {
			int id = dis.readByte();
			int power = dis.readByte();
			int x = dis.readChar();
			int y = dis.readChar();
			int idPlayer = dis.readInt();
			double amount = dis.readDouble();
			planets[i] = new Planet(id, x, y, power, idPlayer, amount);
		}

		int fleetsLength = dis.readChar();
		LinkedList<Fleet> fleets = new LinkedList<Fleet>();



		for (int i = 0; i < fleetsLength; i++) {
			int idFleet = dis.readInt();
			int idPlayer = dis.readInt();
			int idPlanet = dis.readInt();
			int shipsLength = dis.readByte();
			Fleet fl = new Fleet(idFleet, 0, 0, getPlanet(planets, idPlanet), idPlayer);

			LinkedList<Ship> ships = new LinkedList<Ship>();
			for (int j = 0; j < shipsLength; j++) {
				int idShip = dis.readInt();
				double x = dis.readDouble();
				double y = dis.readDouble();
				int amount = dis.readInt();
				double direction = dis.readDouble();
				ships.add(new Ship(idShip, x, y, direction, idFleet, amount));
			}
			fl.ships = ships;
			fleets.add(fl);
		}

		receivedUpdateInfo(tick, players, planets, fleets);
	}

	private static Planet getPlanet(Planet[] planets, int idPlanet) {
		for (Planet p : planets)
			if (p.id == idPlanet)
				return p;
		return null;
	}

	private void decryptShipsFromPlanet(DataInputStream dis) throws IOException {
		int tick = dis.readInt();
		int idFrom = dis.readByte();
		int idTo = dis.readByte();
		int amount = dis.readInt();
		double fromAmountLeft = dis.readDouble();
		double toAmountLeft = dis.readDouble();
		receivedShipsFromPlanet(tick, idFrom, idTo, amount, fromAmountLeft, toAmountLeft);
	}

	private void decryptChangeDirection(DataInputStream dis) throws IOException {
		int tick = dis.readInt();
		int idFleet = dis.readInt();
		int idTo = dis.readByte();
		receivedChangeFleetDirection(tick, idFleet, idTo);
	}

	public void receivedGameStarted() {

	}

	private Timer ticker;

	public void receivedUpdateInfo(int tick, Player[] players, Planet[] planets, LinkedList<Fleet> fleets) {
		engine.updateInfo(tick, players, planets, fleets);
		if (ticker != null)
			ticker.cancel();

		ticker = new Timer();
		ticker.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				engine.doTick();
			}
		}, PartyEngine.TICK_TIME, PartyEngine.TICK_TIME);
	}

	public void receivedShipsFromPlanet(int tick, int idFrom, int idTo, int amount, double amountLeft, double toAmountLeft) throws IOException {
		try {
			engine.sendShipsFromPlanet(idFrom, idTo, amount);
		} catch (GameDataMismatchException e) {
			messageUpdateInfo();
		}
		Planet planet = engine.getPlanet(idFrom);
		Planet to = engine.getPlanet(idTo);
		planet.amount = amountLeft;
		to.amount = toAmountLeft;
	}

	public void messageUpdateInfo() throws IOException {
		os.writeByte(Interaction.UPDATE_INFO);
	}

	public void receivedChangeFleetDirection(int tick, int idFleet, int idTo) {
		try {
			engine.changeFleetDirection(idFleet, idTo);
		} catch (GameDataMismatchException e) {
			e.printStackTrace();
			try {
				messageUpdateInfo();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void receivedPlanetOwned(int tick, int idPlanet, int idPlayer, double amount) {
		Planet p = engine.getPlanet(idPlanet);
		p.idPlayer = idPlayer;
		p.amount = amount;
	}

	public void messageShipsFromPlanet(int idFrom, int idTo, int amount) throws IOException {
		os.writeByte(Interaction.SEND_SHIPS);
		os.writeByte(idFrom);
		os.writeByte(idTo);
		os.writeInt(amount);
		os.flush();
	}

	public void messageChangeFleetDirection(int idFleet, int idTo) throws IOException {
		os.writeByte(Interaction.FLEET_CHANGE_DIRECTION);
		os.writeChar(idFleet);
		os.writeByte(idTo);
		os.flush();
	}

	public Player getPlayer() {
		while(player == null)
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return player;
	}
}