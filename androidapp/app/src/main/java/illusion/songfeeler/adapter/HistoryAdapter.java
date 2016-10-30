package illusion.songfeeler.adapter;

import illusion.songfeeler.R;
import illusion.songfeeler.activity.SongDetailActivity;
import illusion.songfeeler.db.IllusionSQLiteOpenHelper;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryAdapter extends CursorAdapter {

	private LayoutInflater inflater;

	public HistoryAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView ivAvarta = (ImageView) view
				.findViewById(R.id.ivHistoryAvatar);
		TextView tvTitle = (TextView) view.findViewById(R.id.tvHistoryTitle);
		TextView tvArtist = (TextView) view.findViewById(R.id.tvHistoryArtist);

		byte[] image = cursor.getBlob(cursor
				.getColumnIndex(IllusionSQLiteOpenHelper.CL_IMAGE));
		if (image.length > 0) {
			ivAvarta.setImageBitmap(BitmapFactory.decodeByteArray(image, 0,
					image.length));
		} else {
			ivAvarta.setImageBitmap(BitmapFactory.decodeResource(
					context.getResources(), R.drawable.img_no_avarta));
		}

		tvTitle.setText(cursor.getString(cursor
				.getColumnIndex(IllusionSQLiteOpenHelper.CL_TITLE)));
		tvArtist.setText(cursor.getString(cursor
				.getColumnIndex(IllusionSQLiteOpenHelper.CL_ARTIST)));

		final Context contextToPass = context;
		final long _id = cursor.getLong(cursor
				.getColumnIndex(IllusionSQLiteOpenHelper.CL_ID));
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent i = new Intent(contextToPass, SongDetailActivity.class);
				Bundle b = new Bundle();
				b.putLong(IllusionSQLiteOpenHelper.CL_ID, _id);
				i.putExtras(b);
				contextToPass.startActivity(i);
			}
		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		return inflater.inflate(R.layout.history_item, viewGroup, false);
	}
}
