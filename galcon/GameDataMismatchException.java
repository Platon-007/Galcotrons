package galcon;

/**
 * Created by IntelliJ IDEA.
 * User: Денис
 * Date: 21.04.2013
 * Time: 10:34:51
 * To change this template use File | Settings | File Templates.
 */
public class GameDataMismatchException extends Exception {
	public GameDataMismatchException() {
	}

	public GameDataMismatchException(String message) {
		super(message);
	}

	public GameDataMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public GameDataMismatchException(Throwable cause) {
		super(cause);
	}

	public GameDataMismatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
