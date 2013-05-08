package galcon.server;

import galcon.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 21.04.2013
 * Time: 19:05:44
 * To change this template use File | Settings | File Templates.
 */
public class Room implements Runnable {

	private RoomBroadcast b = new RoomBroadcast();
	private PartyEngine engine = new PartyEngine(new Player[0], new Planet[0], new PartyEventListener.Adapter());

	public void addPlayer(ServerPlayer sp) {

		b.addPlayer(sp);
		sp.setEngine(engine);
		for (int i = 1; i <= 6; i++) {
			boolean isUsed = false;
			for (ServerPlayer p : b.players)
				if (p.player.idColor == i) {
					isUsed = true;
					break;
				}
			if (!isUsed) {
				sp.player.idColor = i;
				break;
			}
		}

		try {
			sp.notifyPlayerInstalled(sp.player.id, sp.player.idColor);
		} catch (IOException e) {
			e.printStackTrace();
		}

		synchronized (this) {
			this.notify();
		}
	}

	public List<ServerPlayer> getPlayers() {
		return b.players;
	}

	public void run() {
		System.out.println("started");
		while (true) {
			synchronized (this) {
				try {
					while (b.players.size() < 2)
						this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}


			Player[] players = new Player[b.players.size()];
			{
				int i = 0;
				for (ServerPlayer p : b.players) {
					players[i++] = p.player;
				}
			}
			MapGenerator mg = new MapGenerator();
			engine = new PartyEngine(players, mg.generateMap(players), b);
			b.gameStarted(engine); // TODO: очевидно что при бродкасте у кого-то будет преимущество по оценке карты. надо дать равные права
			Timer t = new Timer();
			t.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					engine.doTick();
					if (engine.isFinished()) {
						synchronized (engine) {
							engine.notify();
						}
					}
				}
			}, PartyEngine.TICK_TIME, PartyEngine.TICK_TIME);
			synchronized (engine) {
				while (!engine.isFinished())
					try {
						engine.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
			}
			Set<Player> playersLeft = engine.getPlayersLeft();
			if (playersLeft.size() > 0) {
				Player[] pl = playersLeft.toArray(new Player[1]);
				b.gameFinished(pl[0].id);
			}

			t.cancel();
		}
	}
}
