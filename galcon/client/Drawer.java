package galcon.client;

import galcon.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 1:30:11
 * To change this template use File | Settings | File Templates.
 */
public class Drawer extends JPanel {

	private PartyEngine engine;
	private ControlsModel controlsModel;
	public static final int FIELD_WIDTH = PartyEngine.FIELD_WIDTH;
	public static final int FIELD_HEIGHT = PartyEngine.FIELD_HEIGHT;

	public Drawer(PartyEngine engine, ControlsModel controlsModel) {
		this.engine = engine;
		this.controlsModel = controlsModel;
		setFocusable(true);
	}

	private static final Color SELECT_COLOR = new Color(Color.CYAN.getRed(), Color.CYAN.getGreen(), Color.CYAN.getBlue(), 80);

	@Override
	protected void paintComponent(Graphics g1) {
		super.paintComponent(g1);
		Graphics2D g = (Graphics2D)g1;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, PartyEngine.FIELD_WIDTH, PartyEngine.FIELD_HEIGHT);
		Player[] players = engine.getPlayers();
		java.util.List<Planet> selectedPlanets = controlsModel.getSelectedPlanets();



		for (Planet p : engine.getPlanets()) {
			Player player = findPlayer(players, p.idPlayer);

			if (player == null)
				g.setColor(Color.GRAY);
			else
				g.setColor(getColor(player.idColor));
			int radius = (int)PartyEngine.getPlanetRadius(p.power);
			g.fill(new Ellipse2D.Double(p.x - radius, p.y - radius, radius*2, radius*2));
			if (selectedPlanets.contains(p) || controlsModel.hoverPlanet != null && controlsModel.hoverPlanet.id == p.id) {
				g.setStroke(new BasicStroke(4));
				g.setColor(SELECT_COLOR);
				g.drawOval(p.x - radius - 6, p.y - radius - 6, radius*2 + 12, radius*2 + 12);
			}

			g.setStroke(new BasicStroke(1));
			g.setColor(Color.BLACK);
			g.drawString(String.valueOf((int)p.amount), p.x - 4, p.y +4);
			g.setColor(Color.RED);
			g.drawString("Id: " + p.id, p.x - 4, p.y - 12);
		}

		for (Fleet fleet : new ArrayList<Fleet>(engine.getFleets())) {
			LinkedList<Ship> ships = new LinkedList<Ship>(fleet.ships);
			for (Ship ship : ships) {
				Player player = findPlayer(players, fleet.idPlayer);
				g.setColor(getColor(player.idColor));
				g.setStroke(new BasicStroke(2));
				double[][] baseCoords = {
						{8, 0, 0},
						{-3, 3, Math.PI/4 + Math.PI/2},
						{-3, -3, Math.PI/4 + Math.PI}
				};

				int[] xx = new int[baseCoords.length];
				int[] yy = new int[baseCoords.length];
				for (int i = 0; i < baseCoords.length; i++) {
					double[] pair = baseCoords[i];
					double ro = Math.sqrt(pair[0]*pair[0] + pair[1]*pair[1]);
					xx[i] = (int)(ro*Math.cos(ship.direction + pair[2]) + ship.x);
					yy[i] = (int)(ro*Math.sin(ship.direction + pair[2]) + ship.y);
				}

				g.drawPolygon(xx, yy, baseCoords.length);
				g.setColor(Color.BLACK);
				g.setStroke(new BasicStroke(1));
				//g.drawOval((int)ship.x - PartyEngine.NEAREST_SHIPS_LENGTH/2, (int)ship.y - PartyEngine.NEAREST_SHIPS_LENGTH/2, PartyEngine.NEAREST_SHIPS_LENGTH, PartyEngine.NEAREST_SHIPS_LENGTH);
				/*double[] vector = PartyEngine.calculateNeightbourDelta(ship, ships);
				double vx = Math.cos(vector[0])*vector[1];
				double vy = Math.sin(vector[0])*vector[1];
				if (vx != 0 || vy != 0)
				g.drawLine((int)ship.x, (int)ship.y, (int)(ship.x + vx), (int)(ship.y + vy));
				g.fillOval((int)ship.x - 1, (int)ship.y - 1, 2, 2);
				g.setColor(getColor(player.idColor));*/
				//g.drawString(String.valueOf(ship.id), (int)ship.x - 4, (int)ship.y - 4);


			}
			Fleet selectedFleet = controlsModel.getSelectedFleet();
			if (selectedFleet != null && selectedFleet.id == fleet.id) {
				g.setStroke(new BasicStroke(4));
				g.setColor(SELECT_COLOR);
				int[] coords = ControlsModel.calculateFleetAvgCoords(selectedFleet);
				g.drawOval(coords[0] - ControlsModel.FLEET_RADIUS, coords[1] - ControlsModel.FLEET_RADIUS, ControlsModel.FLEET_RADIUS*2, ControlsModel.FLEET_RADIUS*2);
			}
		}



