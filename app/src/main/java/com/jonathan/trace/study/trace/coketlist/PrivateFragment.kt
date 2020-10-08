package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailPrivateAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.fragment_private.*

class PrivateFragment: Fragment(){
    private lateinit var mViewModel: NoteViewModel
    private lateinit var notes: LiveData<List<Note>>
    private lateinit var adapter: ThumbnailPrivateAdapter
    private lateinit var warnDialog: MyDialog
    private lateinit var pwDialog: AlertDialog
    private var selectedNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_private, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)
        notes = mViewModel.getAllPrivateNotes()
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
            val action = PrivateFragmentDirections.actionPrivateFragmentToHomeFragment()
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
                    warnDialog.show()
                    warnDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion_permanent_private)
                }
            })

        rv_notes_private.adapter = adapter
        rv_notes_private.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }
}