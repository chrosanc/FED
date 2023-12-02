package bangkit.project.fed.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import bangkit.project.fed.R
import com.bumptech.glide.Glide

class ImageDisplayFragment : Fragment() {

    private var selectedImageUri: Uri? = null

    companion object {
        private const val ARG_IMAGE_URI = "imageUri"

        fun newInstance(imageUri: Uri?): ImageDisplayFragment {
            val fragment = ImageDisplayFragment()
            val args = Bundle()
            args.putParcelable(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedImageUri = it.getParcelable(ARG_IMAGE_URI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayImage()
    }

    private fun displayImage() {
        val picture : ImageView? = view?.findViewById(R.id.picture)
        Glide.with(requireContext()).load(selectedImageUri).into(picture!!)
    }
}
