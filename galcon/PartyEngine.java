package galcon;

import galcon.GameDataMismatchException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 18.04.2013
 * Time: 21:14:58
 * To change this template use File | Settings | File Templates.
 */
public class PartyEngine {

	public static final int FIELD_WIDTH = 800;
	public static final int FIELD_HEIGHT = 500;

	public static final int TICK_TIME = 10;
	private static final int TICKS_TO_END = 5*1000/TICK_TIME;
	private static final int TICK_TO_END_FLYING = 20*1000/TICK_TIME;
	public static final double SHIP_SPEED = 6f/TICK_TIME;

	public static final int SHIP_TURN_FULL_TIME = 500;

	public static final int PLANET_UNIT_RADIUS = 2;
	// количество кораблей на 1 единицу радиуса планеты
	public static final int PLANET_UNIT_AMOUNT = 9;

	public static final int PLANET_BASE_POWER = 100;

	public static final double SHIP_TURN_DELTA = (double)TICK_TIME/SHIP_TURN_FULL_TIME*Math.PI;

	private int tick;
	private Player[] players;
	private Planet[] planets;
	private LinkedList<Fleet> fleets;
	private int idFleetGenerator;
	private int idShipGenerator;
	private PartyEventListener listener;
	private HashSet<Player> playersLeft;


	public PartyEngine(Player[] players, Planet[] planets, PartyEventListener listener) {
		playersLeft = new HashSet<Player>();
		this.players = players;
		this.planets = planets;
		this.fleets = new LinkedList<Fleet>();
		tick = 0;
		idFleetGenerator = 0;
		idShipGenerator = 0;
		this.listener = listener;
	}

	public synchronized void doTick() {
	   tick++;

		playersLeft.clear();
		// прирост планет
		for (Planet p : planets)
			// TODO: более точный расчёт прироста кораблей в минуту?
			if (p.idPlayer != 0) {
				playersLeft.add(getPlayer(p.idPlayer));
				p.amount += p.power*TICK_TIME/(1000d*60);
			}

		if (playersLeft.size() <= 1) {
			if (finishFlyTick == 0)
				finishFlyTick = tick;
		} else
			finishFlyTick = 0;

		// перемещение флотов
		for (Fleet fleet : fleets.toArray(new Fleet[fleets.size()])) {
			Planet destPlanet = fleet.destPlanet;

			playersLeft.add(getPlayer(fleet.idPlayer));
			for (Ship ship : new ArrayList<Ship>(fleet.ships)) {



				/*int dx = fleet.destPlanet.x - ship.x;
				int dy = fleet.destPlanet.y - ship.y;

				double destDirection = Math.atan2(dy, dx);
				if (destDirection < 0)
					destDirection += 2*Math.PI;*/
				double destDirection = calculateDirection(ship.x, ship.y, fleet.destPlanet.x, fleet.destPlanet.y);


				double[] planetDirection = new double[] {destDirection, SHIP_SPEED};

				double[] neighbourDirection = calculateNeightbourDelta(ship, fleet.ships);

				double[] finalDirection = calcFinalDirection(new double[][] {planetDirection, neighbourDirection});
				finalDirection = calculateIntersectPlanetDelta(destPlanet.id, finalDirection[0], finalDirection[1], ship, planets);

				double deltaDirection = finalDirection[0] - ship.direction;

				if (Math.abs(deltaDirection) > 10E-9) {
					if (deltaDirection > Math.PI)
						deltaDirection -= 2*Math.PI;
					else if (deltaDirection < -Math.PI)
						deltaDirection += 2*Math.PI;
					double newDelta = (Math.abs(deltaDirection) < SHIP_TURN_DELTA)?
								deltaDirection
							:   Math.signum(deltaDirection)*SHIP_TURN_DELTA;
					ship.direction += newDelta;
					if (ship.direction < 0)
						ship.direction += 2*Math.PI;
					if (ship.direction > 2*Math.PI)
						ship.direction -= 2*Math.PI;
				}

				ship.x = ship.x + (Math.cos(ship.direction)*finalDirection[1]);
				ship.y = ship.y + (Math.sin(ship.direction)*finalDirection[1]);


				// столкновение кораблей с планетами
				// проверяем по старой доброй формуле x^2 + y^2 < R^2
				if (Math.pow(ship.x - destPlanet.x, 2) + Math.pow(ship.y - destPlanet.y, 2) < Math.pow(getPlanetRadius(destPlanet.power), 2)) {
					if (fleet.idPlayer == destPlanet.idPlayer) {
						// переброска кораблей на свою планету
						destPlanet.amount += ship.amount;
					} else {
						// бомбим вражескую планету!
						if (destPlanet.amount < ship.amount) {
							// взорвали её
							destPlanet.idPlayer = fleet.idPlayer;
							destPlanet.amount = ship.amount - destPlanet.amount;
							listener.planetOwned(tick, destPlanet.id, destPlanet.idPlayer, destPlanet.amount);
						} else {
							destPlanet.amount -= ship.amount;
						}
					}
					fleet.ships.remove(ship);

				}
				if (fleet.ships.isEmpty())
					fleets.remove(fleet);
			}
		}

		if (playersLeft.size() <= 1) {
			if (finishTick == 0)
			finishTick = tick;
		} else
			finishTick = 0;
	}

