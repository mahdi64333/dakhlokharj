package ir.demoodite.dakhlokharj.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.demoodite.dakhlokharj.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}