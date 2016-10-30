package util.song.model.window;

/**
 * Created by thinhhv on 04/08/2014.
 */
public class HanningWindow extends Window {
	public HanningWindow(int fftSize) {
		super(fftSize);
	}

	@Override
	protected void init() {
		for (int i = 0; i < fftSize; i++)
			weight[i] = 0.5f * (1.0f - Math.cos((2 * Math.PI * i) / (fftSize - 1)));
	}
}
