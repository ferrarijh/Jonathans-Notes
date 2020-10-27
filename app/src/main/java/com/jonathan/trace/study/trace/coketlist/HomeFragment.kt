package com.jonathan.trace.study.trace.coketlist

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.jonathan.trace.study.trace.coketlist.dialog.DeleteMultiDialog
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
import com.jonathan.trace.study.trace.coketlist.dialog.PwDialog
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment() {

    private val nViewModel by lazy{ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)}
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var selectDialog: AlertDialog
    private lateinit var sortDialog: AlertDialog
    private lateinit var pwDialog: PwDialog
    private lateinit var deleteMultiDialog: DeleteMultiDialog
    private val btnDeleteMulti by lazy{requireActivity().findViewById<CardView>(R.id.cv_delete_multi)}
    private val parent by lazy{requireActivity() as AppCompatActivity}

    private val appear: Animation by lazy{ AnimationUtils.loadAnimation(context, R.anim.btn_appear)}
    private val disappear: Animation by lazy{ AnimationUtils.loadAnimation(
        context,
        R.anim.global_disappear
    )}

    private val sharedPreference by lazy {
        parent.getSharedPreferences("Sort", Context.MODE_PRIVATE)
    }

    private val spEditor by lazy{sharedPreference.edit()}

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

        hideKeyboard()
        setViewModel()
        setNotes()
        setAdapter()
        setDialog()
        setFAB()
        setAppBar()
        setDrawer(true)
        setWithMultiSelState()
        setBtn()
        setAnimation()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_actions, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_search -> goToSearch()
            R.id.action_sort -> sortDialog.show()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let{
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun setBtn(){
        btnDeleteMulti.setOnClickListener{
            if(nViewModel.selected.isEmpty())
                Toast.makeText(context, getString(R.string.nothing_to_delete), Toast.LENGTH_SHORT).show()
            else
                deleteMultiDialog.show()
        }
    }

    private fun setViewModel(){
        //load shared preference
        val prevSort = sharedPreference.getInt("Sort", NoteViewModel.MODIFIED)
        nViewModel.setPrevSort(prevSort)
    }

    private fun setNotes(){
        //setup with saved sort state
        notes = getNotesBy(nViewModel.prevSort.value!!)

        nViewModel.prevSort.observe(viewLifecycleOwner){
            notes = getNotesBy(it)
            notes.observe(viewLifecycleOwner){ list ->
                adapter.updateList(list)
            }
            spEditor.putInt("Sort", it)
            spEditor.commit()
        }
    }

    private fun getNotesBy(criteria: Int): LiveData<List<Note>>{
        return when(criteria){
            NoteViewModel.MODIFIED -> nViewModel.getAllNotes()
            NoteViewModel.CREATED -> nViewModel.getAllNotesByCreated()
            NoteViewModel.TITLE -> nViewModel.getAllNotesByTitle()
            NoteViewModel.BODY -> nViewModel.getAllNotesByBody()
            NoteViewModel.COLOR -> nViewModel.getAllNotesByColor()
            else -> nViewModel.getAllNotes()
        }
    }

    private fun setDrawer(toggle: Boolean){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        if(toggle)
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        else
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this){
            if(nViewModel.getSelMode() == NoteViewModel.OFF)
                requireActivity().finish()
            else
                nViewModel.setSelMode(NoteViewModel.OFF)
        }
    }

    private fun setAppBar(){
        val toolBar = parent.findViewById<Toolbar>(R.id.toolbar)
        val ivHome = toolBar.findViewById<ImageView>(R.id.iv_hamburger)
        ivHome.let {
            it.setImageResource(R.mipmap.ic_hamburger_foreground)
            it.setOnClickListener{ _ ->
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
        fabAdd = parent.findViewById(R.id.fab_add)
        if(nViewModel.getSelMode() != NoteViewModel.OFF)
            fabAdd.show()

        fabAdd.setOnClickListener{
            goToEditNoteNew()
        }

        rv_notes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (nViewModel.getSelMode() == NoteViewModel.OFF) {
                    if (dy > 0)
                        fabAdd.hide()
                    else
                        fabAdd.show()
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    private fun setDialog() {

        //warnDialog
        warnDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_trash)) {  //pClickListener
            val selectedNote = nViewModel.getNotePointed()?.second
            selectedNote?.let {
                it.trash = 1
                nViewModel.update(it)
                Toast.makeText(context, getString(R.string.trashed), Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        //pwDialog
        pwDialog = PwDialog(requireContext()){
            val pw = pwDialog.findViewById<EditText>(R.id.et_pw_note).text.toString()
            val pwConfirm = pwDialog.findViewById<EditText>(R.id.et_pw_confirm_note).text.toString()
            if(pw.isBlank() || pwConfirm.isBlank()) {
                Toast.makeText(context, getString(R.string.warn_blank_pw), Toast.LENGTH_SHORT).show()
            }else if(pw != pwConfirm){
                Toast.makeText(context, getString(R.string.pw_confirm_mismatch), Toast.LENGTH_SHORT).show()
            }
            else {
                setPw(pw)
                pwDialog.dismiss()
            }
        }

        //selectDialog
        val trash = getString(R.string.trash)
        val setPw = getString(R.string.set_pw)
        val multiSel = getString(R.string.multi_sel)

        val sel = arrayOf(multiSel, setPw, trash)
        val builderSel = AlertDialog.Builder(requireContext())
        builderSel.setItems(sel) { _, i ->
            when (sel[i]) {
                setPw -> pwDialog.show()
                trash -> warnDeletion()
                multiSel -> enterMultiSelMode()
            }
        }
        builderSel.setOnDismissListener{
            if(nViewModel.getSelMode() == NoteViewModel.OFF)
                nViewModel.setSelMode(NoteViewModel.OFF)    //to emit change to VH
        }
        selectDialog = builderSel.create()

        //sortDialog
        val sortTitle = getString(R.string.sort_by_title)
        val sortBody = getString(R.string.sort_by_body)
        val sortCreated = getString(R.string.sort_by_time_created)
        val sortModified = getString(R.string.sort_by_time_modified)
        val sortColor = getString(R.string.sort_by_color)

        val sortSel = arrayOf(sortModified, sortCreated, sortTitle, sortBody, sortColor)
        val builderSort = AlertDialog.Builder(requireContext())
        builderSort.setItems(sortSel) { _, i ->
            when (sortSel[i]) {
                sortModified -> nViewModel.setPrevSort(NoteViewModel.MODIFIED)
                sortCreated -> nViewModel.setPrevSort(NoteViewModel.CREATED)
                sortTitle -> nViewModel.setPrevSort(NoteViewModel.TITLE)
                sortBody -> nViewModel.setPrevSort(NoteViewModel.BODY)
                sortColor -> nViewModel.setPrevSort(NoteViewModel.COLOR)
                else -> nViewModel.getAllNotes()
            }
        }
        sortDialog = builderSort.create()

        //multi-delete dialog
        deleteMultiDialog = DeleteMultiDialog(requireContext()){
            val newListLive = nViewModel.deleteSelectedAndUpdateWith()
            newListLive.observe(viewLifecycleOwner){
                adapter.updateList(it)
            }
            deleteMultiDialog.dismiss()
        }
    }

    private fun setWithMultiSelState(){
        //set delete-multi button
        if(nViewModel.getSelMode() == NoteViewModel.ON) {
            btnDeleteMulti.startAnimation(appear)
            fabAdd.hide()
            parent.supportActionBar!!.hide()
        }

        nViewModel.selMode.observe(viewLifecycleOwner){
            if(it == NoteViewModel.ON) {
                btnDeleteMulti.visibility = View.VISIBLE
                btnDeleteMulti.startAnimation(appear)

                fabAdd.hide()
                parent.supportActionBar!!.hide()
                setDrawer(false)
            }
            else {
                if(btnDeleteMulti.visibility == View.VISIBLE)
                    btnDeleteMulti.startAnimation(disappear)

                fabAdd.show()
                parent.supportActionBar!!.show()
                setDrawer(true)
            }
        }
    }

    private fun enterMultiSelMode(){
        nViewModel.setSelMode(NoteViewModel.ON)
        val firstElem = nViewModel.getNotePointed()!!
        nViewModel.toggleSelectedWith(firstElem.first, firstElem.second)
    }

    private fun warnDeletion(){
        warnDialog.show()
    }

    private fun setPw(s: String){
        val selectedNote = nViewModel.getNotePointed()!!.second
        selectedNote.pw = s
        nViewModel.update(selectedNote)
        Toast.makeText(context, getString(R.string.pw_set), Toast.LENGTH_SHORT).show()
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            //notes.value as MutableList<Note>? ?:
            mutableListOf<Note>(),
            R.layout.thumbnail,
            ThumbnailAdapter.HOME,
            object : ThumbnailAdapter.ThumbnailAdapterListener {
                override fun <T> onClickItem(item: T) {
                    goToEditNoteWith()
                }
            },
            object : ThumbnailAdapter.ThumbnailAdapterLongListener {
                override fun <T> onLongClickItem(item: T) {
                    selectDialog.show()
                }
            }
        )

        rv_notes.adapter = adapter

        val ori = requireActivity().resources.configuration.orientation
        if(ori == Configuration.ORIENTATION_LANDSCAPE)
            rv_notes.layoutManager = StaggeredGridLayoutManager(
                3,
                StaggeredGridLayoutManager.VERTICAL
            )
        else if (ori == Configuration.ORIENTATION_PORTRAIT)
            rv_notes.layoutManager = StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
    }

    private fun setAnimation(){
        disappear.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                btnDeleteMulti.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

        })
    }

    private fun goToEditNoteNew(){
        fabAdd.setOnClickListener(null) //to prevent crash on double click
        fabAdd.hide()

        val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
        action.isNew = true
        findNavController().navigate(action)
    }

    private fun goToEditNoteWith(){
        fabAdd.hide()

        val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
        findNavController().navigate(action)
    }

    private fun goToTrashCan(){
        fabAdd.hide()

        val action = HomeFragmentDirections.actionHomeFragmentToTrashCanFragment()
        findNavController().navigate(action)
    }

    private fun goToPrivate(){
        fabAdd.hide()

        val action = HomeFragmentDirections.actionHomeFragmentToPrivateFragment()
        findNavController().navigate(action)
    }

    private fun goToSearch(){
        fabAdd.setOnClickListener(null) //to prevent crash on double click
        fabAdd.hide()

        val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
        findNavController().navigate(action)
    }
}