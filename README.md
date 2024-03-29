# Jonathan's Notes for Android

Jonathan's Notes is note/diary app for Android.

(playstore: https://play.google.com/store/apps/details?id=com.jonathan.jonathans.notes)

## RoomDB-Repository-ViewModel pattern

app architecture guide:
https://developer.android.com/jetpack/guide?hl=ko

## ROOM DB

### Add table with Foreign key (one-to-many)

New table entity:
```kotlin
@Entity(tableName = "image_table", foreignKeys = [ForeignKey(entity=Note::class, parentColumns = ["id"], childColumns = ["noteId"], onDelete = CASCADE)])
data class Image(
    @PrimaryKey
    var path: String,
    var noteId: Int
)
```

Migration:
```kotlin
    private val MIGRATION_5_6 = object : Migration(5,6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS image_table(noteId INTEGER NOT NULL, path VARCHAR NOT NULL, PRIMARY KEY(path), FOREIGN KEY(noteId) REFERENCES note_table(id) ON DELETE CASCADE)"
            )
        }
    }
```

### Migration
* For ```IllegalStateException``` from migration, stacktrace will print out two parts like below:
```
     Expected:
    TableInfo{name='image_table', columns={path=Column{name='path', type='TEXT', affinity='2', notNull=true, primaryKeyPosition=1, defaultValue='null'}, noteId=Column{name='noteId', type='INTEGER', affinity='3', notNull=true, primaryKeyPosition=0, defaultValue='null'}}, foreignKeys=[ForeignKey{referenceTable='note_table', onDelete='CASCADE', onUpdate='NO ACTION', columnNames=[noteId], referenceColumnNames=[id]}], indices=[]}
     Found:
    TableInfo{name='image_table', columns={path=Column{name='path', type='VARCHAR', affinity='2', notNull=false, primaryKeyPosition=1, defaultValue='null'}, noteId=Column{name='noteId', type='INTEGER', affinity='3', notNull=false, primaryKeyPosition=0, defaultValue='null'}}, foreignKeys=[ForeignKey{referenceTable='note_table', onDelete='NO ACTION', onUpdate='NO ACTION', columnNames=[noteId], referenceColumnNames=[id]}], indices=[]}
```
Here,
```Expected```: defined data class
```Found```: Migration scheme

## Repository
* properties - service instance (ex. dao instance, webservice instance)
* methods - fetch specific data (ex. fun getUser(): LiveData<User>)

## ViewModel
* properties - LiveData objects on which views will observe. 

# LiveData

### When NOT to use LiveData in ViewModel?
* when multiple views are not interacting each other there's no need to use LiveData. (UNLESS you want to recover view states on rotation)

### Extra
* when updating collection of instances in adapter class with another list wrapped with LiveData container,
be aware that this new list should be handled asynchronously.
Thus, below code will trigger NullPointerException.

Bad:
```kotlin
    val newListLive: LiveData<List<User>> = myViewModel.getUsers()    //fetched from ROOM DB
    myAdapter.submitList(newListLive.value!!)    //NO NO!
```

```newListLive.value``` is asynchronously updating but ```myAdapter``` tries to take the value of newListLive
right away, before newListLive has finished fetching all data. By the time ```myAdapter``` tries to access
```newListLive.value``` the value is null so this will trigger NullPointerException.
The right way to do is at below.

Good:
```kotlin
    val newListLive: LiveData<List<User>> = myViewModel.gerUsers()
    newListLive.observe(viewLifeCycleOwner){
        adapter.submitList(it)
    }
```

* If observer is not set on a LiveData object, the object won't update values. For example, if
```LiveData<List<User>>``` is returned by Dao but no observer is set then the value of the LiveData object will always be null.

# UI components

## Collapsing Toolbar with RecyclerView
* Remember: CoordinatorLayout → AppbarLayout → Toolbar

### RecyclerView
* set layout manager type(e.g. LinearLayoutManager, GridLayoutManager)
* ```OnScrollListener``` can be added for various needs (ex. hiding FAB). See below:
```kotlin
        rv_main.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0)
                    fab.hide()
                else
                    fab.show()

                super.onScrolled(recyclerView, dx, dy)
            }
        })
```

### AppBarLayout
* CORRECT: ```layout_height="wrap_content"```, WRONG: ```layout_height="match_parent"```

* Child of (vertical)LinearLayout.
* Depends HEAVILY on being used as direct child of CoordinatorLayout

### Toolbar
* ```setSupportActionBar(toolbar)``` is used along with ```onOptionsItemSelected()``` and ```override fun onCreateOptionsMenu()```.
If ```setSupportActionBar(toolbar)``` is called then ```toolbar.inflaterMenu()``` is unnecessary since
Toolbar is working as ActionBar. If the former is not called the latter should be used with
```toolbar.setOnMenuItemClicklistener()```.

How it's done here:
1) ```setSupportActionBar(toolbar)``` in MainActivity
2) override ```fun onCreateOptionsMenu(...)```
3) override ```fun onOptionsItemSelected(...)```

* For some reason a small gap exists on the left of the left-end child view of Toolbar. Add below line to remove it.
```app:contentInsetStart="0dp"```

* To disable shadow below toolbar, set ```app:elevation="0dp"``` in AppBarLayout like below.
```
        <com.google.android.material.appbar.AppBarLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:elevation="0dp">
```

### FragmentContainerView
* FOR NAVIGATION GRAPH USE <fragment>, NOT THIS(in activity xml where HomeFragment is at). - Else, nav.xml does not acknowledge activity)
* Container for Fragments. Extends FrameLayout.
* Original FrameLayout had problem with properly showing exiting animation of Fragment. FCV fixes this issue.

### Extra
* parent.supportActionBar is lost after rotation so set it back like below:
```kotlin
    val parent = requireActivity() as AppCompatActivity
    parent.supportActionBar = parent.findViewById<Toolbar>(R.id.my_toolbar)
    supportActionBar?.apply{
        doSomething()
    }
```

* Change visibility by ```fab.hide()```, NOT ```fab.visibility = View.INVISIBLE```. (Latter is buggy)

* To customize Dialog,
1) set layout_gravity in xml for position.
2) to set margin do below. (10% here)
```kotlin
        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val newX = context.resources.displayMetrics.widthPixels     //screen pixel
            val newY = context.resources.displayMetrics.heightPixels
            setLayout((newX*0.9).toInt(), (newY*0.9).toInt())
            Log.d("", "layout: $newX, $newY")
        }
```
3) to adjust 'parent view'(window) dimension do below.
```kotlin
        val lp = window?.attributes
        lp?.apply{
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.8f
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
```
By default window is whole screen for app. (area except notification bar and bottom bar)

* ```fillAfter=true``` unnecessary if the animation is 'hiding' type of animation and target view's default visibility is ```visibility="invisible"``` in XML.

# Furthermore..
* Iterating through Collection without iterators to remove entry will trigger `ConcurrentModificationException`.

Bad: (will trigger `ConcurrentModificationException`)
```kotlin
    myMap.forEach{
        myMap.remove(it.key)
    }
```

Good:
```kotlin
    val iter = myMap.iterator()
    while(iter.hasNext()){
        iter.next()
        iter.remove()
    }
```

* ViewPager2 glitch with ListAdapter - Calling ```.submitList(newList)``` after data deletion does not properly set
```viewPager2.currentItem```. Thus if page transformer is attached page positions displayed are spoiled after the deletion.
Solved simply by using ```RecyclerView.Adapter```.
