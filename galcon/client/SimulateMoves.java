package galcon.client;

import galcon.*;
import galcon.server.MapGenerator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 24.04.2013
 * Time: 23:02:13
 * To change this template use File | Settings | File Templates.
 */
public class SimulateMoves {
	public static void main(String[] args) {
		Player[] players = new Player[] {
				new Player(1, 1),
				new Player(2, 2),
				new Player(3, 3),
				new Player(4, 4)
		};
		MapGenerator mg = new MapGenerator();
		Planet[] planets = mg.generateMap(players);
		final PartyEngine engine = new PartyEngine(players, planets, new PartyEventListener.Adapter());
		final ControlsModel cm = new ControlsModel(engine, new ControlModelListenerAdapter(engine), players[0]);

		JFrame f = new JFrame();
		f.setLayout(new BorderLayout());
		final Drawer dr = new Drawer(engine, cm);
		dr.setSize(600, 300);
		UserInputListener uml = new UserInputListener(cm);
		dr.addMouseListener(uml);
		dr.addMouseMotionListener(uml);
		dr.addKeyListener(uml);
		dr.addMouseWheelListener(uml);

		f.add(dr, BorderLayout.CENTER);

		JPanel c = new JPanel();
		c.setLayout(new FlowLayout());
		f.add(c, BorderLayout.SOUTH);

		final Timer ttt = new Timer(PartyEngine.TICK_TIME, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				engine.doTick();
			}
		});

		JButton b = new JButton("Эмуляция шага");
		b.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				ttt.start();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ttt.stop();
			}
		});
		c.add(b);

		c.add(new JButton(new AbstractAction("Изменить направление") {
			public void actionPerformed(ActionEvent e) {
				java.util.List<Fleet> items = engine.getFleets();
				Fleet cur = items.get((int)(Math.random()*items.size()));

				Planet[] pp = engine.getPlanets();
				Planet dest;
				do {
					dest = pp[(int)(Math.random()*pp.length)];
				} while(dest.id == cur.destPlanet.id);

				try {
					engine.changeFleetDirection(cur.id, dest.id);
				} catch (GameDataMismatchException e1) {
					e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}));
		c.add(new JButton(new AbstractAction("Генерировать флот") {
			public void actionPerformed(ActionEvent e) {
				Random r = new Random();


				Planet[] pp = engine.getPlanets();
				Planet from = pp[r.nextInt(pp.length)];
				while (from.idPlayer == 0)
					from = pp[r.nextInt(pp.length)];

				int idTo = pp[r.nextInt(pp.length)].id;
				while (idTo == from.id)
					idTo = pp[r.nextInt(pp.length)].id;
				try {
					engine.sendShipsFromPlanet(from.id, idTo, 20);
				} catch (GameDataMismatchException e1) {
					e1.printStackTrace();
				}
			}
		}));

		f.setSize(850, 550);

		f.setLocation(0, 0);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setResizable(false);
		f.setVisible(true);

		javax.swing.Timer timer = new javax.swing.Timer(PartyEngine.TICK_TIME, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dr.repaint();
			}
		});
		timer.start();
	}
}
