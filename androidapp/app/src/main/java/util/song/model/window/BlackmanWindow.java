package util.song.model.window;

/**
 * Created by thinhhv on 11/08/2014.
 */
public class BlackmanWindow extends Window {
	private static final double alpha = 0.16;

	public BlackmanWindow(int fftSize) {
		super(fftSize);
	}

	@Override
	protected void init() {
		double a0 = (1 - alpha) / 2, a1 = 0.5, a2 = alpha / 2;
		for (int i = 0; i < fftSize; i++)
			weight[i] = a0 - a1 * Math.cos((2 * Math.PI * i) / (fftSize - 1)) + a2 * Math.cos((4 * Math.PI * i) / (fftSize - 1));
	}
}