	private double[] calculateIntersectPlanetDelta(int idTo, double destDirection, double speed, Ship ship, Planet[] planets) {
		double x2 = ship.x;
		double y2 = ship.y;

		double ddx = 0;
		double ddy = 0;
		boolean wasIntersected = false;

		for (Planet p : planets) {
			if (idTo == p.id) continue;
			double x1 = p.x;
			double y1 = p.y;
			double as = x2 - x1;
			double bs = y2 - y1;
			double a = as*as;
			double b = bs*bs;

			double planetRadius = getPlanetRadius(p.power) + 12;
			double rho = Math.sqrt(a + b);
			if (rho < planetRadius) {
				if (false && rho < planetRadius - 10) {
					return new double[] {destDirection, speed};

				} else {

					double th = Math.atan2(bs, as);
					double x3 = Math.cos(destDirection)*speed + x2;
					double y3 = Math.sin(destDirection)*speed + y2;

					double as2 = x3 - x1;
					double bs2 = y3 - y1;

					double rho2 = Math.sqrt(as2*as2 + bs2*bs2);
					if (rho2 >= rho) continue;
					double th2 = Math.atan2(bs2, as2);

					double newX = Math.cos(th2 - th)*rho2;
					double newY = Math.sin(th2 - th)*rho2;

					if (true && newX < planetRadius) {
						if (newY == 0) {
							newY = Math.signum(Math.random() - 0.5)*speed;
						} else
							newY = Math.signum(newY)*speed;
						newX = rho;
						double rho3 = Math.sqrt(newX*newX + newY*newY);
						double th3  = Math.atan2(newY, newX);
						double resX = Math.cos(th3 + th)*rho3;
						double resY = Math.sin(th3 + th)*rho3;

						ddx += resX - as;
						ddy += resY - bs;

					}
				}
				wasIntersected = true;
			}
		}



		if (wasIntersected)
			return new double[] {Math.atan2(ddy, ddx), speed};
		else
			return new double[] {destDirection, speed};
	}

	private Planet getIntersectPlanet(double x, double y) {
		for (Planet p : planets)
			if (GalconUtil.isPointInCircle(p.x, p.y, (int)getPlanetRadius(p.power), (int)x, (int)y))
				return p;
		return null;
	}

	private double[] calcFinalDirection(double[][] directions) {
		double resX = 0;
		double resY = 0;
		for (double[] i : directions) {
			resX += Math.cos(i[0])*i[1];
			resY += Math.sin(i[0])*i[1];
		}
		return new double[] {Math.atan2(resY, resX), Math.min(SHIP_SPEED, Math.sqrt(resX*resX + resY*resY))};
	}

	public static final int NEAREST_SHIPS_LENGTH = 15;

	public static double[] calculateNeightbourDelta(Ship ship, LinkedList<Ship> ships) {
		int ddx = 0;
		int ddy = 0;
		double x2 = ship.x;
		double y2 = ship.y;
		for (Ship ss : ships) {
			if (ship.id == ss.id) continue;
			double x1 = ss.x;
			double y1 = ss.y;
			double as = x2 - x1;
			double bs = y2 - y1;
			double a = as*as;
			double b = bs*bs;


			double rho = Math.sqrt(a + b);
			if (rho < NEAREST_SHIPS_LENGTH) {
				double resX;
				double resY;
				if (as == 0 && bs == 0) {
					resX = Math.random()*NEAREST_SHIPS_LENGTH - NEAREST_SHIPS_LENGTH/2;
					resY = Math.random()*NEAREST_SHIPS_LENGTH - NEAREST_SHIPS_LENGTH/2;
				} else if (bs == 0) {
					resX = Math.signum(as)*(NEAREST_SHIPS_LENGTH - Math.abs(as));
					resY = 0;
				} else if (as == 0) {
					resY =  Math.signum(as)*(NEAREST_SHIPS_LENGTH - Math.abs(bs));
					resX = 0;
				} else {
					double theta = Math.atan2(bs, as);
					resX = (NEAREST_SHIPS_LENGTH - rho)*Math.cos(theta);
					resY = (NEAREST_SHIPS_LENGTH - rho)*Math.sin(theta);
					//resX = Math.signum(as)*NEAREST_SHIPS_LENGTH*Math.sqrt(b/(a+b));
					//resY = Math.signum(bs)*NEAREST_SHIPS_LENGTH*Math.sqrt(1 - b/(a + b));
				}
				ddx += resX;
				ddy += resY;
			}

		}

		//double data = Math.min(NEAREST_SHIPS_LENGTH, Math.sqrt(ddx*ddx + ddy*ddy));

		double len = SHIP_SPEED*Math.sqrt(ddx*ddx + ddy*ddy)/NEAREST_SHIPS_LENGTH;

		return new double[] {Math.atan2(ddy, ddx), len};
	}

	private int finishTick;
	private int finishFlyTick;

