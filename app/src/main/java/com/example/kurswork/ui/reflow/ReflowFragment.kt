package com.example.kurswork.ui.reflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.kurswork.R
import com.example.kurswork.models.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class ReflowFragment : Fragment() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var termsCheckBox: CheckBox
    private lateinit var registerButton: Button
    private lateinit var usernameTextView: TextView // Для отображения имени пользователя
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reflow, container, false)

        // Инициализация элементов интерфейса
        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        termsCheckBox = view.findViewById(R.id.termsCheckBox)
        registerButton = view.findViewById(R.id.registerButton)

        // Инициализация элементов Toolbar
        usernameTextView = activity?.findViewById(R.id.usernameTextView) ?: throw IllegalStateException("Toolbar not found")

        // Установка обработчика нажатия на кнопку регистрации
        registerButton.setOnClickListener {
            registerUser()
        }
        return view
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        // Проверка на пустые поля
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на совпадение паролей
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на согласие с условиями
        if (!termsCheckBox.isChecked) {
            Toast.makeText(requireContext(), "Вы должны согласиться с условиями", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка наличия пользователя с таким же именем в Firebase
        checkIfUserExists(username) { userExists ->
            if (userExists) {
                Toast.makeText(requireContext(), "Пользователь с таким именем уже существует", Toast.LENGTH_SHORT).show()
            } else {
                // Генерация случайного ID
                val userId = UUID.randomUUID().toString()

                // Создание объекта пользователя
                val user = Users(id = userId, username = username, userPassword = password)

                // Сохранение пользователя в Firebase
                database.child(userId).setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Обновление состояния после успешной регистрации
                        updateLoginState(username)
                        Toast.makeText(requireContext(), "Регистрация успешна", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Обновляет состояние после успешного входа/регистрации.
     */
    private fun updateLoginState(username: String) {
        usernameTextView.text = username // Обновляем имя пользователя
    }

    /**
     * Возвращает состояние "Unlogin" и скрывает кнопку выхода.
     */
    private fun resetLoginState() {
        usernameTextView.text = "Unlogin" // Возвращаем текст к "Unlogin"
    }

    /**
     * Логика выхода из аккаунта.
     */
    private fun logout() {
        resetLoginState() // Сброс состояния
        Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
        // Дополнительные действия при выходе (например, очистка данных сессии)
    }

    /**
     * Проверяет, существует ли пользователь с указанным именем в базе данных.
     */
    private fun checkIfUserExists(username: String, callback: (Boolean) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userExists = false
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Users::class.java)
                    if (user != null && user.username == username) {
                        userExists = true
                        break
                    }
                }
                callback(userExists)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Ошибка при проверке существования пользователя: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
        })
    }
}