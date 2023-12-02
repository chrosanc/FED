package bangkit.project.fed.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import bangkit.project.fed.databinding.FragmentSettingBinding
import bangkit.project.fed.ui.login.LoginActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel =
            ViewModelProvider(this)[SettingViewModel::class.java]
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            navigatetoLogin()
        }

        viewModel.userName.observe(viewLifecycleOwner, Observer { userName ->
            binding.nameText.text = userName
        })

        viewModel.userEmail.observe(viewLifecycleOwner, Observer {userEmail ->
            binding.emailText.text = userEmail
        })

        return binding.root
    }

    private fun navigatetoLogin() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}