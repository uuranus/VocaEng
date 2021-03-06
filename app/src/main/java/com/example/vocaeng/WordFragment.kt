package com.example.vocaeng

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vocaeng.databinding.AddvocaBinding
import com.example.vocaeng.databinding.EditvocaBinding
import com.example.vocaeng.databinding.FragmentWordBinding


class WordFragment : Fragment() {
    var binding:FragmentWordBinding?=null
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: MyAdapter
    lateinit var myDBHelper: MyDBHelper
    var data=ArrayList<Voca>()
    var tablename="TOEFL"
    var color2=0
    var gray=0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding= FragmentWordBinding.inflate(layoutInflater,container,false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initRecyclerView("TOEFL")
    }

    private fun init() {
        color2=(activity as MainActivity).getColor2()
        gray=R.color.gray

        myDBHelper= MyDBHelper(requireContext())
        binding!!.TOEFL.setOnClickListener {
            tablename="TOEFL"
            initRecyclerView(tablename)
        }
        binding!!.TOEIC.setOnClickListener {
            tablename="TOEIC"
            initRecyclerView(tablename)
        }
        binding!!.SOONEUNG.setOnClickListener {
            tablename="SOONEUNG"
            initRecyclerView(tablename)
        }
        binding!!.addvoca.setOnClickListener {
            val dlgBinding= AddvocaBinding.inflate(layoutInflater)
            val dlgbuilder= AlertDialog.Builder(requireActivity())
            dlgbuilder.setView(dlgBinding.root)
                .setTitle("?????? ????????????")
                .setPositiveButton("??????"){
                        _,_ ->
                        val word=dlgBinding.editaddword.text.toString()
                        val meaning=dlgBinding.editaddmeaning.text.toString()

                        if(word.length==0||meaning.length==0){
                            Toast.makeText(activity,"?????? ?????? ??????",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            val flag=myDBHelper.insertVoca(Voca(word,meaning,null,0),tablename)
                            if(flag==0L){
                                Toast.makeText(activity,"$word ????????? ?????? ???????????????",Toast.LENGTH_SHORT).show()
                            }
                            else if(flag>0L){
                                Toast.makeText(activity,"$word ????????? ???????????????",Toast.LENGTH_SHORT).show()
                            }
                            else if(flag==-1L){
                                Toast.makeText(activity,"$word ?????? ?????? ??????",Toast.LENGTH_SHORT).show()
                            }
                            initRecyclerView(tablename)
                        }
                }
                .setNegativeButton("??????"){
                        _,_ ->
                }
            val dlg=dlgbuilder.create()
            dlg.show()
        }
    }

    private fun initRecyclerView(tablename:String) {
        recyclerView=binding!!.recyclerview
        recyclerView.layoutManager= LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        data=myDBHelper.getAllRecord(tablename)
        adapter= MyAdapter(data,color2)
        adapter.itemClickListener=object:MyAdapter.OnItemClickListener {
            override fun OnItemClick(
                    holder: MyAdapter.ViewHolder,
                    view: View,
                    data: Voca,
                    position: Int
            ) {
                if(holder.meaning.visibility==View.VISIBLE){
                    holder.meaning.visibility=View.GONE
                }
                else {
                    holder.meaning.visibility= View.VISIBLE
                }
            }

            override fun OnStarClick(
                holder: MyAdapter.ViewHolder,
                view: View,
                data: Voca,
                position: Int
            ) {
                 if(!view.isSelected) { //???????????? ???????????? ?????? ???????????????
                     view.isSelected = true
                     myDBHelper.insertVoca(Voca(data.word,data.meaning,tablename,-1),"MYWORD")
                     myDBHelper.updateVoca(Voca(data.word,data.meaning,null,1),tablename)
                 }
                else{
                    view.isSelected=false
                     myDBHelper.deleteVoca2(data.word,"MYWORD",tablename)
                     myDBHelper.updateVoca(Voca(data.word,data.meaning,null,0),tablename)
                 }


            }

            override fun OnItemLongClick(
                holder: MyAdapter.ViewHolder,
                view: View,
                data: Voca,
                position: Int
            ): Boolean {
                editAlertDlg(holder,data)
                return true
            }

        }
        recyclerView.adapter=adapter

        val simpleCallback=object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN or ItemTouchHelper.UP,
            ItemTouchHelper.LEFT){
            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteAlertDlg(viewHolder)
            }

        }
        val itemTouchHelper= ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    fun deleteAlertDlg(viewHolder: RecyclerView.ViewHolder){
        val word=data.get(viewHolder.adapterPosition).word
        val builder=AlertDialog.Builder(requireContext())
        builder.setMessage("?????? ????????? ?????????????????????????")
                .setPositiveButton("???"){
                    _,_ ->
                        adapter.removeItem(viewHolder.adapterPosition)
                        myDBHelper.deleteVoca(word,tablename)
                        myDBHelper.deleteVoca2(word,"MYWORD",tablename) //myword??? ????????????????????? ?????? ??????
                }
                .setNegativeButton("?????????"){
                    _,_ ->
                }
        val dlg=builder.create()
        dlg.show()
    }

    fun editAlertDlg(viewHolder: RecyclerView.ViewHolder,data:Voca){
        val dlgBinding= EditvocaBinding.inflate(layoutInflater)
        dlgBinding.addwordview.text=data.word
        dlgBinding.editaddmeaning.setText(data.meaning)
        val builder=AlertDialog.Builder(requireContext())
        builder.setView(dlgBinding.root)
            .setTitle("?????? ????????????")
            .setPositiveButton("???"){
                    _,_ ->
                    val word=dlgBinding.addwordview.text.toString()
                    val meaning=dlgBinding.editaddmeaning.text.toString()
                    adapter.editItem(viewHolder.adapterPosition,meaning)
                    myDBHelper.updateVoca(Voca(word,meaning,null,data.isChecked),tablename)
                    myDBHelper.updateVoca(Voca(word,meaning,tablename,-1),"MYWORD") //myword??? ????????????
            }
            .setNegativeButton("?????????"){
                    _,_ ->
            }
        val dlg=builder.create()
        dlg.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding=null
    }
}