	public boolean isFinished() {
		return finishTick != 0 && finishTick + TICKS_TO_END <= tick
				|| finishFlyTick != 0 && finishFlyTick + TICK_TO_END_FLYING <= tick;
	}

	public Set<Player> getPlayersLeft() {
		return playersLeft;
	}

	public synchronized void removePlayer(int idPlayer) {
		for (Planet p : planets)
			if (p.idPlayer == idPlayer)
				p.idPlayer = 0;
		Iterator<Fleet> it = fleets.iterator();
		while (it.hasNext()) {
			Fleet f = it.next();
			if (f.idPlayer == idPlayer)
				it.remove();
		}
		
		Player[] newPlayers = new Player[players.length - 1];

		for (int i = 0; i < players.length; i++)
			if (players[i].id == idPlayer) {
				System.arraycopy(players, i+1, newPlayers, i, players.length - i - 1);
				break;
			} else
				newPlayers[i] = players[i];
		this.players = newPlayers;
		listener.playerRemoved(idPlayer);
	}

	private synchronized Player getPlayer(int idPlayer) {
		for (Player p : players)
			if (p.id == idPlayer)
				return p;
		return null;
	}

	public synchronized void sendShipsFromPlanet(int idFrom, int idTo, int amount) throws GameDataMismatchException {
		Planet source = null;
		Planet dest = null;
		for (Planet p : planets)
			if (p.id == idFrom)
				source = p;
			else if (p.id == idTo)
				dest = p;
		if (source == null || dest == null)
			throw new GameDataMismatchException("no such id " + idFrom + " or " + idTo);
		boolean wrongShipsAmount = (source.amount < amount);
		if (wrongShipsAmount)
			amount = (int)source.amount;


		Fleet fl = new Fleet(++idFleetGenerator, 0, 0, dest, source.idPlayer);

		int totalAmount = getTotalShipsAmount();
		int shipsAmount = getShipAmount(amount, totalAmount);
		int multiplex = Math.max(amount/shipsAmount, 1);

		int minShips = Math.min(amount, shipsAmount);
		for (int i = 0; i < minShips; i++) {
			fl.ships.add(new Ship(++idShipGenerator, source.x, source.y, calculateDirection(source.x, source.y, dest.x, dest.y), fl.id, multiplex));
		}
		int restShips = amount - minShips*multiplex;
		if (restShips > 0)
			fl.ships.add(new Ship(++idShipGenerator, source.x, source.y, calculateDirection(source.x, source.y, dest.x, dest.y), fl.id, restShips));
		source.amount -= amount;
		fleets.add(fl);
		listener.sendFromPlanet(tick, fl.id, source.id, dest.id, amount, source.amount, dest.amount);
		if (wrongShipsAmount)
			throw new GameDataMismatchException("not enough ships");
	}

	private int getTotalShipsAmount() {
		double total = 0;
		for (Planet p : planets)
			if (p.idPlayer != 0)
				total += p.amount;
		for (Fleet f : fleets)
		 for (Ship s : f.ships) {
			 total += s.amount;
		 }

		return (int)total;
	}

	private static int getShipAmount(int amount, int totalAmount) {
		double m = 800 - 800/Math.sqrt(Math.sqrt(Math.sqrt(Math.sqrt(totalAmount))));
		return Math.max(1, (int)(amount*m/totalAmount));
	}

	public synchronized void changeFleetDirection(int idFleet, int idTo) throws GameDataMismatchException {
		Planet dest = null;
		for (Planet p : planets)
			if (p.id == idTo) {
				dest = p;
				break;
			}
		if (dest == null)
			throw new GameDataMismatchException("no such Planet ID " + idTo);
		Fleet fleet = null;
		for (Fleet f : fleets)
			if (f.id == idFleet) {
				fleet = f;
				break;
			}
		if (fleet == null)
			throw new GameDataMismatchException("no such Fleet ID " + idFleet);
		fleet.destPlanet = dest;
		listener.changeFleetDirection(tick, fleet.id, dest.id);
	}

	public synchronized Planet[] getPlanets() {
		return planets;
	}

	public synchronized LinkedList<Fleet> getFleets() {
		return fleets;
	}

	public synchronized Fleet getFleet(int id) {
		for (Fleet p : fleets)
			if (p.id == id)
				return p;
		return null;
	}

	public synchronized Planet getPlanet(int id) {
		for (Planet p : planets)
			if (p.id == id)
				return p;
		return null;
	}

	private static double calculateDirection(double x1, double y1, double x2, double y2) {
		double dx = x2 - x1;
		double dy = y2 - y1;
		double direction = Math.atan2(dy, dx);
		if (direction < 0)
			direction += 2*Math.PI;
		return direction;
	}

	public static double getPlanetRadius(int power) {
		return power*PLANET_UNIT_RADIUS/PLANET_UNIT_AMOUNT + 5;
	}

	public synchronized Player[] getPlayers() {
		return players;
	}

	public synchronized int getTick() {
		return tick;
	}


	public synchronized void updateInfo(int tick, Player[] players, Planet[] planets, LinkedList<Fleet> fleets) {
		this.tick = tick;
		this.players = players;
		this.planets = planets;
		this.fleets = fleets;
	}


}
