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
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jonathan.trace.study.trace.coketlist.adapter.thumbnail.ThumbnailAdapter
import com.jonathan.trace.study.trace.coketlist.room.Note
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import com.jonathan.trace.study.trace.coketlist.viewmodel.FragmentStateViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment: Fragment(){
    private lateinit var mViewModel: NoteViewModel
    private lateinit var fViewModel: FragmentStateViewModel
    private lateinit var notesAllLive: LiveData<List<Note>>
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
        fViewModel = ViewModelProvider(this).get(FragmentStateViewModel::class.java)
        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        notesAllLive = mViewModel.getAllNotes()
        notesAllLive.observe(viewLifecycleOwner){
            fViewModel.curNotesAll = it
        }

        setTextChangedListener()
        setAdapter()
        setDialog()
        setOnBackPressed()
        setOtherUI()
    }

    private fun setOtherUI(){
        val parent = requireActivity() as AppCompatActivity
        parent.setSupportActionBar(parent.findViewById(R.id.toolbar))
        parent.supportActionBar!!.hide()

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

        if(fViewModel.curNotes.isNotEmpty()) {
            Log.d("","adapter.thumbnailsVisibe.size: ${adapter.thumbnailsVisible.size}")
            Log.d("", "fViewModel.curNotes.size: ${fViewModel.curNotes.size}")
            adapter.updateList(fViewModel.curNotes)
            Log.d("", "adapter.thumbnailsVisible.size: ${adapter.thumbnailsVisible.size}")
        }
    }

    private fun setTextChangedListener(){
        et_search.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("", "onTextChanged() called")
                s?.let{
                    fViewModel.curNotes.clear()
                    if (!it.isBlank()){
                        fViewModel.curNotesAll.forEach{n ->
                            if(it in n.title || it in n.body){
                                fViewModel.curNotes.add(n)
                            }
                        }
                    }
                    adapter.updateList(fViewModel.curNotes)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

}