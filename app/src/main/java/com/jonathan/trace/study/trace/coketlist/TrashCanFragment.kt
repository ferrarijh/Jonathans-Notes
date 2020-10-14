package com.jonathan.trace.study.trace.coketlist

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import kotlinx.android.synthetic.main.fragment_trash_can.*

class TrashCanFragment : Fragment() {

    private lateinit var nViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var warnClearDialog: MyDialog
    private lateinit var selectDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_trash_can, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        setNotes()
        setDialog()
        setAdapter()
        setAppBar()
        setDrawer()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_trash_can, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        warnClearDialog.show()
        warnClearDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_clear_all)
        return super.onOptionsItemSelected(item)
    }

    private fun setNotes(){
        notes = nViewModel.getAllTrashNotes()
        notes.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setAppBar(){
        val parent = requireActivity() as AppCompatActivity

        parent.findViewById<ImageView>(R.id.iv_hamburger).setImageResource(android.R.color.transparent)

        val toolBar = parent.findViewById<Toolbar>(R.id.toolbar)
        parent.setSupportActionBar(toolBar)
        toolBar.setNavigationIcon(R.drawable.back)
        toolBar.navigationIcon?.setTint(resources.getColor(R.color.icons))
        toolBar.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
        parent.supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    private fun setDialog(){

        warnDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_deletion_permanent)){  //pClickListener
            val notePointed = nViewModel.getNotePointed()!!.second
            notePointed.let {
                nViewModel.delete(it)
                Toast.makeText(context, getString(R.string.deleted_perma), Toast.LENGTH_SHORT).show()
            }
            warnDialog.dismiss()
        }

        val permaDel = getString(R.string.permanent_deletion)
        val recover = getString(R.string.recover)
        val items = arrayOf(permaDel, recover)
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(items){_, b ->
            when(items[b]){
                permaDel -> warnDialog.show()
                recover -> recoverNote()
            }
        }
        selectDialog = builder.create()

        warnClearDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_clear_all)){
            nViewModel.deleteAll()
            Toast.makeText(context, getString(R.string.cleared), Toast.LENGTH_SHORT).show()
            warnClearDialog.dismiss()
        }
    }

    private fun recoverNote(){
        val notePointed = nViewModel.getNotePointed()!!.second
        notePointed.trash = 0
        nViewModel.update(notePointed)
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            notes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            R.layout.thumbnail_trash,
            ThumbnailAdapter.TRASH,
            object: ThumbnailAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    goToViewMode(item as Note)
                }
            },
            object: ThumbnailAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    //selectedNote = item as Note
                    selectDialog.show()
                }
            })

        rv_notes_trash.adapter = adapter
        val ori = requireActivity().resources.configuration.orientation
        if(ori == Configuration.ORIENTATION_LANDSCAPE)
            rv_notes_trash.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        else if (ori == Configuration.ORIENTATION_PORTRAIT)
            rv_notes_trash.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun goToViewMode(item: Note){
        val action = TrashCanFragmentDirections.actionTrashCanFragmentToViewModeFragment(item)
        requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
        findNavController().navigate(action)
    }
}