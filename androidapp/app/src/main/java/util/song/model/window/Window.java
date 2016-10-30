package util.song.model.window;

/**
 * Created by thinhhv on 23/07/2014.
 */
public abstract class Window {
	protected double weight[];
	protected int fftSize;

	public Window(int fftSize) {
		this.fftSize = fftSize;
		weight = new double[fftSize];
		init();
	}

	protected abstract void init();

	public void applyWindowing(double[] sample) {
		if (sample.length != fftSize) throw new WindowRuntimeException("confict window size");
		for (int i = 0; i < fftSize; i++)
			sample[i] *= weight[i];
	}
}
