package com.jonathan.trace.study.trace.coketlist

import android.app.AlertDialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailPrivateAdapter
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
import com.jonathan.trace.study.trace.coketlist.dialog.PwCheckDialog
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import kotlinx.android.synthetic.main.fragment_private.*

class PrivateFragment: Fragment(){
    private lateinit var nViewModel: NoteViewModel
    private lateinit var privateNotes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var warnDeleteDialog: MyDialog
    private lateinit var pwCheckDialog: PwCheckDialog
    private lateinit var removeLockDialog: PwCheckDialog
    private lateinit var optionsDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_private, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        setNotes()
        setDialog()
        setAdapter()
        setDrawer()
        setAppBar()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun setNotes(){
        privateNotes = nViewModel.getAllPrivateNotes()
        privateNotes.observe(viewLifecycleOwner){
            adapter.updateList(it)
        }
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }


    private fun setAppBar(){
        requireActivity().findViewById<ImageView>(R.id.iv_hamburger).setImageResource(android.R.color.transparent)
        val toolBar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolBar.setNavigationIcon(R.drawable.back)
        toolBar.navigationIcon?.setTint(resources.getColor(R.color.icons))

        toolBar.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
    }

    private fun setDialog(){
        //warn permanent delete dialog
        warnDeleteDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_deletion_permanent_private)){  //pClickListener
            val notePointed = nViewModel.getNotePointed()!!.second
            notePointed.let {
                nViewModel.delete(it)
                Toast.makeText(context, getString(R.string.deleted), Toast.LENGTH_SHORT).show()
            }
            warnDeleteDialog.dismiss()
        }

        //pw check dialog
        pwCheckDialog = PwCheckDialog(requireContext()){
            val input = pwCheckDialog.findViewById<EditText>(R.id.et_pw_check).text.toString()
            val notePointed = nViewModel.getNotePointed()!!.second
            if(input == notePointed.pw){
                pwCheckDialog.dismiss()

                val toolBar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
                toolBar.navigationIcon = null

                val action = PrivateFragmentDirections.actionPrivateFragmentToEditNoteFragment()
                action.note = notePointed
                findNavController().navigate(action)
            }
            else
                Toast.makeText(context, getString(R.string.invalid_pw_note), Toast.LENGTH_SHORT).show()
        }

        //remove pw dialog
        removeLockDialog = PwCheckDialog(requireContext()){
            val input = removeLockDialog.findViewById<EditText>(R.id.et_pw_check).text.toString()
            val notePointed = nViewModel.getNotePointed()!!.second  //TODO("NullPointerException")
            if(input == notePointed.pw){
                notePointed.pw = null
                nViewModel.update(notePointed)
                //TODO("will adapter update itself?")

                pwCheckDialog.dismiss()
                Toast.makeText(context, getString(R.string.lock_removed), Toast.LENGTH_SHORT).show()
            }
            else
                Toast.makeText(context, getString(R.string.invalid_pw_note), Toast.LENGTH_SHORT).show()
        }

        //options dialog
        val oBuilder = AlertDialog.Builder(requireContext())
        val removeLock = getString(R.string.remove_lock)
        val permaDel = getString(R.string.permanent_deletion)
        val options = arrayOf(removeLock, permaDel)

        oBuilder.setItems(options){_, i ->
            when(options[i]){
                removeLock -> removeLockDialog.show()
                permaDel -> warnDeleteDialog.show()
            }
        }
        optionsDialog = oBuilder.create()
    }


    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            privateNotes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            R.layout.thumbnail_private,
            ThumbnailAdapter.PRIVATE,
            object: ThumbnailAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    pwCheckDialog.show()
                }
            },
            object: ThumbnailAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    optionsDialog.show()
                }
            })
        /*
        adapter = ThumbnailPrivateAdapter(
            privateNotes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            object: ThumbnailPrivateAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    pwCheckDialog.show()
                }
            },
            object: ThumbnailPrivateAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    optionsDialog.show()
                }
            })

         */

        rv_notes_private.adapter = adapter
        val ori = requireActivity().resources.configuration.orientation
        if(ori == Configuration.ORIENTATION_LANDSCAPE)
            rv_notes_private.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        else if (ori == Configuration.ORIENTATION_PORTRAIT)
            rv_notes_private.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

}