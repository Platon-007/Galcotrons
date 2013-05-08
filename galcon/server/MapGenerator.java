package galcon.server;

import galcon.PartyEngine;
import galcon.Planet;
import galcon.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 13:49:48
 * To change this template use File | Settings | File Templates.
 */
public class MapGenerator {
	public Planet[] generateMap(Player[] players) {
		ArrayList<Planet> planets = new ArrayList<Planet>(players.length);
		int idGenerator = 0;

		int[][] positions = generateBasePositions(players.length);

		for (int i = 0, playersLength = players.length; i < playersLength; i++) {
			Player p = players[i];
			int[] coords = positions[i];

			// TODO: не забыть, что планеты могут слипаться. Нужно проверять на отдалённость
			planets.add(new Planet(++idGenerator, coords[0], coords[1], 100, p.id, PartyEngine.PLANET_BASE_POWER));
		}
		// TODO: генерировать нейтральные планеты
		Random rnd = new Random();
		int neutralAmount = 8 + rnd.nextInt(8);
		for (int i = 0; i < neutralAmount; i++) {
			int power = 15 + rnd.nextInt(100 - 15);
			int amount = rnd.nextInt(50);
			int[] coords = getCoords(planets, power, false);
			planets.add(new Planet(++idGenerator, coords[0], coords[1], power, 0, amount));
		}

		return planets.toArray(new Planet[planets.size()]);
	}

	private static int[][] generateBasePositions(int positions) {
		double cx = PartyEngine.FIELD_WIDTH/2;
		double cy = PartyEngine.FIELD_HEIGHT/2;
		int a = (int)(cx*0.75);
		int b = (int)(cy*0.75);
		int shiftCoord = a/6;

		int[][] res = new int[positions][];
		Random rnd = new Random();
		ArrayList<Integer> indexes = new ArrayList<Integer>(positions);
		for (int i = 0; i < positions; i++)
			indexes.add(i);
		Collections.shuffle(indexes, rnd);


		double shiftAngle = rnd.nextDouble()*2*Math.PI;
		double dAlpha = 2*Math.PI/positions;
		for (int i = 0; i < positions; i++) {
			double angle = i*dAlpha + shiftAngle + rnd.nextDouble()*0.1;
			while (angle > Math.PI*2)
				angle -= Math.PI*2;
			double tan = Math.tan(angle);
			double x = 1/Math.sqrt(Math.pow(tan/b, 2) + 1./(a*a));
			double y = tan*x;
			if (angle > 3*Math.PI/2) { //4
				
			} /*else if (angle > Math.PI) { //3
				y = -y;
				x = -x;
			}*/ else if (angle > Math.PI/2) { //2
				x = -x;
				y = -y;
			}
			res[indexes.get(i)] = new int[] {(int)(x + cx) + rnd.nextInt(shiftCoord) - shiftCoord/2, (int)(y + cy) + rnd.nextInt(shiftCoord) - shiftCoord/2};
		}
		return res;
	}

	private int[] getCoords(ArrayList<Planet> planets, int power, boolean basePlanet) {
		int x;
		int y;
		double defaultRadius = PartyEngine.getPlanetRadius(power);
		do {
			x = (int)(Math.random()*PartyEngine.FIELD_WIDTH);
			if (basePlanet && x*3 > PartyEngine.FIELD_WIDTH && x*3 < 2*PartyEngine.FIELD_WIDTH)
				if (Math.random() < 0.5)
					x -= PartyEngine.FIELD_WIDTH/3;
				else
					x += PartyEngine.FIELD_WIDTH/3;
			if (x - defaultRadius < 0)
				x = (int)defaultRadius;
			else if (x + defaultRadius > PartyEngine.FIELD_WIDTH)
				x = PartyEngine.FIELD_WIDTH - (int)defaultRadius;
			y = (int)(Math.random()*PartyEngine.FIELD_HEIGHT);
			if (basePlanet && y*3 > PartyEngine.FIELD_HEIGHT && y*3 < 2*PartyEngine.FIELD_HEIGHT)
				if (Math.random() < 0.5)
					y -= PartyEngine.FIELD_HEIGHT/3;
				else
					y += PartyEngine.FIELD_HEIGHT/3;
			if (y - defaultRadius < 0)
				y = (int)defaultRadius;
			else if (y + defaultRadius > PartyEngine.FIELD_HEIGHT)
				y = PartyEngine.FIELD_HEIGHT - (int)defaultRadius;

			// проверяем слипшиеся планеты
			boolean isIn = false;
			for (Planet existingPlanet : planets) {
				double radius = Math.sqrt(Math.pow(existingPlanet.x - x, 2) + Math.pow(existingPlanet.y - y, 2));
				double r1 = PartyEngine.getPlanetRadius(existingPlanet.power);
				double r2 = defaultRadius;
				if (radius < r1 + r2) {
					isIn = true;
					break;
				}
			}
			if (!isIn)
				break;
		} while(true);


		return new int[] {x, y};
	}

	public static void main(String[] args) {
		final int[][] coords = generateBasePositions(6);
		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());

		f.add(new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, PartyEngine.FIELD_WIDTH, PartyEngine.FIELD_HEIGHT);
				g.setColor(Color.BLACK);
				int[] xx = new int[coords.length];
				int[] yy = new int[coords.length];
				for (int i = 0; i < coords.length; i++) {
					xx[i] = coords[i][0];
					yy[i] = coords[i][1];
					g.drawString(String.valueOf(i + 1), xx[i], yy[i] + i*4);
				}

				g.drawPolygon(xx, yy, coords.length);
			}
		});

		f.setSize(850, 550);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
}
