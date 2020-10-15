package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.jonathan.trace.study.trace.coketlist.thumbnail.adapter.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.dialog.MyDialog
import com.jonathan.trace.study.trace.coketlist.dialog.PwDialog
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment: Fragment(){
    private val nViewModel by lazy{ViewModelProvider(requireActivity()).get(NoteViewModel::class.java)}
    private val fViewModel by lazy {ViewModelProvider(this).get(FragmentStateViewModel::class.java)}
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var deleteDialog: MyDialog
    private lateinit var optionsDialog: AlertDialog
    private lateinit var pwDialog: PwDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewModel()
        setTextChangedListener()
        setDialog()
        setAdapter()
        setOtherUI()
    }

    private fun setViewModel(){
        if(fViewModel.curNotesAll == null){
            val newNotesLive = getNewNotesBySort()
            fViewModel.setCurNotesAll(newNotesLive)
        }
        fViewModel.curNotesAll!!.observe(viewLifecycleOwner){
            //do nothing. Just for update
        }
    }

    private fun getNewNotesBySort(): LiveData<List<Note>> {
        return when(nViewModel.prevSort.value){
            NoteViewModel.MODIFIED -> nViewModel.getAllNotes()
            NoteViewModel.CREATED -> nViewModel.getAllNotesByCreated()
            NoteViewModel.TITLE -> nViewModel.getAllNotesByTitle()
            NoteViewModel.BODY -> nViewModel.getAllNotesByBody()
            NoteViewModel.COLOR -> nViewModel.getAllNotesByColor()
            else -> nViewModel.getAllNotes()
        }
    }

    private fun setOtherUI(){
        //hide toolbar
        val parent = requireActivity() as AppCompatActivity
        parent.setSupportActionBar(parent.findViewById(R.id.toolbar))
        parent.supportActionBar!!.hide()

        //set home button to back
        val back = parent.findViewById<ImageView>(R.id.iv_back)
        back.setOnClickListener{
            findNavController().navigateUp()
        }

        //set drawer
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setDialog(){
        //delete dialog
        deleteDialog = MyDialog(requireContext(), R.layout.dialog, getString(R.string.warn_trash)){  //pClickListener
            val notePointed = nViewModel.getNotePointed()?.second
            notePointed?.let {
                it.trash = 1
                nViewModel.update(it)

                val newNotesLive = getNewNotesBySort()
                fViewModel.setCurNotesAll(newNotesLive)
                fViewModel.curNotesAll!!.observe(viewLifecycleOwner){
                    //do nothing
                }

                setAfterModified()

                Toast.makeText(context, getString(R.string.moved_to_trash), Toast.LENGTH_SHORT).show()
            }
            deleteDialog.dismiss()
            nViewModel.setSelMode(NoteViewModel.OFF)
        }

        //password dialog
        pwDialog = PwDialog(requireContext()){
            val pw = pwDialog.findViewById<EditText>(R.id.et_pw_note).text.toString()
            val pwConfirm = pwDialog.findViewById<EditText>(R.id.et_pw_confirm_note).text.toString()
            if(pw.isBlank() || pwConfirm.isBlank()) {
                Toast.makeText(context, getString(R.string.warn_blank_pw), Toast.LENGTH_SHORT).show()
            } else if(pw != pwConfirm){
                Toast.makeText(context, getString(R.string.pw_confirm_mismatch), Toast.LENGTH_SHORT).show()
            } else {
                setPw(pw)
                pwDialog.dismiss()
                nViewModel.setSelMode(NoteViewModel.OFF)
            }
        }

        //options Dialog
        val setPw = getString(R.string.set_pw)
        val trash = getString(R.string.trash)
        val options = arrayOf(trash, setPw)

        val oBuilder = AlertDialog.Builder(requireContext())
        oBuilder.setItems(options){ _, i ->
            when(options[i]){
                setPw -> pwDialog.show()
                trash -> deleteDialog.show()
            }
        }.setOnDismissListener{
            nViewModel.setSelMode(NoteViewModel.OFF)
        }
        optionsDialog = oBuilder.create()
    }

    private fun setPw(s: String){
        val notePointed = nViewModel.getNotePointed()!!.second
        notePointed.pw = s
        nViewModel.update(notePointed)
        setAfterModified()
        Toast.makeText(context, getString(R.string.pw_set), Toast.LENGTH_SHORT).show()
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            mutableListOf(),
            R.layout.thumbnail,
            ThumbnailAdapter.HOME,
            object: ThumbnailAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    goToEditNoteWith()
                }
            },
            object: ThumbnailAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    optionsDialog.show()
                }
            }
        )

        rv_notes_searched.adapter = adapter
        rv_notes_searched.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if(fViewModel.curNotes.value!!.isNotEmpty()) {
            adapter.updateList(fViewModel.curNotes.value!!)
        }

        fViewModel.curNotes.observe(viewLifecycleOwner){list->
            adapter.updateList(list)
        }
    }

    private fun setTextChangedListener(){
        et_search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //no action
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let{
                    val notes = fViewModel.curNotes.value!!
                    notes.clear()
                    if (!it.isBlank()){
                        val str = s.toString().toLowerCase()
                        val notesAll = fViewModel.curNotesAll!!.value!!
                        notesAll.forEach{n ->
                            if(str in n.title.toLowerCase() || str in n.body.toLowerCase()){
                                notes.add(n)
                            }
                        }
                    }
                    fViewModel.curNotes.value = notes
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //no action
            }
        })
    }

    private fun setAfterModified(){
        val goneNote = nViewModel.getNotePointed()!!.second
        fViewModel.curNotes.value!!.remove(goneNote)
        fViewModel.curNotes.value = fViewModel.curNotes.value!!
    }

    private fun goToEditNoteWith(){
        val action = SearchFragmentDirections.actionSearchFragmentToEditNoteFragment()
        action.fromSearch = true

        requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
        findNavController().navigate(action)
    }

}