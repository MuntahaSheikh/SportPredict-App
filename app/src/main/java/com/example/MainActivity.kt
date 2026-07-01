package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.AppViewModel
import com.example.ui.AppViewModelFactory
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Retrieve the repository singleton instance from the MainApplication
        val repository = (application as MainApplication).repository
        
        // Instantiate the single state ViewModel for the entire app lifecycle
        val viewModel: AppViewModel by viewModels { AppViewModelFactory(repository) }
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation(viewModel)
            }
        }
    }
}
