## Collapsing Toolbar with RecyclerView
* Remember: CoordinatorLayout → AppbarLayout → Toolbar

### RecyclerView
* set layout manager type(e.g. LinearLayoutManager, GridLayoutManager)

### AppBarLayout
* CORRECT: ```layout_height="wrap_content"```, WRONG: ```layout_height="match_parent"```

* Child of (vertical)LinearLayout.
* Depends HEAVILY on being used as direct child of CoordinatorLayout

### Toolbar
* If ```setSupportActionBar(toolbar)``` is called, ```toolbar.inflaterMenu()``` is unnecessary since
Toolbar is working as ActionBar. If the former is not called the latter should be used with
```toolbar.setOnMenuItemClicklistener()```.

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
* Container for Fragments. Extends FrameLayout.
* Original FrameLayout had problem with properly showing exiting animation of Fragment. FCV fixes this issue.