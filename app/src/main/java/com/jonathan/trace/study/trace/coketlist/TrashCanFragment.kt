package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailTrashAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.fragment_trash_can.*

class TrashCanFragment : Fragment() {

    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailTrashAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var warnClearDialog: AlertDialog
    private lateinit var selectDialog: AlertDialog
    private var selectedNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_trash_can, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        setNotes()
        setDialog()
        setAdapter()
        setAppBar()
        setOnBackPressed()
        setDrawer()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_trash_can, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        warnClearDialog.show()
        return super.onOptionsItemSelected(item)
    }

    private fun setNotes(){
        notes = mViewModel.getAllTrashNotes()
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
        val parent = requireActivity() as AppCompatActivity

        parent.findViewById<ImageView>(R.id.iv_hamburger).setImageResource(android.R.color.transparent)

        val toolBar = parent.findViewById<Toolbar>(R.id.toolbar)
        parent.setSupportActionBar(toolBar)
        toolBar.setNavigationIcon(R.drawable.back)
        toolBar.navigationIcon?.setTint(resources.getColor(R.color.icons))
        toolBar.setNavigationOnClickListener{
            val action = TrashCanFragmentDirections.actionTrashCanFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        parent.supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun setDialog(){

        warnDialog = MyDialog(requireContext()){  //pClickListener
            selectedNote?.let {
                mViewModel.delete(it)
                Toast.makeText(context, "Note deleted.", Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        val permaDel = getString(R.string.permanent_deletion)
        val recover = getString(R.string.recover)
        val items = arrayOf(permaDel, recover)
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(items){_, b ->
            when(items[b]){
                permaDel -> deleteNote()
                recover -> recoverNote()
            }
        }
        selectDialog = builder.create()

        val builderClear = AlertDialog.Builder(requireContext())
        builderClear.setTitle(getString(R.string.warn_clear_all))
            .setPositiveButton("OK"){ _, _ ->
                mViewModel.deleteAll()
                Toast.makeText(context, "All trashed notes deleted.", Toast.LENGTH_SHORT).show()
            }
        warnClearDialog = builderClear.create()
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