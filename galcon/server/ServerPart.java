package galcon.server;

import galcon.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 19.04.2013
 * Time: 21:04:59
 * To change this template use File | Settings | File Templates.
 */
public class ServerPart {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(4576);
		int idPlayerGenerator = 0;


		final Room room = new Room();
		Thread t = new Thread(room, "room monitor");
		t.start();
		while (true) {
			Socket s = ss.accept();
			System.out.println("Connected: " + s);
			ServerPlayer sp = new ServerPlayer(new Player(++idPlayerGenerator, 0), s);

			room.addPlayer(sp);

		}



	}


}
