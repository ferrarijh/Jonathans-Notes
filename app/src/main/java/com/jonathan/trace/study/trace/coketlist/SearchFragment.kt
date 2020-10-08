package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment: Fragment(){
    private lateinit var mViewModel: NoteViewModel
    private lateinit var notesAllLive: LiveData<List<Note>>
    private var notesAll = listOf<Note>()
    private var notes = mutableListOf<Note>()
    private lateinit var adapter: ThumbnailAdapter
    private lateinit var myDialog: MyDialog
    private var selectedNote: Note? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        notesAllLive = mViewModel.getAllNotes()
        notesAllLive.observe(viewLifecycleOwner){
            notesAll = it
        }
        setTextChangedListener()
        setAdapter()
        setDialog()
        setOnBackPressed()
        setOtherUI()
    }

    private fun setOtherUI(){
        val parent = requireActivity()
        parent.findViewById<FloatingActionButton>(R.id.fab_add).hide()
        (parent as AppCompatActivity).supportActionBar!!.hide()

        val back = parent.findViewById<ImageView>(R.id.iv_back)
        back.setOnClickListener{
            val action = SearchFragmentDirections.actionSearchFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        setDrawer()
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this){
            val action = SearchFragmentDirections.actionSearchFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun setDialog(){
        myDialog = MyDialog(requireContext()){  //pClickListener
            selectedNote?.let {
                mViewModel.delete(it)
                Toast.makeText(context, "Note deleted.", Toast.LENGTH_SHORT).show()
            }
            myDialog.dismiss()
        }
    }

    private fun setAdapter(){
        adapter = ThumbnailAdapter(
            mutableListOf<Note>(),
            object: ThumbnailAdapter.ThumbnailAdapterListener{
                override fun <T> onClickItem(item: T) {
                    val action = SearchFragmentDirections.actionSearchFragmentToEditNoteFragment()
                    action.note = item as Note

                    requireActivity().findViewById<AppBarLayout>(R.id.appBar).setExpanded(true)
                    findNavController().navigate(action)

                }
            },
            object: ThumbnailAdapter.ThumbnailAdapterLongListener{
                override fun <T> onLongClickItem(item: T) {
                    selectedNote = item as Note
                    myDialog.show()
                    myDialog.findViewById<TextView>(R.id.tv_dialog_title).text = getString(R.string.warn_deletion)
                }
            })

        rv_notes_searched.adapter = adapter
        rv_notes_searched.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun setTextChangedListener(){
        et_search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let{
                    notes.clear()
                    if (!it.isBlank()){
                        notesAll.forEach{n ->
                            if(it in n.title || it in n.body){
                                notes.add(n)
                            }
                        }
                    }
                    adapter.updateList(notes)
                    Log.d("", "adapter data size: ${adapter.thumbnailsVisible.size}")
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

}