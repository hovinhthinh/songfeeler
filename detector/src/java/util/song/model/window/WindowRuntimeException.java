package util.song.model.window;

/**
 * Created by thinhhv on 04/08/2014.
 */
public class WindowRuntimeException extends RuntimeException {
	public WindowRuntimeException() {
		super();
	}

	public WindowRuntimeException(String message) {
		super(message);
	}

	public WindowRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public WindowRuntimeException(Throwable cause) {
		super(cause);
	}
}
