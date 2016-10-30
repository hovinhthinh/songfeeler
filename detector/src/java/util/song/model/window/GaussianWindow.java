package util.song.model.window;

/**
 * Created by thinhhv on 15/08/2014.
 */
public class GaussianWindow extends Window {
	private static final double sigma = 0.5;

	public GaussianWindow(int fftSize) {
		super(fftSize);
	}

	@Override
	protected void init() {
		for (int i = 0; i < fftSize; i++)
			weight[i] = Math.pow(Math.E, -0.5 * (i * 2 / sigma / (fftSize - 1) - 1 / sigma) * (i * 2 / sigma / (fftSize - 1) - 1 / sigma));
	}
}
