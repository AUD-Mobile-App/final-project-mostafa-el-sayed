package com.elsayed.mustafa.bucket;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.elsayed.mustafa.bucket.BucketListActivity.userID;
import static com.elsayed.mustafa.bucket.BucketListActivity.chosenItem;
import static com.elsayed.mustafa.bucket.BucketListActivity.isEditing;
import static com.elsayed.mustafa.bucket.MainActivity.databaseReference;

public class AddAndEditItemActivity extends AppCompatActivity implements OnMapReadyCallback
{
    // global variables
    private GoogleMap mMap;
    private Marker chosenPoint;
    private Date chosenDate = new Date();
    // GUI variables
    private EditText titleEditText, descriptionEditText;
    private TextView latitudeTextView, longitudeTextView;
    private Button dateButton, saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_item);
        // setup toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // back clicked
        myToolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        // initialize global variables
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        latitudeTextView = (TextView)findViewById(R.id.latitudeTextView);
        longitudeTextView = (TextView)findViewById(R.id.longitudeTextView);
        dateButton = (Button) findViewById(R.id.dateButton);
        saveButton = (Button) findViewById(R.id.saveButton);

        // set date button to today's date
        DateFormat dateFormat = DateFormat.getDateInstance();
        dateButton.setText(dateFormat.format(new Date()));

        // populate EditTexts, only if editing
        if(isEditing)
        {
            // title
            titleEditText.setText(chosenItem.title);
            // description
            descriptionEditText.setText(chosenItem.description);
            // date
            chosenDate = chosenItem.dueDate;
            dateButton.setText(dateFormat.format(chosenDate));
        }
    }
    // fires when Google Maps is ready
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        // initialize mao instance
        mMap = googleMap;

        // show item location
        LatLng pos;
        if (isEditing)
        {
            pos = new LatLng(chosenItem.latitude, chosenItem.longitude);
        }
        else
        {
            pos = new LatLng(25.090877, 55.156412);
        }
        chosenPoint = mMap.addMarker(new MarkerOptions().position(pos).title("point"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
        // set gui
        longitudeTextView.setText("Long.: " + String.format("%.5f", chosenPoint.getPosition().longitude));
        latitudeTextView.setText("Lat.: " + String.format("%.5f", chosenPoint.getPosition().latitude));

        initializeMapClicks();
    }


    // GUI
    public void dateClicked(View view)
    {
        final Calendar calendar = Calendar.getInstance();
        final DateFormat dateFormat = DateFormat.getDateInstance();

        // fires when date is chosen
        final DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) // date set
            {
                // set date value
                calendar.set(year, month, dayOfMonth);
                chosenDate = calendar.getTime();
                // update GUI
                dateButton.setText(dateFormat.format(calendar.getTime()));
            }
        };

        // show calendar
        if (isEditing)
        {
            calendar.setTime(chosenItem.dueDate);
        }

        new DatePickerDialog(AddAndEditItemActivity.this, datePicker, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void saveClicked(View view)
    {
        // ensure fields are not empty
        if (titleEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input a title", Toast.LENGTH_LONG).show();
            return;
        }
        if (descriptionEditText.getText().toString().length() == 0)
        {
            Toast.makeText(this, "Please input a description", Toast.LENGTH_LONG).show();
            return;
        }

        // create a new bucket list item
        BucketItem bucketItem = new BucketItem();
        bucketItem.title = titleEditText.getText().toString();
        bucketItem.description = descriptionEditText.getText().toString();
        bucketItem.dueDate = chosenDate;
        bucketItem.checked = false; // default state is false
        bucketItem.latitude = chosenPoint.getPosition().latitude;
        bucketItem.longitude = chosenPoint.getPosition().longitude;

        // create new item or edit existing one
        if(isEditing)
        {
            // edit
            databaseReference.child(userID).child("items").child(chosenItem.itemID).setValue(bucketItem);
        }
        else
        {
            // create new
            databaseReference.child(userID).child("items").push().setValue(bucketItem);
        }
        Toast.makeText(this, "Item saved successfully", Toast.LENGTH_LONG).show();
        finish();
    }

    // FUNCTIONS
    void initializeMapClicks()
    {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng point)
            {
                chosenPoint.remove(); // remove previous point
                chosenPoint = mMap.addMarker(new MarkerOptions().position(point).title("point")); // update point
                // set gui
                longitudeTextView.setText("Long.: " + String.format("%.5f", chosenPoint.getPosition().longitude));
                latitudeTextView.setText("Lat.: " + String.format("%.5f", chosenPoint.getPosition().latitude));
            }
        });
    }
}
