package illusion.songfeeler.fragment;

import illusion.songfeeler.R;
import illusion.songfeeler.activity.SongDetailActivity;
import illusion.songfeeler.db.IllusionSQLiteOpenHelper;
import illusion.songfeeler.detect.DetectStream;
import illusion.songfeeler.detect.InputStreamProcessor;
import illusion.songfeeler.detect.OutputStreamProcessor;
import illusion.songfeeler.detect.SampleRecorder;
import illusion.songfeeler.entity.Song;

import java.net.Socket;

import util.EngineConfiguration;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	private IllusionSQLiteOpenHelper dbHelper;

	private static String DETECTOR_ADDRESS = EngineConfiguration.getInstance()
			.get("detector-server.address");
	private static final int DETECTOR_PORT = Integer
			.parseInt(EngineConfiguration.getInstance().get(
					"detector-server.detect-port"));
	private static final int SAMPLE_RATE = Integer.parseInt(EngineConfiguration
			.getInstance().get("frequency-spectrum.sample-rate"));
	private static final int DURATION = Integer.parseInt(EngineConfiguration
			.getInstance().get("detector.detect-time-limit"));
	private static final int MAX_DURATION = Integer
			.parseInt(EngineConfiguration.getInstance().get(
					"detector.detect-total-time-limit"));

	private boolean detecting = false;

	private Socket sc = null;

	private RotateAnimation anim;
	private ImageView ivDetectCircle;
	private TextView tvStatus;
	private boolean connectionProblem;
	private boolean forcedStopDetecting;

	public boolean isDetecting() {
		return detecting;
	}

	public void forceStopDetecting() {
		detecting = false;
		forcedStopDetecting = true;
		try {
			sc.getInputStream().close();
		} catch (Exception e) {
		}
		try {
			sc.getOutputStream().close();
		} catch (Exception e) {
		}
		try {
			sc.close();
		} catch (Exception e) {
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View rootView = inflater.inflate(
				illusion.songfeeler.R.layout.fragment_home, container, false);

		final ImageButton ibtnDetect = (ImageButton) rootView
				.findViewById(illusion.songfeeler.R.id.ibtnDetect);
		tvStatus = (TextView) rootView
				.findViewById(illusion.songfeeler.R.id.tvStatus);
		ivDetectCircle = (ImageView) rootView
				.findViewById(illusion.songfeeler.R.id.ivDetectCircle);
		anim = (RotateAnimation) AnimationUtils.loadAnimation(getActivity(),
				R.anim.rotate);
		anim.setDuration(5000);

		ivDetectCircle.startAnimation(anim);

		ibtnDetect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO
				if (detecting) {
					return;
				}

				LayoutParams lp = ivDetectCircle.getLayoutParams();
				lp.height *= 1.5f;
				lp.width *= 1.5f;
				ivDetectCircle.setLayoutParams(lp);

				anim.cancel();
				anim.reset();
				anim.setDuration(3000);
				anim.start();
				tvStatus.setText(getResources()
						.getString(R.string.listening_en));
				new Thread(new Runnable() {

					@Override
					public void run() {
						SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
						String server = sharedPrefs.getString("prefServerAddress", null);
						int port = -1;
						if (server != null) {
							server = server.trim();
							if (server.isEmpty()) server = null;
							if (server != null) {
								int pos = server.lastIndexOf(":");
								if (pos != -1) {
									try {
										port = Integer.parseInt(server.substring(pos + 1));
									} catch (Exception e) {
										port = -1;
									}
									server = server.substring(0, pos);
									server = server.trim();
									if (server.isEmpty()) server = null;

								}
							}
						}
						Log.d("REMOTE", server + " " + port);
						Song song = null;
						connectionProblem = true;
						try {
							Log.d(HomeFragment.class.getSimpleName(),
									"new detect request");
							detecting = true;
							forcedStopDetecting = false;
							DetectStream detectStream = new DetectStream(
									DURATION, SAMPLE_RATE);

							sc = new Socket(server != null ? server : DETECTOR_ADDRESS, port != -1 ? port : DETECTOR_PORT);
							sc.setSendBufferSize(4096);
							SampleRecorder recorder = new SampleRecorder(
									detectStream);
							InputStreamProcessor inputProcessor = new InputStreamProcessor(
									detectStream, sc.getInputStream());
							OutputStreamProcessor outputStreamProcessor = new OutputStreamProcessor(
									detectStream, sc.getOutputStream());
							Log.d(HomeFragment.class.getSimpleName(),
									"initiated");
							recorder.startRecording();
							inputProcessor.startInputing();
							outputStreamProcessor.startOutputing();

							inputProcessor.join(MAX_DURATION * 1000);

							song = inputProcessor.getResult();
							connectionProblem = inputProcessor.isInputing();
						} catch (Exception e) {
							Log.e(HomeFragment.class.getSimpleName(),
									e.getMessage());
						} finally {
							detecting = false;
							try {
								sc.getInputStream().close();
							} catch (Exception e) {
							}
							try {
								sc.getOutputStream().close();
							} catch (Exception e) {
							}
							try {
								sc.close();
							} catch (Exception e) {
							}
							try {
								getActivity().runOnUiThread(new Runnable() {

									@Override
									public void run() {
										LayoutParams lp = ivDetectCircle
												.getLayoutParams();
										lp.height /= 1.5f;
										lp.width /= 1.5f;
										ivDetectCircle.setLayoutParams(lp);
										anim.cancel();
										anim.reset();
										anim.setDuration(5000);
										anim.start();
										tvStatus.setText(getResources()
												.getString(R.string.identify_en));
									}
								});
							} catch (Exception e) {
								Log.e(getClass().getSimpleName(),
										e.getMessage());
							}
						}

						if (forcedStopDetecting) {
							return;
						}
						if (song == null) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											getActivity(),
											connectionProblem ? R.string.connection_error_en
													: R.string.no_match_en,
											Toast.LENGTH_LONG).show();
								}
							});
						} else {
							long _id = dbHelper.insertSong(song);
							if (_id == -1) {
								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getActivity(),
												R.string.db_error_en,
												Toast.LENGTH_LONG).show();
									}
								});
							} else {
								Intent i = new Intent(getActivity(),
										SongDetailActivity.class);
								Bundle b = new Bundle();
								b.putLong(IllusionSQLiteOpenHelper.CL_ID, _id);
								i.putExtras(b);
								startActivity(i);
							}
						}
					}
				}).start();
			}
		});
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		dbHelper = new IllusionSQLiteOpenHelper(activity);
	}
}
