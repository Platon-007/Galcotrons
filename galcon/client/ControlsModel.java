package galcon.client;

import galcon.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 1:31:24
 * To change this template use File | Settings | File Templates.
 */
public class ControlsModel {

	public PartyEngine engine;

	public ControlModelListener listener;

	public Rectangle selection;

	public Point startSelection;

	public Player player;

	public Planet hoverPlanet;

	public ArrayList<Planet> selectedPlanets = new ArrayList<Planet>();
	public ArrayList<Planet> newSelectedPlanets = new ArrayList<Planet>();

	public Fleet selectedFleet = null;
	public static final int FLEET_RADIUS = 25;

	public int percentage = 50;
	private static final int PLANET_MIN_CLICK_RADIUS = 30;

	public ControlsModel(PartyEngine engine, ControlModelListener listener, Player player) {
		this.engine = engine;
		this.player = player;
		this.listener = listener;
	}

	public Fleet getSelectedFleet() {
		return selectedFleet;
	}

	public java.util.List<Planet> getSelectedPlanets() {
		if (newSelectedPlanets.isEmpty())
			return selectedPlanets;
		if (selectedPlanets.isEmpty())
			return newSelectedPlanets;

		Set<Planet> all = new HashSet<Planet>(selectedPlanets.size() + newSelectedPlanets.size());
		all.addAll(selectedPlanets);
		all.addAll(newSelectedPlanets);
		return new ArrayList<Planet>(all);
	}

	public Rectangle getSelectionRange() {
		return selection;
	}

	public void clickArea(int x, int y, boolean ctrl) {
		Planet[] planets = engine.getPlanets();
		boolean somethingChosen = false;

		for (Fleet f : new ArrayList<Fleet>(engine.getFleets())) {
			int[] coords = calculateFleetAvgCoords(f);
			if (GalconUtil.isPointInCircle(coords[0], coords[1], FLEET_RADIUS, x, y)) {
				// TODO: найти самый близкий флот
				selectedFleet = f;
				selectedPlanets.clear();
				somethingChosen = true;
			}
		}

		for (Planet p : planets)
			if (pointInPlanet(p, x, y)) {
				if (p.idPlayer == player.id) { // кликаем на свою планету
					if (ctrl) {
						if (selectedPlanets.contains(p))
							selectedPlanets.remove(p);
						else
							selectedPlanets.add(p);
					} else { // без ctrl
						if (selectedPlanets.isEmpty()) {
							if (selectedFleet == null)
								selectedPlanets.add(p);
						} else {
							moveSelectedShips(p);
							selectedPlanets.clear();
						}

						if (selectedFleet != null) {
							changeDirection(selectedFleet, p);
							selectedFleet = null;
						}
					}
				} else {
					moveSelectedShips(p);
					selectedPlanets.clear();

					if (selectedFleet != null) {
						changeDirection(selectedFleet, p);
						selectedFleet = null;
					}
				}
				somethingChosen = true;
			}



		if (!somethingChosen)
			if (!ctrl) {
				clearSelection();
			}
	}

	private static boolean pointInPlanet(Planet p, int x, int y) {
		return Math.pow(x - p.x, 2) + Math.pow(y - p.y, 2) < Math.pow(Math.max(PartyEngine.getPlanetRadius(p.power) + 10, PLANET_MIN_CLICK_RADIUS), 2);
	}

	public static int[] calculateFleetAvgCoords(Fleet fleet) {

		if (fleet.ships.isEmpty())
			return new int[2];

		if (fleet.ships.size() == 1) {
			Ship s = fleet.ships.get(0);
			return new int[] {(int)s.x, (int)s.y};
		}


		double x = 0;
		double y = 0;
		for (Ship s : fleet.ships) {
			x += s.x;
			y += s.y;
		}

		return new int[] {(int)x/fleet.ships.size(), (int)y/fleet.ships.size()};

	}

	private void changeDirection(Fleet fleet, Planet planet) {
		try {
			listener.messageChangeFleetDirection(fleet.id, planet.id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void moveSelectedShips(Planet to) {
		for (Planet pp : selectedPlanets)
			if (pp.id != to.id) {
				int amount = (int)pp.amount*percentage/100;
				if (amount > 0) {
					try {
						listener.messageShipsFromPlanet(pp.id, to.id, amount);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
	}

	public void startSelect(int x, int y, boolean ctrl) {
		startSelection = new Point(x, y);
		selection = new Rectangle(x, y, 0, 0);
		/*if (!ctrl)
			selectedPlanets.clear();
		newSelectedPlanets.clear();
		Planet[] planets = engine.getPlanets();
		for (Planet p : planets)
			if (p.idPlayer == p.id && GalconUtil.pointInPlanet(p, x, y))
				newSelectedPlanets.add(p);*/
	}

	public void moveSelection(int x, int y, boolean ctrl) {
		selection.width = Math.abs(startSelection.x - x);
		selection.height = Math.abs(startSelection.y - y);
		selection.x = Math.min(x, startSelection.x);
		selection.y = Math.min(y, startSelection.y);
		if (selection.width < 20 && selection.height < 20)
			return;

		if (!ctrl) {
			clearSelection();

		}
		newSelectedPlanets.clear();
		Planet[] planets = engine.getPlanets();
		for (Planet p : planets)
			if (p.idPlayer == player.id && GalconUtil.isPlanetIntersectsRect(p, selection.x, selection.y, selection.width, selection.height))
				newSelectedPlanets.add(p);
	}

	private void clearSelection() {
		selectedPlanets.clear();
		selectedFleet = null;
	}

	public void stopSelection(int x, int y, boolean ctrl) {
		if (selection.width < 20 && selection.height < 20) { // считаем кликом
			// TODO: возможно расчёт клика стоит сделать с привязкой к периоду нажатия/отпуска кнопки
			selection = null;
			clickArea(startSelection.x, startSelection.y, ctrl);
			return;
		}
		selection = null;
		for (Planet p : newSelectedPlanets)
			if (!selectedPlanets.contains(p))
				selectedPlanets.add(p);
		newSelectedPlanets.clear();
	}

	public void hoverAction(int x, int y) {
		Planet[] planets = engine.getPlanets();
		//if (!selectedPlanets.isEmpty()) {
			for (Planet p : planets)
				if (pointInPlanet(p, x, y)) {
					hoverPlanet = p;
					return;
				}
		//}
		hoverPlanet = null;
	}

	public void changePercentage(int change) {
		int newVal = percentage + change;
		if (newVal < 5)
			newVal = 5;
		else if (newVal > 100)
			newVal = 100;
		percentage = newVal;
	}

	public void setPercentage(int np) {
		percentage = np;
	}
}
