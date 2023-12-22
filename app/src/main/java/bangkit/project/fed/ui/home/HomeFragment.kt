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
import bangkit.project.fed.ui.home.adapter.RecentRvAdapter
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: HomeViewModel
    private lateinit var libraryAdapter: LibraryRvAdapter
    private lateinit var recentAdapter: RecentRvAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        getLibraryList()
        getRecentList()

        return binding.root
    }

    private fun getRecentList() {

        val currentUser = auth.currentUser
        currentUser?.uid?.let {
            viewModel.fetchEggDataByRecentDate(it)
        }

        recentAdapter = RecentRvAdapter(requireContext())
        binding.recentRv.adapter = recentAdapter
        binding.recentRv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.eggDataList.observe(viewLifecycleOwner) {eggDataList ->
            recentAdapter.submitList(eggDataList)
        }


    }

    private fun getLibraryList() {

        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            viewModel.fetchEggDataByUserId(userId)
        }

        libraryAdapter = LibraryRvAdapter(requireContext())
        binding.LibraryRv.adapter = libraryAdapter
        binding.LibraryRv.layoutManager = LinearLayoutManager(requireContext())

        viewModel.eggDataList.observe(viewLifecycleOwner) { eggDataList ->
            libraryAdapter.submitList(eggDataList)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}