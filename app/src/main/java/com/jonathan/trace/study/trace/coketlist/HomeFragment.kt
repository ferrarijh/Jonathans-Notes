package com.jonathan.trace.study.trace.coketlist

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var selectDialog: AlertDialog
    private lateinit var sharedSortState: LiveData<NoteViewModel.SortState>
    private lateinit var pwDialog: AlertDialog
    private var selectedNote: Note? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setOnBackPressed()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        rv_notes.setOnTouchListener{ _, _ ->
            Log.d("", "mViewModel.getAllNotes.value.size: ${mViewModel.getAllNotes.value!!.size}")
            Log.d("", "mViewModel.getAllNotesByCreated.value.size: ${mViewModel.getAllNotesByCreated.value!!.size}")
            Log.d("", "mViewModel.getAllNotesByTitle.value.size: ${mViewModel.getAllNotesByTitle.value!!.size}")
            Log.d("", "mViewModel.getAllNotesByBody.value.size: ${mViewModel.getAllNotesByBody.value!!.size}")
            Log.d("", "mVIewModel.sortState: ${mViewModel.sortState.value}")
            Log.d("", "adapter data size: ${adapter.thumbnailsVisible.size}")
            true
        }

         */

        mViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        sharedSortState = mViewModel.sortState
        notes = mViewModel.getAllNotes()
        setDialog()
        setAdapter()
        setFAB()
        setAppBar()
        setNavView()
        setDrawer()

        sharedSortState.observe(viewLifecycleOwner){
            Log.d("", "sortState observed: $it")
            notes = when(it){
                NoteViewModel.SortState.MODIFIED -> mViewModel.getAllNotes()
                NoteViewModel.SortState.CREATED -> mViewModel.getAllNotesByCreated()
                NoteViewModel.SortState.TITLE -> mViewModel.getAllNotesByTitle()
                NoteViewModel.SortState.BODY -> mViewModel.getAllNotesByBody()
            }
            notes.observe(viewLifecycleOwner){ lis ->
                adapter.updateList(lis)
            }
        }

        Log.d("", "HomeFragment onViewCreated() - sortState: ${sharedSortState.value}")
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
                    Toast.makeText(context, "private!", Toast.LENGTH_SHORT).show()
                    val action = HomeFragmentDirections.actionHomeFragmentToPrivateFragment()
                    findNavController().navigate(action)
                }
                R.id.item_trash_can -> {
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
        (requireActivity() as AppCompatActivity).supportActionBar!!.show()
    }

    private fun setFAB(){
        val fabAdd = requireActivity().findViewById<FloatingActionButton>(R.id.fab_add)
        val fabSave = requireActivity().findViewById<FloatingActionButton>(R.id.fab_save)

        fabAdd.show()
        fabSave.hide()

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
                Toast.makeText(context, "Note moved to trash can.", Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        //selectDialog on longClick
        val etPw = EditText(context)
        etPw.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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

    }

    private fun warnDeletion(){
        warnDialog.show()
        warnDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion)
    }

    private fun setPw(s: String){
        selectedNote!!.pw = s
        /*
        Note(selectedNote!!.id, selectedNote!!.title, selectedNote!!.body, selectedNote!!.dateTimeCreated
            , selectedNote!!.dateTimeModified, selectedNote!!.trash, selectedNote!!.color, s)
         */
        mViewModel.update(selectedNote!!)
        Toast.makeText(context, "Password set.", Toast.LENGTH_SHORT).show()
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            notes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            object : ThumbnailAdapter.ThumbnailAdapterListener {
                override fun <T> onClickItem(item: T) {
                    requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)

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
        rv_notes.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

}