		Rectangle selectionArea = controlsModel.getSelectionRange();
		if (selectionArea != null && (selectionArea.width > 10 || selectionArea.height > 10)) {
			g.setStroke(new BasicStroke(4));
			g.draw(controlsModel.getSelectionRange());
			g.setStroke(new BasicStroke(1));
		}


		if (controlsModel.hoverPlanet != null)
		for (Planet p : selectedPlanets) {
			if (p.id == controlsModel.hoverPlanet.id) continue;
			g.setStroke(new BasicStroke(4));
			g.setColor(SELECT_COLOR);

			int dx = controlsModel.hoverPlanet.x - p.x;
			int dy = controlsModel.hoverPlanet.y - p.y;
			double alpha = Math.atan2(dy, dx);
			double radius = getPlanetRadius(p.power) + 8;
			int x1 = p.x + (int)(radius*Math.cos(alpha));
			int y1 = p.y + (int)(radius*Math.sin(alpha));

			alpha += Math.PI;
			radius = getPlanetRadius(controlsModel.hoverPlanet.power) + 8;
			int x2 = controlsModel.hoverPlanet.x + (int)(radius*Math.cos(alpha));
			int y2 = controlsModel.hoverPlanet.y + (int)(radius*Math.sin(alpha));

			g.drawLine(x1, y1, x2, y2);
		}

		if (controlsModel.selectedFleet != null && controlsModel.hoverPlanet != null) {
			Planet p = controlsModel.hoverPlanet;
			int[] coords = ControlsModel.calculateFleetAvgCoords(controlsModel.selectedFleet);
			g.setStroke(new BasicStroke(4));
			g.setColor(SELECT_COLOR);

			int dx = coords[0] - p.x;
			int dy = coords[1] - p.y;
			double alpha = Math.atan2(dy, dx);
			double radius = getPlanetRadius(p.power) + 8;
			int x1 = p.x + (int)(radius*Math.cos(alpha));
			int y1 = p.y + (int)(radius*Math.sin(alpha));

			alpha += Math.PI;
			radius = ControlsModel.FLEET_RADIUS + 8;
			int x2 = coords[0] + (int)(radius*Math.cos(alpha));
			int y2 = coords[1] + (int)(radius*Math.sin(alpha));

			g.drawLine(x1, y1, x2, y2);

		}

		g.setStroke(new BasicStroke(2));
		g.setColor(SELECT_COLOR);
		g.drawString(String.valueOf(controlsModel.percentage) + " %", FIELD_WIDTH - 40, FIELD_HEIGHT - 10);

	}

	private static double[] calculateNeightbourDelta(Ship ship, LinkedList<Ship> ships) {
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

			double resX;
			double resY;
			double rho = Math.sqrt(a + b);
			if (rho < PartyEngine.NEAREST_SHIPS_LENGTH) {
				if (as == 0 && bs == 0) {
					resX = Math.random()*PartyEngine.NEAREST_SHIPS_LENGTH - PartyEngine.NEAREST_SHIPS_LENGTH/2;
					resY = Math.random()*PartyEngine.NEAREST_SHIPS_LENGTH - PartyEngine.NEAREST_SHIPS_LENGTH/2;
				} else if (bs == 0) {
					resX = Math.signum(as)*(PartyEngine.NEAREST_SHIPS_LENGTH - Math.abs(as));
					resY = 0;
				} else if (as == 0) {
					resY =  Math.signum(as)*(PartyEngine.NEAREST_SHIPS_LENGTH - Math.abs(bs));
					resX = 0;
				} else {
					double theta = Math.atan2(bs, as);
					resX = (PartyEngine.NEAREST_SHIPS_LENGTH - rho)*Math.cos(theta);
					resY = (PartyEngine.NEAREST_SHIPS_LENGTH - rho)*Math.sin(theta);
					//resX = Math.signum(as)*PartyEngine.NEAREST_SHIPS_LENGTH*Math.sqrt(b/(a+b));
					//resY = Math.signum(bs)*PartyEngine.NEAREST_SHIPS_LENGTH*Math.sqrt(1 - b/(a + b));
				}
				ddx += resX;
				ddy += resY;
			}

		}

		return new double[] {Math.atan2(ddy, ddx), Math.min(PartyEngine.NEAREST_SHIPS_LENGTH, Math.sqrt(ddx*ddx + ddy*ddy))};
	}

	private double getPlanetRadius(int power) {
		return PartyEngine.getPlanetRadius(power);
	}

	private Player findPlayer(Player[] players, int idPlayer) {
		for (Player p : players)
			if (idPlayer == p.id)
				return p;
		return null;
	}

	private static Color getColor(int idColor) {
		 switch (idColor) {
			 case 1: return Color.RED;
			 case 2: return Color.BLUE;
			 case 3: return Color.ORANGE;
			 case 4: return Color.GREEN;
			 case 5: return Color.PINK;
			 case 6 : return Color.YELLOW;
			 default: return Color.GRAY;
		 }
	}
}