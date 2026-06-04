package uz.yuk24.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import uz.yuk24.app.presentation.common.theme.Yuk24Theme
import uz.yuk24.app.presentation.navigation.Yuk24App

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Yuk24Theme {
                Yuk24App()
            }
        }
    }
}
