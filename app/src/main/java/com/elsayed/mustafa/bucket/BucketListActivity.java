package com.elsayed.mustafa.bucket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.elsayed.mustafa.bucket.MainActivity.firebaseAuth;
import static com.elsayed.mustafa.bucket.MainActivity.databaseReference;

public class BucketListActivity extends AppCompatActivity
{
    // global variables
    ArrayList<BucketItem> listOfBucketItems; // contains bucket items
    CustomAdapter listViewAdapter; // adapter for listview
    public static String userID = "";
    public static BucketItem chosenItem;
    public static boolean isEditing = false;
    // GUI elements
    ListView listView;

    // constructor
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_list);

        // initialize global variables
        listOfBucketItems = new ArrayList<>();
        listViewAdapter = new CustomAdapter(listOfBucketItems);
        userID = firebaseAuth.getCurrentUser().getUid();
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listViewAdapter);

        // create new item button
        FloatingActionButton newItemButton = (FloatingActionButton) findViewById(R.id.newItemButton);
        newItemButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                isEditing = false;
                startActivity(new Intent(BucketListActivity.this, AddAndEditItemActivity.class));
            }
        });

        // edit item (when its clicked)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                chosenItem = listOfBucketItems.get(position);
                isEditing = true;
                startActivity(new Intent(BucketListActivity.this, AddAndEditItemActivity.class));
            }
        });

        // set listener for when data changes on firebase
        setDataListener();
    }
    // menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    // menu option selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.logout)
        {
            // signout
            FirebaseAuth.getInstance().signOut();
            // navigate to main login page
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return true;
    }


    // FUNCTIONS
    void setDataListener()
    {
        // fires when database items change
        databaseReference.child(userID).child("items").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // clear the list
                listOfBucketItems.clear();
                for (DataSnapshot imageSnapshot: dataSnapshot.getChildren()) // loop over items
                {
                    // get the bucket item
                    BucketItem tempBucketItem = imageSnapshot.getValue(BucketItem.class);
                    // set key unique key of item
                    tempBucketItem.itemID = imageSnapshot.getKey();
                    // add it to list
                    listOfBucketItems.add(tempBucketItem);
                }
                // sort the list according to date and check-state
                sortList();
                // update listview
                listViewAdapter.notifyDataSetChanged();

                if (listOfBucketItems.size() == 0)
                    Toast.makeText(BucketListActivity.this, "Empty list!", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // nothing
            }
        });
    }
    public void sortList()
    {
        // sort list by items due date
        Collections.sort(listOfBucketItems, new Comparator<BucketItem>()
        {
            public int compare(BucketItem itemA, BucketItem itemB)
            {
                if (itemA.dueDate == null || itemB.dueDate == null)
                {
                    return 0;
                }
                else
                {
                    return itemA.dueDate.compareTo(itemB.dueDate);
                }
            }
        });

        // move checked items to the bottom
        for (int i = 0; i < listOfBucketItems.size(); i++)
        {
            for (int j = listOfBucketItems.size()-1; j >= 0; j--)
            {
                if(listOfBucketItems.get(j).checked == true)
                {
                    listOfBucketItems.add(listOfBucketItems.get(j));
                    listOfBucketItems.remove(j);
                }
            }
        }
    }

    // ListView adapter
    public class CustomAdapter extends BaseAdapter
    {
        private ArrayList<BucketItem> bucketItems;
        public CustomAdapter(ArrayList<BucketItem> bucketItems)
        {
            this.bucketItems = bucketItems;
        }
        @Override
        public int getCount()
        {
            return bucketItems.size();
        }
        @Override
        public Object getItem(int i)
        {
            return null;
        }
        @Override
        public long getItemId(int i)
        {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup)
        {
            view = getLayoutInflater().inflate(R.layout.mylist, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);
            TextView dateTextView = (TextView) view.findViewById(R.id.dateTextView);

            // detect clicks on checkboxes
            checkBox.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    BucketItem item = listOfBucketItems.get(i);
                    // update local bucket items
                    item.checked = checkBox.isChecked();
                    // update on database
                    databaseReference.child(userID).child("items").child(item.itemID).setValue(item);
                }
            });

            // set GUI attributes
            // Checkbox
            if(bucketItems.get(i).checked)
            {
                checkBox.setChecked(true);
            }
            else
            {
                checkBox.setChecked(false);
            }
            // date
            DateFormat dateFormat = DateFormat.getDateInstance();
            dateTextView.setText(dateFormat.format(bucketItems.get(i).dueDate));
            // title
            titleTextView.setText(bucketItems.get(i).title);
            return view;
        }
    }

}
