package com.jonathan.trace.study.trace.coketlist

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailPrivateAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.fragment_private.*

class PrivateFragment: Fragment(){
    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailPrivateAdapter
    private lateinit var warnDeleteDialog: MyDialog
    private lateinit var pwDialog: AlertDialog
    private var selectedNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_private, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        setNotes()
        setDialog()
        setAdapter()
        setOnBackPressed()
        setDrawer()
        setAppBar()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    private fun setNotes(){
        notes = mViewModel.getAllPrivateNotes()
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
            val action = PrivateFragmentDirections.actionPrivateFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun setAppBar(){
        requireActivity().findViewById<ImageView>(R.id.iv_hamburger).setImageResource(android.R.color.transparent)
        val toolBar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolBar.setNavigationIcon(R.drawable.back)
        toolBar.navigationIcon?.setTint(resources.getColor(R.color.icons))
        toolBar.setNavigationOnClickListener{
            findNavController().navigate(PrivateFragmentDirections.actionPrivateFragmentToHomeFragment())
        }
    }

    private fun setDialog(){
        warnDeleteDialog = MyDialog(requireContext()){  //pClickListener
            selectedNote?.let {
                mViewModel.delete(it)
                Toast.makeText(context, "Note deleted.", Toast.LENGTH_SHORT).show()
            }
            warnDeleteDialog.dismiss()
        }

        val builderPw = AlertDialog.Builder(requireContext())
        val et = EditText(requireContext())
        et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builderPw.setView(et)
            .setTitle(getString(R.string.pw))
            .setPositiveButton("OK"){ _, _ ->
                confirmPw(et.text.toString())
            }
        pwDialog = builderPw.create()
    }

    private fun confirmPw(s: String){
        if(s == selectedNote!!.pw){
            val action = PrivateFragmentDirections.actionPrivateFragmentToEditNoteFragment()
            action.note = selectedNote
            findNavController().navigate(action)
        }
        else
            Toast.makeText(context, "Invalid Password.", Toast.LENGTH_SHORT).show()
    }

    private fun setAdapter(){
        adapter = ThumbnailPrivateAdapter(
            notes.value as MutableList<Note>? ?: mutableListOf<Note>(),
            object: ThumbnailPrivateAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    selectedNote = item as Note
                    pwDialog.show()
                }
            },
            object: ThumbnailPrivateAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    selectedNote = item as Note
                    warnDeleteDialog.show()
                    warnDeleteDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion_permanent_private)
                }
            })

        rv_notes_private.adapter = adapter
        rv_notes_private.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

}