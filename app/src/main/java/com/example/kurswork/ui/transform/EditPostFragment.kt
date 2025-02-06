package com.example.kurswork.ui.transform

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kurswork.models.Post
import com.example.kurswork.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class EditPostFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var postId: String
    private lateinit var editTextDescription: EditText
    private lateinit var editTextImageUrl: EditText
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.editpost_fragment, container, false)

        editTextDescription = view.findViewById(R.id.editTextDescription)
        editTextImageUrl = view.findViewById(R.id.editTextImageUrl)
        val buttonSave = view.findViewById<Button>(R.id.buttonSave)
        val buttonDelete = view.findViewById<Button>(R.id.buttonDelete)
        val buttonSelectImage = view.findViewById<Button>(R.id.buttonSelectImage) // Кнопка для выбора изображения

        // Получение данных из аргументов
        postId = arguments?.getString("postId") ?: ""
        val description = arguments?.getString("description") ?: ""
        val image = arguments?.getString("image") ?: ""

        // Установка текста в EditText
        editTextDescription.setText(description)
        editTextImageUrl.setText(image)

        // Инициализация базы данных
        database = FirebaseDatabase.getInstance().getReference("posts")

        // Обработчик нажатия на кнопку "Сохранить"
        buttonSave.setOnClickListener {
            updatePost()
        }

        // Обработчик нажатия на кнопку "Удалить"
        buttonDelete.setOnClickListener {
            deletePost()
        }

        // Обработчик нажатия на кнопку "Выбрать изображение"
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
            val imageUri = data.data
            editTextImageUrl.setText(imageUri.toString()) // Устанавливаем URI изображения в EditText
        }
    }

    private fun updatePost() {
        val updatedDescription = editTextDescription.text.toString()
        val updatedImageUrl = editTextImageUrl.text.toString()

        // Создание обновленного поста
        val updatedPost = Post(postId, updatedDescription, updatedImageUrl)
        database.child(postId).setValue(updatedPost)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Пост обновлен", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed() // Вернуться к предыдущему фрагменту или активности
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка при обновлении поста", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deletePost() {
        // Убедитесь, что postId не пустой
        if (postId.isNotEmpty()) {
            database.child(postId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Пост удален", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed() // Вернуться к предыдущему фрагменту или активности
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Ошибка при удалении поста", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Ошибка: ID поста не найден", Toast.LENGTH_SHORT).show()
        }
    }
}