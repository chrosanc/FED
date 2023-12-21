package bangkit.project.fed.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import bangkit.project.fed.databinding.FragmentHomeBinding
import bangkit.project.fed.ui.home.adapter.LibraryRvAdapter
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: LibraryRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            viewModel.fetchEggDataByUserId(userId)
        }

        getLibraryList()

        return binding.root
    }

    private fun getLibraryList() {

        adapter = LibraryRvAdapter(requireContext())
        binding.LibraryRv.adapter = adapter
        binding.LibraryRv.layoutManager = LinearLayoutManager(requireContext())

        viewModel.eggDataList.observe(viewLifecycleOwner) { eggDataList ->
            adapter.submitList(eggDataList)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}