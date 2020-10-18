# ROOM DB-Repository-ViewModel pattern

app architecture guide:
https://developer.android.com/jetpack/guide?hl=ko

## Repository
* properties - service instance (ex. dao instance, webservice instance)
* methods - fetch specific data (ex. fun getUser(): LiveData<User>)

## ViewModel
* properties - LiveData objects on which views will observe. 

# LiveData

### When NOT to use LiveData in ViewModel?
* when multiple views are not interacting each other there's no need to use LiveData.

### Extra
* when updating collection of instance in adapter class with another list wrapped with LiveData container,
be aware that this new list should be handled asynchronously.
Thus, below code will trigger NullPointerException.

Bad:
```kotlin
    val newListLive = myViewModel.getUsers()    //fetched from ROOM DB
    myAdapter.submitList(newListLive.value!!)    //NO NO!
```

```newListLive.value``` is asynchronously updating but ```myAdapter``` tries to take the value of newListLive
right away, before newListLive has finished fetching all data. By the time ```myAdapter``` tries to access
```newListLive.value``` the value is null so this will trigger NullPointerException.
The right way to do is at below.

Good:
```kotlin
    val newListLive = myViewModel.gerUsers()
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
```xml
        <com.google.android.material.appbar.AppBarLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            app:elevation="0dp">
        (...)
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
* Iterating through Collection without iterators to remove entry will trigger ConcurrentModificationException.

Bad: (will trigger ConcurrentModificationException)
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

# Unsolved
* LiveData retrieved by ROOM db seems to update automatically after change in dataset?