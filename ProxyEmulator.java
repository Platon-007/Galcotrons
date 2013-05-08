import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 28.04.2013
 * Time: 12:01:25
 * To change this template use File | Settings | File Templates.
 */
public class ProxyEmulator {
	public static void main(String[] args) throws IOException {
		ServerSocket s = new ServerSocket(4456);
		while (true) {
			final Socket ss = s.accept();
			final Socket out = new Socket("localhost", 4576);
			final Random rnd = new Random();
			Thread t = new Thread(new Runnable() {

				public void run() {
					int read = 0;
					try {
						InputStream is = ss.getInputStream();

						OutputStream os = out.getOutputStream();
						while (true) {
							int r = is.read();
							read++;
							if (read % 1024 == 0)
								System.out.println("In: " + (read / 1024) + " KB");
							Thread.sleep(rnd.nextInt(10));
							os.write(r);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}

				}
			});
			t.start();
			t = new Thread(new Runnable() {
				public void run() {
					int read = 0;
					try {
						InputStream is = out.getInputStream();

						OutputStream os = ss.getOutputStream();
						while (true) {
							int r = is.read();
							read++;
							if (read % 1024 == 0)
								System.out.println("Out: " + (read / 1024) + " KB");
							Thread.sleep(rnd.nextInt(10));
							os.write(r);
						}
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}
			});
			t.start();
		}
	}
}
