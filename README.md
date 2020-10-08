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

# ROOM DB-Repository-ViewModel pattern

app architecture guide:
https://developer.android.com/jetpack/guide?hl=ko

## Repository
* properties - service instance (ex. dao instance, webservice instance)
* methods - fetch specific data (ex. fun getUser(): LiveData<User>)

## ViewModel
* properties - LiveData objects which views will observe. 