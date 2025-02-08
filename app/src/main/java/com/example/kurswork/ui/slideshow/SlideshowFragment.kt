package com.example.kurswork.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kurswork.R
import com.example.kurswork.models.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SlideshowFragment : Fragment() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var usernameTextView: TextView // Для отображения имени пользователя
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slideshow, container, false)

        // Инициализация элементов интерфейса
        usernameEditText = view.findViewById(R.id.editTextUsername)
        passwordEditText = view.findViewById(R.id.editTextPassword)
        loginButton = view.findViewById(R.id.buttonLogin)
        statusTextView = view.findViewById(R.id.textViewStatus)

        // Инициализация элементов Toolbar
        usernameTextView = activity?.findViewById(R.id.usernameTextView) ?: throw IllegalStateException("Toolbar not found")

        // Установка обработчика нажатия на кнопку входа
        loginButton.setOnClickListener {
            loginUser()
        }

        return view
    }

    /**
     * Обрабатывает логин пользователя.
     */
    private fun loginUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Проверка на пустые поля
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка существования пользователя в Firebase
        checkUserCredentials(username, password) { userExists ->
            if (userExists) {
                updateLoginState(username) // Обновляем состояние после успешного входа
                Toast.makeText(requireContext(), "Вы успешно вошли", Toast.LENGTH_SHORT).show()
            } else {
                statusTextView.text = "Неверное имя пользователя или пароль!"
            }
        }
    }

    /**
     * Проверяет учетные данные пользователя в базе данных.
     */
    private fun checkUserCredentials(username: String, password: String, callback: (Boolean) -> Unit) {
        database.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userFound = false
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Users::class.java)
                    if (user != null && user.userPassword == password) {
                        userFound = true
                        break
                    }
                }
                callback(userFound)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Ошибка подключения к базе данных", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }

    /**
     * Обновляет состояние после успешного входа/регистрации.
     */
    private fun updateLoginState(username: String) {
        statusTextView.text = "Вы вошли как $username"
        statusTextView.setTextColor(resources.getColor(android.R.color.holo_green_dark))
        usernameTextView.text = username // Обновляем имя пользователя в тулбаре
    }

    /**
     * Возвращает состояние "Unlogin" и скрывает кнопку выхода.
     */
    private fun resetLoginState() {
        statusTextView.text = "Необходимо войти"
        statusTextView.setTextColor(resources.getColor(android.R.color.holo_red_dark))
        usernameTextView.text = "Unlogin" // Сбрасываем текст в "Unlogin"
    }

    /**
     * Логика выхода из аккаунта.
     */
    private fun logout() {
        resetLoginState() // Сброс состояния
        Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
        // Дополнительные действия при выходе (например, очистка данных сессии)
    }
}