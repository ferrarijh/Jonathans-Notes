package com.jonathan.trace.study.trace.coketlist

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.cache.NotesCache
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.dialog.*
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var selectDialog: AlertDialog
    private lateinit var sortDialog: AlertDialog
    private lateinit var pwDialog: AlertDialog
    private var selectedNote: Note? = null

    private lateinit var fabAdd: FloatingActionButton

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        setNotes()
        setAdapter()
        setDialog()
        setFAB()
        setAppBar()
        setNavView()
        setDrawer()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_actions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> {
                fabAdd.hide()

                val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
                findNavController().navigate(action)
            }
            R.id.action_sort -> {
                sortDialog.show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NotesCache.notes?.value?.apply{
            clear()
            addAll(notes.value!!)
        }
    }

    private fun setNotes(){
        NotesCache.notes?.let {
            notes = it as LiveData<List<Note>>
            return
        }
        notes = mViewModel.getAllNotes()

        notes.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        if(drawer.getDrawerLockMode(GravityCompat.START) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun setNavView(){
        requireActivity().findViewById<NavigationView>(R.id.nv).setNavigationItemSelectedListener {
            requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
            when(it.itemId){
                R.id.item_private_note -> {
                    fabAdd.hide()

                    val action = HomeFragmentDirections.actionHomeFragmentToPrivateFragment()
                    findNavController().navigate(action)
                }
                R.id.item_trash_can -> {
                    fabAdd.hide()

                    val action = HomeFragmentDirections.actionHomeFragmentToTrashCanFragment()
                    findNavController().navigate(action)
                }
            }
            requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer).close()
            true
        }
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this){
            requireActivity().finish()
        }
    }

    private fun setAppBar(){
        val parent = requireActivity() as AppCompatActivity
        val toolBar = parent.findViewById<Toolbar>(R.id.toolbar)
        val ivHome = toolBar.findViewById<ImageView>(R.id.iv_hamburger)
        ivHome.apply {
            setImageResource(R.drawable.hamburger)
            setOnClickListener{
                val drawer = parent.findViewById<DrawerLayout>(R.id.layout_drawer)
                drawer.openDrawer(GravityCompat.START)
            }
        }
        parent.apply {
            setSupportActionBar(toolBar)
            supportActionBar!!.apply{
                setDisplayShowTitleEnabled(false)
                setDisplayHomeAsUpEnabled(false)
                show()
            }
        }
    }

    private fun setFAB(){
        val parent = requireActivity()

        fabAdd = parent.findViewById(R.id.fab_add)
        fabAdd.show()

        fabAdd.setOnClickListener{
            fabAdd.hide()
            val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
            findNavController().navigate(action)
        }

        rv_notes.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0)
                    fabAdd.hide()
                else
                    fabAdd.show()

                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun setDialog() {

        //warnDialog
        warnDialog = MyDialog(requireContext()) {  //pClickListener
            selectedNote?.let {
                it.trash = 1
                mViewModel.update(it)
                Toast.makeText(context, getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        /*
        val warnBuilder = AlertDialog.Builder(requireContext())
        warnBuilder.setTitle(getString(R.string.warn_deletion))
            .setPositiveButton(getString(R.string.ok)){_, _ ->
                selectedNote?.let {
                    it.trash = 1
                    mViewModel.update(it)
                    Toast.makeText(context, "Note moved to trash can.", Toast.LENGTH_SHORT).show()
                }
            }.setNegativeButton(getString(R.string.cancel)){_, _->}
        warnDialog = warnBuilder.create()
         */

        //selectDialog on longClick
        val etPw = EditText(context)
        val lp = requireView().layoutParams as ViewGroup.MarginLayoutParams
        lp.apply{
            setMargins(15, 15, 15, 15)
        }
        lp.setMargins(20, 20, 20, 20)
        lp.width = ViewGroup.MarginLayoutParams.MATCH_PARENT
        lp.height = ViewGroup.MarginLayoutParams.WRAP_CONTENT

        etPw.layoutParams = lp
        val builderPw = AlertDialog.Builder(requireContext())
        builderPw.setView(etPw)
            .setTitle(getString(R.string.pw))
            .setPositiveButton("OK") { _, _ ->
                setPw(etPw.text.toString())
            }
        pwDialog = builderPw.create()

        val setPw = getString(R.string.set_pw)
        val delete = getString(R.string.delete)
        val sel = arrayOf(setPw, delete)
        val builderSel = AlertDialog.Builder(requireContext())
        builderSel.setItems(sel) { _, i ->
            when (sel[i]) {
                setPw -> pwDialog.show()
                delete -> warnDeletion()
            }
        }
        selectDialog = builderSel.create()

        val sortTitle = getString(R.string.sort_by_title)
        val sortBody = getString(R.string.sort_by_body)
        val sortCreated = getString(R.string.sort_by_time_created)
        val sortModified = getString(R.string.sort_by_time_modified)
        val sortColor = getString(R.string.sort_by_color)

        val sortSel = arrayOf(sortModified, sortCreated, sortTitle, sortBody, sortColor)
        val builderSort = AlertDialog.Builder(requireContext())
        builderSort.setItems(sortSel) { _, i ->
            notes = when (sortSel[i]) {
                sortModified -> mViewModel.getAllNotes()
                sortCreated -> mViewModel.getAllNotesByCreated()
                sortTitle -> mViewModel.getAllNotesByTitle()
                sortBody -> mViewModel.getAllNotesByBody()
                sortColor -> mViewModel.getAllNotesByColor()
                else -> mViewModel.getAllNotes()
            }
            notes.observe(viewLifecycleOwner){
                adapter.updateList(it)
            }
        }
        sortDialog = builderSort.create()
    }

    private fun warnDeletion(){
        warnDialog.show()
        warnDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion)
    }

    private fun setPw(s: String){
        selectedNote!!.pw = s
        mViewModel.update(selectedNote!!)
        Toast.makeText(context, "Password set.", Toast.LENGTH_SHORT).show()
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            notes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            object : ThumbnailAdapter.ThumbnailAdapterListener {
                override fun <T> onClickItem(item: T) {
                    requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)

                    fabAdd.hide()

                    val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
                    action.note = item as Note
                    findNavController().navigate(action)
                }
            },
            object : ThumbnailAdapter.ThumbnailAdapterLongListener {
                override fun <T> onLongClickItem(item: T) {
                    selectedNote = item as Note
                    selectDialog.show()
                }
            })

        rv_notes.adapter = adapter
        val ori = requireActivity().resources.configuration.orientation
        if(ori == Configuration.ORIENTATION_LANDSCAPE)
            rv_notes.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        else if (ori == Configuration.ORIENTATION_PORTRAIT)
            rv_notes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

}