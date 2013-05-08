package galcon.client;

import galcon.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 20.04.2013
 * Time: 1:35:52
 * To change this template use File | Settings | File Templates.
 */
public class ClientFrame {
	public static void main(String[] args) throws IOException {
		String host;
		if (args.length > 0)
			host = args[0];
		else
			host = "localhost";
		Socket s = new Socket(host, 4576);

		/*Socket s = new Socket(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("199.15.248.179", 7808)));
		s.connect(new InetSocketAddress(host, 4576));*/
		final PartyEngine engine = new PartyEngine(new Player[0], new Planet[0], new PartyEventListener.Adapter());
		ClientPlayer cp = new ClientPlayer(engine, s);
		ControlsModel cm = new ControlsModel(engine, cp, cp.getPlayer());
		cp.messageUpdateInfo();
		final Drawer dr = new Drawer(engine, cm);
		JFrame f = new JFrame("Player #" + cp.getPlayer().id);


		f.setLayout(new BorderLayout());
		dr.setSize(Drawer.FIELD_WIDTH, Drawer.FIELD_HEIGHT);

		UserInputListener uml = new UserInputListener(cm);
		dr.addMouseListener(uml);
		dr.addMouseMotionListener(uml);
		dr.addMouseWheelListener(uml);
		dr.addKeyListener(uml);

		f.add(dr, BorderLayout.CENTER);

		f.setSize(Drawer.FIELD_WIDTH + 50, Drawer.FIELD_HEIGHT + 50);

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
