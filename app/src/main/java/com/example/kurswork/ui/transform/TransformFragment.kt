package com.example.kurswork.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.kurswork.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurswork.databinding.FragmentTransformBinding
import com.example.kurswork.databinding.ItemTransformBinding
import com.example.kurswork.models.Post
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class TransformFragment : Fragment() {

    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: TransformAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransformBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewTransform
        adapter = TransformAdapter()
        recyclerView.adapter = adapter

        // Инициализация Firebase Database
        database = FirebaseDatabase.getInstance().getReference("posts")

        // Получение данных из Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<Post>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(Post::class.java)
                    post?.let {
                        posts.add(it) // Добавляем пост в список
                    }
                }
                adapter.submitList(posts) // Обновляем адаптер
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class TransformAdapter :
        ListAdapter<Post, TransformViewHolder>(object : DiffUtil.ItemCallback<Post>() {

            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.image == newItem.image

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = ItemTransformBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TransformViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            val post = getItem(position)
            holder.textView.text = post.description
            Glide.with(holder.imageView.context)
                .load(post.image)
                .into(holder.imageView)

            holder.itemView.setOnClickListener {
                // Создание нового экземпляра EditPostFragment
                val editPostFragment = EditPostFragment().apply {
                    arguments = Bundle().apply {
                        putString("postId", post.id) // Убедитесь, что у вас есть уникальный идентификатор поста
                        putString("description", post.description)
                        putString("image", post.image)
                    }
                }

                // Открытие фрагмента редактирования поста
                (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, editPostFragment) // Замените на ваш контейнер
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    class TransformViewHolder(binding: ItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val imageView: ImageView = binding.imageViewItemTransform
        val textView: TextView = binding.textViewItemTransform
    }
}