package galcon.server;

import galcon.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class ServerPlayer {

	public Player player;
	public Socket socket;

	private InputStream is;
	private DataOutputStream os;


	private PartyEngine engine;


	public ServerPlayer(Player player, Socket socket) throws IOException {
		engine = new PartyEngine(new Player[0], new Planet[0], new PartyEventListener.Adapter());
		this.player = player;
		this.socket = socket;
		is = socket.getInputStream();
		os = new DataOutputStream(socket.getOutputStream());
		Thread t = new Thread(new Runnable() {
			public void run() {
				DataInputStream dis = new DataInputStream(is);
				try {
					do {
						decryptReceivedMessage(dis);
					} while (true);

				} catch (IOException e) {
					e.printStackTrace();
					engine.removePlayer(ServerPlayer.this.player.id);
				}
			}
		});
		t.start();
	}

	/*private void notifyRemovePlayer(int id) {
		os.writeInt(id);
	}*/

	public void notifyGameStarted(PartyEngine engine) throws IOException {
		this.engine = engine;
		receivedUpdateInfo();
		os.writeByte(Interaction.NOTIFY_GAME_STARTED);
		os.flush();
	}

	public void setEngine(PartyEngine engine) {
		this.engine = engine;
	}

	public void notifyGameFinished(int id) throws IOException {
		os.writeByte(Interaction.NOTIFY_GAME_FINISHED);
		os.writeInt(id);
		os.flush();
	}

	public void notifyShipsFromPlanet(int tick, int idFrom, int idTo, int amount, double amountLeft, double toAmountLeft) throws IOException {
		os.writeByte(Interaction.NOTIFY_SHIPS_SENT);
		os.writeInt(tick);
		os.writeByte(idFrom);
		os.writeByte(idTo);
		os.writeInt(amount);
		os.writeDouble(amountLeft);
		os.writeDouble(toAmountLeft);
		os.flush();
	}

	public void notifyChangeFleetDirection(int tick, int idFleet, int idTo) throws IOException {
		os.writeByte(Interaction.NOTIFY_FLEET_CHANGE_DIRECTION);
		os.writeInt(tick);
		os.writeInt(idFleet);
		os.writeByte(idTo);
		os.flush();
	}

	public void notifyPlanetOwned(int tick, int idPlanet, int idUser, double amount) throws IOException {
		os.writeByte(Interaction.NOTIFY_PLANET_OWNED);
		os.writeInt(tick);
		os.writeByte(idPlanet);
		os.writeInt(idUser);
		os.writeDouble(amount);
		os.flush();
	}

	public void decryptReceivedMessage(DataInputStream dis) throws IOException {
		byte command = dis.readByte();
		switch (command) {
			case Interaction.FLEET_CHANGE_DIRECTION:
				decryptChangeDirection(dis);
				break;
			case Interaction.SEND_SHIPS:
				decryptShipsFromPlanet(dis);
				break;
			case Interaction.UPDATE_INFO:
				receivedUpdateInfo();
				break;
		}
	}

	private void decryptShipsFromPlanet(DataInputStream dis) throws IOException {
		int idFrom = dis.readByte();
		int idTo = dis.readByte();
		int amount = dis.readInt();
		receivedShipsFromPlanet(idFrom, idTo, amount);
	}

	private void decryptChangeDirection(DataInputStream dis) throws IOException {
		int idFleet = dis.readChar();
		int idTo = dis.readByte();
		receivedChangeFleetDirection(idFleet, idTo);
	}

	public void receivedShipsFromPlanet(int idFrom, int idTo, int amount) {
		Planet[] planets = engine.getPlanets();
		for (Planet p : planets)
			if (idFrom == p.id && p.idPlayer == player.id) {
				try {
					engine.sendShipsFromPlanet(idFrom, idTo, amount);
				} catch (GameDataMismatchException e) {
					try {
						receivedUpdateInfo();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}

	}

	public void receivedChangeFleetDirection(int idFleet, int idTo) {
		List<Fleet> fleets = engine.getFleets();
		for (Fleet f : fleets)
			if (f.id == idFleet && f.idPlayer == player.id)
				try {
					engine.changeFleetDirection(idFleet, idTo);
				} catch (GameDataMismatchException e) {
					e.printStackTrace();
					try {
						receivedUpdateInfo();
					} catch (IOException e1) {
						e1.printStackTrace();
						// TODO: снова выкинуть парня
					}
				}
	}

	public void receivedUpdateInfo() throws IOException {
		os.writeByte(Interaction.NOTIFY_UPDATE_INFO);
		Player[] players = engine.getPlayers();
		Planet[] planets = engine.getPlanets();
		int tick = engine.getTick();

		os.writeInt(tick);


		os.writeByte(players.length);
		for (Player p : players) {
			os.writeInt(p.id);
			os.writeByte(p.idColor);
		}
		os.writeByte(planets.length);
		for (Planet p : planets) {
			os.writeByte(p.id);
			os.writeByte(p.power);
			os.writeChar(p.x);
			os.writeChar(p.y);
			os.writeInt(p.idPlayer);
			os.writeDouble(p.amount);
		}

		List<Fleet> fleets = engine.getFleets();
		os.writeChar(fleets.size());
		for (Fleet fl : fleets) {
			os.writeInt(fl.id);
			os.writeInt(fl.idPlayer);
			os.writeInt(fl.destPlanet.id);
			os.writeByte(fl.ships.size());
			for (Ship s : fl.ships) {
				os.writeInt(s.id);
				os.writeDouble(s.x);
				os.writeDouble(s.y);
				os.writeInt(s.amount);
				os.writeDouble(s.direction);
			}
		}
		os.flush();
	}

	public void notifyPlayerInstalled(int id, int idColor) throws IOException {
		os.writeByte(Interaction.NOTIFY_PLAYER_INSTALLED);
		os.writeInt(id);
		os.writeByte(idColor);
		os.flush();
	}

	public void notifyPlayerLeft(int idPlayer) throws IOException {
		os.writeByte(Interaction.NOTIFY_PLAYER_LEFT);
		os.writeInt(idPlayer);
		os.flush();
	}

	public void notifyPlayerAdded(int id, int idColor) throws IOException {
		os.writeByte(Interaction.NOTIFY_PLAYER_ENTERED);
		os.writeInt(id);
		os.writeByte(idColor);
		os.flush();
	}
}