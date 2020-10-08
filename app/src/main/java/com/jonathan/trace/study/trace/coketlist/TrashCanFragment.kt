package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailTrashAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.fragment_trash_can.*

class TrashCanFragment : Fragment() {

    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailTrashAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var selectDialog: AlertDialog
    private var selectedNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_trash_can, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        notes = mViewModel.getAllTrashNotes()
        setDialog()
        setAdapter()
        setFAB()
        setAppBar()
        setOnBackPressed()
        setDrawer()

        notes.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this){
            val action = TrashCanFragmentDirections.actionTrashCanFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun setAppBar(){
        (requireActivity() as AppCompatActivity).supportActionBar!!.hide()
    }

    private fun setFAB(){
        val fabAdd = requireActivity().findViewById<FloatingActionButton>(R.id.fab_add)
        val fabSave = requireActivity().findViewById<FloatingActionButton>(R.id.fab_save)
        fabAdd.hide()
        fabSave.hide()
    }

    private fun setDialog(){

        warnDialog = MyDialog(requireContext()){  //pClickListener
            selectedNote?.let {
                mViewModel.delete(it)
                Toast.makeText(context, "Note deleted.", Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        val permanentDeletion = getString(R.string.permanent_deletion)
        val recover = getString(R.string.recover)
        val items = arrayOf(permanentDeletion, recover)
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(items){_, b ->
            when(items[b]){
                permanentDeletion -> deleteNote()
                recover -> recoverNote()
            }
        }
        selectDialog = builder.create()
    }

    private fun deleteNote(){
        warnDialog.show()
        warnDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion_permanent)
    }

    private fun recoverNote(){
        selectedNote!!.trash = 0
        mViewModel.update(selectedNote!!)
    }

    private fun setAdapter(){
        adapter = ThumbnailTrashAdapter(
            notes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            object: ThumbnailTrashAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    val action = TrashCanFragmentDirections.actionTrashCanFragmentToViewModeFragment(item as Note)
                    requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
                    findNavController().navigate(action)
                }
            },
            object: ThumbnailTrashAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    selectedNote = item as Note
                    selectDialog.show()
                }
            })

        rv_notes_trash.adapter = adapter
        rv_notes_trash.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }
}