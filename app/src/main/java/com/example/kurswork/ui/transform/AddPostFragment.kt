package com.example.kurswork.ui.transform

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.kurswork.models.Post
import com.example.kurswork.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddPostFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var editTextDescription: EditText
    private lateinit var editTextImageUrl: EditText
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.addpost_fragment, container, false)

        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextImageUrl = view.findViewById(R.id.editTextImageUrl)
        val buttonAdd = view.findViewById<Button>(R.id.buttonAdd)
        val buttonSelectImage = view.findViewById<Button>(R.id.buttonSelectImage) // Кнопка для выбора изображения

        database = FirebaseDatabase.getInstance().getReference("posts")

        buttonAdd.setOnClickListener {
            addPost()
        }

        buttonSelectImage.setOnClickListener {
            openGallery()
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            editTextImageUrl.setText(imageUri.toString()) // Устанавливаем URI изображения в EditText
        }
    }

    private fun addPost() {
        val description = editTextDescription.text.toString()
        val imageUrl = editTextImageUrl.text.toString()

        // Создание нового поста с уникальным ID
        val newPostRef = database.push() // Создает уникальный ключ
        val newPost = Post(id = newPostRef.key ?: "", description = description, image = imageUrl)

        // Сохранение поста в Firebase
        newPostRef.setValue(newPost)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Пост добавлен", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed() // Вернуться к предыдущему фрагменту или активности
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка при добавлении поста", Toast.LENGTH_SHORT).show()
            }
    }
}