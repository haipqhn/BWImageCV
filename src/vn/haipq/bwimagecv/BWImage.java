package vn.haipq.bwimagecv;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class BWImage extends ActionBarActivity {
	static {
		System.loadLibrary("opencv_java");
	}
	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private Bitmap bmp = null;
	BitmapFactory.Options options ;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int PICK_FROM_FILE = 2;

	public native void convertToGrayscale(Bitmap bitmapIn, Bitmap bitmapOut);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupActionBar();
		mImageView = (ImageView) findViewById(R.id.ivMain);
	}

	private void setupActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle("");
		actionBar.setHomeButtonEnabled(true);
	}

	private void refreshImage() {
		if (bmp == (null)) {
			mImageView.setImageBitmap(bmp);
		}
	}

	private void openImage() {
		final String[] items = new String[] { "From Camera", "From Storage" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.select_dialog_item, items);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Select Image");
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					File file = new File(Environment
							.getExternalStorageDirectory(), "tmp_avatar_"
							+ String.valueOf(System.currentTimeMillis())
							+ ".jpg");
					mImageCaptureUri = Uri.fromFile(file);

					try {
						intent.putExtra(
								android.provider.MediaStore.EXTRA_OUTPUT,
								mImageCaptureUri);
						intent.putExtra("return-data", true);

						startActivityForResult(intent, PICK_FROM_CAMERA);
					} catch (Exception e) {
						e.printStackTrace();
					}

					dialog.cancel();
				} else {
					Intent intent = new Intent();

					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);

					startActivityForResult(Intent.createChooser(intent,
							"Complete action using"), PICK_FROM_FILE);
				}
			}
		});

		final AlertDialog dialog = builder.create();

		dialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_refresh:
			refreshImage();
			return true;
		case R.id.action_openimage:
			openImage();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bitmap = null;
		options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;

		if (resultCode == RESULT_OK) {
			Uri imageFileUri = data.getData();
			try {
				BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
				bmpFactoryOptions.inJustDecodeBounds = true;
				bmp = BitmapFactory
						.decodeStream(
								getContentResolver().openInputStream(
										imageFileUri), null, bmpFactoryOptions);

				bmpFactoryOptions.inJustDecodeBounds = false;
				bmp = BitmapFactory
						.decodeStream(
								getContentResolver().openInputStream(
										imageFileUri), null, bmpFactoryOptions);
				bitmap = bmp;

				Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(),
						CvType.CV_8UC3);
				Utils.bitmapToMat(bitmap, mat);
				Mat mat1 = new Mat(bmp.getHeight(), bmp.getWidth(),
						CvType.CV_8UC1);
				Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);
				Utils.matToBitmap(mat1, bitmap);
				mImageView.setImageBitmap(bitmap);
			} catch (Exception e) {
				Log.v("ERROR", e.toString());
			}
		}
	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	public byte[] convertBitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream(
				bitmap.getWidth() * bitmap.getHeight());
		bitmap.compress(CompressFormat.PNG, 100, buffer);
		return buffer.toByteArray();
	}
}
