package com.example.kurswork.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kurswork.R
import android.text.TextWatcher
import com.example.kurswork.databinding.FragmentTransformBinding
import com.example.kurswork.databinding.ItemTransformBinding
import com.example.kurswork.models.Post
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.text.Editable
import com.google.firebase.database.ValueEventListener

class TransformFragment : Fragment() {
    private var _binding: FragmentTransformBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: TransformAdapter
    private var isUserLoggedIn = false // Флаг для отслеживания статуса входа

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
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
        })

        // Проверка статуса входа пользователя
        checkUserLoginStatus()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Метод для установки статуса входа пользователя
    fun setUserLoginStatus(loggedIn: Boolean) {
        if (isUserLoggedIn != loggedIn) { // Проверяем, изменился ли статус
            isUserLoggedIn = loggedIn
            adapter.updateLoginStatus(loggedIn) // Обновляем статус в адаптере
        }
    }

    // Проверка статуса входа пользователя через TextView
    private fun checkUserLoginStatus() {
        val toolbar = activity?.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val usernameTextView = toolbar?.findViewById<TextView>(R.id.usernameTextView)

        usernameTextView?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Обновляем статус входа на основе текста в TextView
                val isLoggedIn = s.toString() != "Unlogin"
                setUserLoginStatus(isLoggedIn)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Внутренний класс адаптера
    inner class TransformAdapter :
        ListAdapter<Post, TransformViewHolder>(object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem.image == newItem.image

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean =
                oldItem == newItem
        }) {

        // Метод для обновления статуса входа
        fun updateLoginStatus(loggedIn: Boolean) {
            if (isUserLoggedIn != loggedIn) { // Проверяем, изменился ли статус
                isUserLoggedIn = loggedIn
                notifyDataSetChanged() // Перерисовываем весь список
            }
        }

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
                if (isUserLoggedIn) {
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
                } else {
                    // Показываем сообщение о необходимости входа
                    Toast.makeText(holder.itemView.context, "Please log in to view this post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ViewHolder для элементов списка
    inner class TransformViewHolder(val binding: ItemTransformBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageViewItemTransform
        val textView: TextView = binding.textViewItemTransform
    }
}